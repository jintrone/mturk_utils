package edu.msu.mi.mturk_utils


import com.amazonaws.mturk.service.axis.RequesterService
import groovy.util.logging.Log4j
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

/**
 * Created by josh on 3/1/16.
 */

@Log4j
class Bonuser {

    RequesterService svc
    Closure check
    String reason
    boolean test
    //List ignore = ["A3RUUTINBKU8YE","A14HW8I4RYHNC4","A247FJ235YHG9J","A1M03U3W99BGZ4","A1CA46R2A6TV9W","A12VU5YSHGQH2V","A3MFK5UEUG95M3","A3PRZRK9IC5CBI","A2VRDE2FHCBMF8","A1EUBMQ86K32XE","A2HG1N3BVQO6I","ANZKTE853UXSD","A38DC3BG1ZCVZ2","A3HMDGRGC5JB7E","AZP4WEKYTLUAY","A2ZKUKB5FES9ZT","AC4LBMA5QJJ1I","ABMPX2Y2IRBMG","A2F93SIUUV5HQH","A1SL65Z68BK1UT","A37S96RT1P1IT2","A2VXIWQ7WRR962","ARLGZWN6W91WD","A1EQ2HDHF2WWSY","A2P065E9CYMYJL","A1VW8Y7XCV3DRW","APXNY64HXO08K","AHT8HA3JRWVGV","A2B6WQG0A9CKXQ","AV22FQTJNBUZT","AUSRHMZM3R6CW","A2TYLR23CHRULH","A1MJVTR0PCKBWW","A3QSFE6GKO157S","AFU00NU09CFXE","AEVU71Z2FDTUX","A2ZRF4I5RTKN7G","A2JCHN90PRUWDH","A15FXHC1CVNW31","A1AQK667NBERJ1","A2541C8MY0BYV3","A1UXWUM04RAO59","A2V3P1XE33NYC3","A610SH5RY1NG1","A1VNYP58BTF4HX","ARVXIBUCA8WDZ","A130X6CF795CQ5"]
    List ignore = []
    public Bonuser(RequesterService svc, Closure check, String reason = null, boolean test = true) {
        this.svc = svc
        this.check = check
        this.reason = reason
        this.test = test
    }

    /**
     * Presumes a csv results file
     * @param f
     */
    public void bonusFromCsvByAssignment(File f,String assignmentId="AssignmentId",String workerid="WorkerId") {
        CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.EXCEL.withHeader())
        parser.each { row ->
            double bonus = check(row.toMap())
            if (bonus > 0) {
                if (test) {
                    log.info("Would bonus: ${row.get(workerid)} \$${bonus} for ${reason}")
                } else {
                    log.info("Actually bonusing: ${row.get(workerid)} \$${bonus} for ${reason}")
                    svc.grantBonus(row.get(workerid), bonus, row.get(assignmentId), reason ?: "Because today is your lucky day!")
                }
            } else {
                log.info("NO bonus")
            }

        }


    }

    /**
     * Presumes a csv results file by HIT
     * @param f
     */
    public void bonusFromCsvByHIT(File f, boolean aggregate = false) {
        CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.EXCEL.withHeader());
        Map<String, List<Map>> byHit = [:]
        Map<String, Map> bonuses = [:]
        int count = 0
        int total = 0
        double amount = 0.0
        parser.each {
            if (byHit.containsKey(it.get("HITId"))) {
                byHit[it.get("HITId")] << it.toMap()
            } else {
                byHit[it.get("HITId")] = [it.toMap()]
            }
        }
        byHit.each { k,list ->
            log.info("HIT $k")
            List<Double> bonus = check(list)
            total+=bonus.size()
            list.eachWithIndex { Map entry, int i ->

                if (bonus[i]>0) {
                    amount+=bonus[i]
                    count++

                        log.info("Bonus ${entry.get("WorkerId")} ${bonus[i]} for ${entry.get("HITId")}")

                        if (aggregate) {
                            if (!bonuses.containsKey(entry.get("WorkerId"))) {
                                bonuses[entry.get("WorkerId")] = [[assignmentId:entry.get("AssignmentId"),bonus:bonus.get(i)]]
                            }
                            else {
                                bonuses[entry.get("WorkerId")] << [assignmentId:entry.get("AssignmentId"),bonus:bonus.get(i)]

                            }

                        } else {
                            //svc.grantBonus(entry.get("WorkerId"), bonus.get(i), entry.get("AssignmentId"), reason ?: "Because today is your lucky day!")
                        }

                } else {


                        log.info("Not bonusing ${entry.get("WorkerId")}")

                }

            }
        }
        double ototal = 0d
        if (aggregate) {

           //log.info(bonuses.entrySet().join("\n"))
            bonuses.each { k, v->
                if (!(k in ignore)) {
                    double tmp = 0d
                    //log.info("Bonus worker: ${k}")
                    float wtotal = Math.round(v.sum { it.bonus } * 100f) / 100f
                    log.info("Bonus worker ${k}: ${wtotal} using ${v.first().assignmentId}")
                    if (!test  && wtotal > 0.0f) {
                        svc.grantBonus(k, wtotal, v.first().assignmentId, reason ?: "Because today is your lucky day!")
                    }


                    ototal += wtotal
                } else {
                    log.info("Ignore $k")
                }



            }
        }
        log.info("Bonusing ${count} of ${total}, total=${amount}, ototal=${ototal}")

    }

}
