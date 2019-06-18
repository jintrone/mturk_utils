package scripts

import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils
import groovy.sql.Sql
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

import javax.xml.rpc.Call
import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Created by josh on 6/21/16.
 */
class BonusLoomWorkers {

    public static String database = "loom-analysis"
    public static String user = "loom"
    public static String password = "loom"

    public static float reject_bonus = 0.75f
    public static float complete_bonus = 1.75f
    public static float score_bonus = 1.00f
    public static float per_minute_bonus = 0.03f

    public static DecimalFormat df = new DecimalFormat("####0.00");
    public static SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy")

    public static SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd HH:mm:ss")
    public static SimpleDateFormat f2 = new SimpleDateFormat("yyyyMMdd HH:mm:ss")
    static {
        f2.setTimeZone(TimeZone.getTimeZone("GMT"))
    }



    public BonusLoomWorkers(int sessionid, File inputFile, String force = null, boolean dobonus = false) {

        Sql s = Sql.newInstance("jdbc:mysql://localhost/$database", user, password)
        CSVParser parser = new CSVParser(new FileReader(inputFile), CSVFormat.EXCEL.withHeader())
        RequesterService svc = Utils.requesterService


        def r = s.firstRow("select session.*, min(user_round_story.time) first_submit from session inner join user_round_story on " +
                "user_round_story.session_id = session.id and user_round_story.round = 0 where session.id=$sessionid" as String)
        def codes = [(r.waiting_code): "waiting", (r.done_code): "done", (r.full_code): "full"]
        long session_start = r.first_submit?.time?:0 - 90000

        Map results = s.rows("select user_session.*, user.username, AVG(ur_2.score) score from user_session " +
                "inner join session on session_id=session.id inner join user on user_id = user.id " +
                "left join user_round_story ur_2 on user_session.user_alias = ur_2.user_alias and " +
                "ur_2.session_id = session.id where session.id=$sessionid group by user_session.id" as String).collectEntries {

            [it.username, [score: it.score, completioncode: it.completion_code, started: it.started, stopped: it.stopped_waiting]]

        }



        Float total = 0.0f

        parser.each { row ->
            String worker = row.get("WorkerId")
            String assignment = row.get("AssignmentId")
            String code = row.get("Answer.surveycode")


            switch (code) {

                case { "waiting" in [codes[it], force] }:

                    Date stopped
                    if (results[worker]?.started) {


                        def started = converDBTime(results[worker].started)

                        if (results[worker].stopped) {
                            stopped = converDBTime(results[worker].stopped)

                        } else {

                            stopped = sdf.parse(row.get("SubmitTime"))

                        }

                        def amount = Math.min(1.80d,cleanDouble(per_minute_bonus * (stopped.time - started.time) / 60000.0f))
                        println "$worker WAITING ${results[worker] ? "$amount" : "no record"}"
                        if (dobonus) svc.grantBonus(worker, amount, assignment, "Bonus for waiting for the the Loom game")
                        total+=amount
                    } else {
                        println "$worker has no start time"
                    }

                    break;

                case { "full" in [codes[it], force] }:
                    println "$worker FULL ${results[worker] ? "waited for ${results[worker].started.time - session_start}" : "no record"}"
                    if (dobonus) svc.grantBonus(worker, reject_bonus as Double, assignment, "Bonus for attempting to join the Loom game when it was full")
                    total+=reject_bonus

                    break;

                case { "done" in [codes[it], force] }:
                    println "$worker DONE"
                    if (dobonus) svc.grantBonus(worker, reject_bonus as Double, assignment, "Bonus for attempting to join the Loom game after it finished")
                    total+=reject_bonus
                    break;

                case { results[worker]?.completioncode == it }:
                    double scoreBonus = Double.parseDouble(df.format(score_bonus * results[worker].score))
                    double bonus = Double.parseDouble(df.format(scoreBonus + complete_bonus))
                    println "$worker FINISHED " + " Bonus for completing the game (\$1.75 + \$$scoreBonus = \$$bonus)"

                    if (dobonus) svc.grantBonus(worker, bonus, assignment, "Bonus for completing the Loom game (\$1.75 + \$$scoreBonus = \$$bonus)")
                    total+=(scoreBonus+complete_bonus)
                    break;

                default:

                    if (results[worker]) {
                        double scoreBonus = Double.parseDouble(df.format(score_bonus * results[worker].score))
                        double bonus = Double.parseDouble(df.format(scoreBonus + complete_bonus))
                        println "$worker BUG " + " Bonus trying to play (encountered bug) the Loom game (\$1.75 + \$$scoreBonus = \$$bonus)"
                        if (dobonus) svc.grantBonus(worker, bonus, assignment, "Bonus for trying to play (despite encountering bugs) the Loom game (\$1.75 + \$$scoreBonus = \$$bonus)")
                        total+=bonus
                    } else {
                        println "No idea: $worker - bonus anyway"
                        if (dobonus) svc.grantBonus(worker, complete_bonus as Double, assignment, "Bonus for attempting to play the Loom game despite bugs")
                        total+=complete_bonus
                    }


            }


        }
        println "Total outlay is $total"


    }

    public static converDBTime(Date d) {
        f2.parse(f1.format(d))
    }

    public static double cleanDouble(double d) {
        Double.parseDouble(df.format(d))
    }

    public static void main(String[] args) {
        new BonusLoomWorkers(args[0] as Integer, new File(args[1]),null,true)

    }


}
