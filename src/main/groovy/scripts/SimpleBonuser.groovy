package scripts

import edu.msu.mi.mturk_utils.Bonuser
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 6/24/16.
 */
class SimpleBonuser {


   static String message = "Bonus for participating in early (buggy!) pilot of the Loom game.  Sorry for the delay, and thanks for the help!"

    public static void main(String[] args) {
        File f = new File(args[0])
        new Bonuser(Utils.requesterService,{Float.parseFloat(args[1])},message,false).bonusFromCsvByAssignment(f)



    }

}
