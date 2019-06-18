package scripts

import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.ManageQualifications
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 3/3/16.
 */
class AssignCoderQualification {

    public static void main(String[] args) {
        RequesterService svc = Utils.requesterService
        String qid = Utils.getQualificationId(args[0],svc)

        if (qid) {
            new ManageQualifications(qid, svc, true).fromCsv(new File(args[1]))
        }


    }
}
