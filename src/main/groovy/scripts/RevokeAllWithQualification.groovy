package scripts

import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 3/5/17.
 */


public class RevokeAllWithQualification {

    public RevokeAllWithQualification(String qualification, String reason) {

        RequesterService svc = Utils.requesterService

        try {

            String qid = Utils.getQualificationId(qualification, svc)
            def ws = {String s->
                if (qid) {
                    svc.getAllQualificationsForQualificationType(qid).collect {
                        it.subjectId
                    }
                } else {
                    []
                }
            }



            Set<String> workers = ws(qualification)
            println "We have ${workers.size()} workers"
            workers.each {
                println "Revoked $qid from $it"
                svc.revokeQualification(qid, it, reason)
            }

        } catch (Exception e) {
            e.printStackTrace()
        }
    }


    public static void main(String[] args) {
        new RevokeAllWithQualification(args[0],args[1])
    }


}