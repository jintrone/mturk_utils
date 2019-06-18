package edu.msu.mi.mturk_utils

import com.amazonaws.mturk.requester.Assignment
import com.amazonaws.mturk.requester.AssignmentStatus
import com.amazonaws.mturk.requester.HIT
import com.amazonaws.mturk.requester.HITStatus
import com.amazonaws.mturk.service.axis.RequesterService
import groovy.util.logging.Log4j
import org.xml.sax.SAXException


/**
 * Created by josh on 3/1/16.
 */
@Log4j
class MonitorAndValidate {


    RequesterService svc
    String titlePattern
    String hitTypeId
    long delay = 30000
    boolean running
    Timer t = new Timer()
    Set badHits = [] as Set

    public MonitorAndValidate(RequesterService svc, String titlePattern) {
        this.svc = svc
        this.titlePattern = titlePattern

    }

    public void start(Closure check) {
        running = true
        set(check)
    }

    public void set(Closure check) {
        t.schedule(new TimerTask() {
            @Override
            void run() {

                try {
                    checkCompleteHit check
                } catch (Throwable t) {
                    t.printStackTrace()
                }
                if (running) {
                    set(check)
                }
            }
        }, delay)
    }


    private void checkCompleteHit(Closure check) {
        def hits = getHits()
        if (!hits.empty) {
            log.info("Got ${hits.size()} newly completed hits")
        }
        hits.each {
            Assignment[] a = svc.getAllAssignmentsForHIT(it.getHITId())

            def approvals = check(a)
            a.eachWithIndex { Assignment entry, int i ->
                if (entry.getAssignmentStatus() == AssignmentStatus.Submitted) {
                    if (approvals.get(i).approve) {
                        svc.approveAssignment(entry.assignmentId, approvals.get(i).message)
                    } else {
                        svc.rejectAssignment(entry.assignmentId, approvals.get(i).message)
                    }
                }
            }
            //NOTE: This line has a problem if it is not in the "reviewable" state
            svc.setHITAsReviewing(it.getHITId())

        }


    }

    public List<HIT> getHits() {
        List<HIT> result

        if (!hitTypeId) {
            result = svc.searchAllHITs()
            result.retainAll { hit ->
                if (hit.getHITId() in badHits) {
                    return false
                } else {
                    try {
                        //HIT h = svc.getHIT(hit.getHITId(),)
                        String title =  hit.title
                        boolean match = title.matches(titlePattern)
                        if (match && !hitTypeId) {
                            log.info("Set hit type")
                            hitTypeId = hit.getHITTypeId()
                        }
                        //log.info("reviewStatus: ${hit.getHITReviewStatus()} status: ${hit.getHITStatus()} title: $title matches: $match")
                        return hit.getHITStatus()==HITStatus.Reviewable && match


                    } catch (Exception e) {
                        log.error("Problem parsing HIT ${hit.getHITId()}")
                        badHits << hit.getHITId()
                        return false
                    }
                }
            }

        } else {
            result = svc.getAllReviewableHITs(hitTypeId)
            log.info("Got ${result.size()} hits to review")
        }
        result
    }
}
