package scripts

import com.amazonaws.mturk.requester.Assignment
import com.amazonaws.mturk.requester.QualificationType
import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.MonitorAndValidate
import edu.msu.mi.mturk_utils.Utils
import groovy.sql.Sql
import groovy.util.logging.Log4j

import java.sql.Timestamp

/**
 * Created by josh on 3/2/16.
 */
@Log4j
class ValidateConsistency {

    String qualificationType = "3HN7640Z3OWH5JABOK5UGPEVVF4TSD"
    //String qualificationType ="3J9ZK359J5FCCIDNWMJZA0I16KR314"
    Sql connection = getSqlConnection()
    int minCount = 25
    float minRate = 0.80f


    public void run(String pattern) {
        RequesterService svc = Utils.requesterService

        MonitorAndValidate mnv = new MonitorAndValidate(svc, pattern)
        def stddev = { l ->
            float m = l.sum()/l.size()
            (l.sum{(it-m)**2}/(l.size()-1))**0.5
        }
        mnv.start { Assignment[] asses ->
            Map m = asses.collectEntries {
                [it.workerId, (Utils.extractAnswer(it.answer,"storyRating")?:0) as int]
            }

            float sd = stddev(m.values())
            float mean = m.values().sum()/m.size()
            m.each { k,v ->
                updateWorker(k, Math.abs(v-mean) <= sd)
            }
            retireWorkers().each { String workerId->
                svc.assignQualification(qualificationType,workerId,null,false)
            }

            [[approve:true,message:"Thanks for helping"]]*asses.size()
        }
    }

    public void updateWorker(String workerid, boolean b) {
        String is = "insert into worker_history (`workerId`,`date`,`count`,`acceptable`) values (:id, :d , 1 , :inc) "+
                "on duplicate key update count=count+1,date=:d,acceptable=acceptable+:inc"
        connection.executeInsert(is,[id:workerid,d:new Date(), inc:b?1:0])
    }

    public List<String> retireWorkers() {
        List<String> result = connection.rows("select workerId from worker_history where enabled = 1 and `count` > ? and `acceptable`/`count` < ?" as String,minCount, minRate).collect {
            it.workerId
        }
        //ignore jason p
        result-= "A15FXHC1CVNW31"
        if (!result.empty) {
            log.info("Retire workers ${result}")
            connection.executeUpdate("update worker_history set enabled = 0 where workerId in (${result.collect { "'$it'" }.join(",")})" as String)
        }
        result

    }

    public static Sql getSqlConnection() {
        Sql.newInstance("jdbc:mysql://localhost:3306/mturk_support", "mturk", "mturk", 'com.mysql.jdbc.Driver')
    }

    public static void main(String[] args) {
        new ValidateConsistency().run(/.+NARR2.+/)
    }


}
