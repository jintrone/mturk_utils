package edu.msu.mi.mturk_utils

import com.amazonaws.mturk.service.axis.RequesterService
import com.amazonaws.mturk.service.exception.ObjectAlreadyExistsException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

import java.lang.ref.ReferenceQueue

/**
 * Created by josh on 3/3/16.
 */
class ManageQualifications {

    String qualificationId
    RequesterService svc
    boolean assign = true

    public ManageQualifications(String qualificationId, RequesterService svc, boolean assign = true) {
        this.qualificationId = qualificationId
        this.svc = svc
        this.assign = assign
    }

    public void fromCsv(File f, Closure check = { true }) {
        Set workers = [] as Set
        CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.EXCEL.withHeader());
        parser.each { row ->
            String workerId = row.get("WorkerId")

            if (check(row.toMap()) && !workers.contains(workerId)) {
                try {
                    if (assign) {

                        svc.assignQualification(qualificationId, workerId, 0, false)
                    } else {
                        svc.revokeQualification(qualificationId, workerId, null)
                    }
                } catch (ObjectAlreadyExistsException o) {


                }
                workers.add(workerId)
            }


        }

        println "${assign ? "Assigned" : "Removed"} $qualificationId to:\n$workers"

    }

    public static void main(String[] args) {
        RequesterService svc = Utils.requesterService
        new ManageQualifications(Utils.getQualificationId("NeedsPayment", svc), svc, true).fromCsv(new File(args[0]))
    }

}
