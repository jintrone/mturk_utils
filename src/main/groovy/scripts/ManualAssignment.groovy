package scripts

import com.amazonaws.mturk.requester.SearchQualificationTypesResult
import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 6/10/16.
 */


public class ManualAssignment {




    public ManualAssignment(String workerid,String qualificationName) {

        RequesterService svc = Utils.requesterService

        try {

            SearchQualificationTypesResult result = svc.searchQualificationTypes(qualificationName, false, true, null, null, null, null)
            if (result.numResults != 1) {
                println("Could not identify a unique qualification type")
            } else {
                svc.assignQualification(result.getQualificationType(0).qualificationTypeId, workerid, 0, true)
                //println "Would assign ${result.getQualificationType(0).qualificationTypeId} to $workerid"
            }
        } catch (
                Exception e
                ) {
            e.printStackTrace()
        }
    }


    public static void main(String[] args) {
        def payment = ["A15FXHC1CVNW31"]
        payment.each {
            new ManualAssignment(it, "BetaTester")
        }
    }

}


