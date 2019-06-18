package scripts

import com.amazonaws.mturk.requester.SearchQualificationTypesResult
import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 6/10/16.
 */


public class ManualRemoval {




    public ManualRemoval(String workerid, String qualificationName) {

        RequesterService svc = Utils.requesterService

        try {

            SearchQualificationTypesResult result = svc.searchQualificationTypes(qualificationName, false, true, null, null, null, null)
            if (result.numResults != 1) {
                println("Could not identify a unique qualification type")
            } else {
                svc.revokeQualification(result.getQualificationType(0).qualificationTypeId, workerid, "Already paid")
                //println "Would assign ${result.getQualificationType(0).qualificationTypeId} to $workerid"
            }
        } catch (
                Exception e
                ) {
            e.printStackTrace()
        }
    }


    public static void main(String[] args) {
        def payment = ["ABMX8XUNPR3LP","A2EI075XZT9Y2S","A3QSFE6GKO157S"]
        payment.each {
            new ManualRemoval(it, "NeedsPayment")
        }
    }

}


