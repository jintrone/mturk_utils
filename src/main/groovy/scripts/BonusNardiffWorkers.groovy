package scripts

import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Bonuser
import edu.msu.mi.mturk_utils.Utils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

/**
 * Created by josh on 10/24/17.
 */
class BonusNardiffWorkers {


    public static void main(String[] args) {

        new Bonuser(Utils.requesterService, { Map m ->
            Math.round((m["value"] as float) * 100f) / 100f
        },
                "This is a bonus for your performance on the story matching task (last ruth batch).  So sorry for " +
                        "delay on this!  I've been absolutely swamped. Thank you, again, for all the great work!",
                false).bonusFromCsvByAssignment(new File(args[0]), "assignment", "worker")


    }
}
