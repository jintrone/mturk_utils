package scripts

import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

/**
 * Created by josh on 6/12/16.
 */
class MessageWorkersFromCsv {


    public MessageWorkersFromCsv(File f,String message, String subject) {

        RequesterService svc = Utils.requesterService
//        String message = "Thanks for participating in the 'Read a story' HIT last week.  Unfortunately, you may have noticed " +
//                "we didn't have a field for 'White / Caucasian' in the survey; without this, we won't be able publish our study " +
//                "in the journal we are targeting. Could you please take a few seconds to confirm your race for us?\n\n"
//                "I've put together a very simple form (https://goo.gl/htmc84 - please include your mturk id!) or you could just " +
//                "reply to this email and let me know if you're " +
//                "white or something else.\n\n" +
//                "Thanks so much for all your help,\n"
//                "Josh"

        if (message == null) {
            throw new RuntimeException("Add a message, dumbass")
        }
        //message = message?:"Whoops!  Looks like I forgot to add a message, Josh."

        CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.DEFAULT.withHeader())
        List<String> workers = []
        parser.each { row ->
            workers << row.WorkerId
        }

        try {

            println "We have ${workers.size()} workers"
            while (!workers.isEmpty()) {
                List<String> w = workers.take(100)
                try {
                    println("${workers.size()} left")
                    svc.notifyWorkers(subject, message, w as String[])
                    workers -= w
                } catch (Exception e) {
                    e.printStackTrace()
                    Thread.sleep(500)

                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }

    }

    public static void main(String[] args) {
        String message1 = "Hi - thanks for trying the LKT qualifier.  You did a great job!  I'll be assigning a qualification soon, and will then launch the batch. " +
                "I've set up a Slack channel to communicate about the batch; I find that can be a *lot* easier than email. You don't have to join, but it's" +
                "probably a good idea.  I'll post more info about the batch there, and you can ask me questions. " +
                "Here's the invite link: https://tinyurl.com/yc8deatl. \n\nBest,\nJosh."

        String message2 = "Hi again!  The LKT batch is now up - only those of you with the qualifier can see it. There are about 600 HITs - I anticipate each one will take a bit under" +
                " a minute, and have set the pay rate to get you \$10-\$11/hr.  There is a bonus for good performance, which I determine by comparing you to others who work on a HIT." +
                " If everyone gets the same answer, everyone wins! (max .03 per hit).\n\n Again, I find Slack to be a great way to communicate on these batches, so here's that invite" +
                " link one more time: https://tinyurl.com/yc8deatl. You can also always email me. \n\nBest,\nJosh."

        //String message = "I have launched the qualifier HIT; it will expire in 3 days.  The HIT is titled Nardiff Qualifier.\n\nBest,\nJosh."
        new MessageWorkersFromCsv(new File(args[0]),message2,"LKT batch is up!")
    }
}
