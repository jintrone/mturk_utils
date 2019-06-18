package scripts

import edu.msu.mi.mturk_utils.ManageQualifications
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 3/6/16.
 */
class RemoveCoderQualification {

    public static void main(String[] args) {
        new ManageQualifications("3ANVAKG92NJUMHX8IBCKN1EIZGTJ24", Utils.requesterService, false).fromCsv(new File(args[0]))


    }
}
