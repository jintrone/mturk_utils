package scripts

import com.amazonaws.mturk.requester.Qualification
import com.amazonaws.mturk.requester.QualificationType
import com.amazonaws.mturk.requester.SearchQualificationTypesResult
import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils

/**
 * Created by josh on 6/12/16.
 */
class MessageWorkersWithQualification {


    public MessageWorkersWithQualification(List<String> withQual=[], List<String> withoutQual=[]) {

        RequesterService svc = Utils.requesterService

        String message1 =   "I will be creating a new Loom HIT in just a few moments! " +
                "It will require 50 people and you are eligible; please look for it and sign up as soon as you can. "+
                "If you encounter bugs, " +
                "please send me a note. You will know there is a problem if you get a red box around your story area. "+
                "If this happens, please try (not required though!) to include a screenshot, and (if you know how) any "+
                "bugs in the javascript console in the browser (don't worry if you don't know what I'm talking about). "+
                "Thanks! -- Josh"


        String message2 = "Thanks so much for putting up with this. If you got in or not, tried or didn't, you have my deep gratitude. "+
                            "This is important research, and I'm happy to tell anyone about it if you are interested. "+
                            "I'll try to be a good requester and will take care of you all - it will take me a little while to do this, but please let me know "+
                            "if I promised something and haven't made good on it.  The bonuses will take me a couple of days as that part is not automated yet."+
                            "I'm going to be out of the country for a week, but when I come back," +
                            "expect to see a lot more of these HITs.  Hopefully, with fewer kinks!  Thanks - Josh"




        String message4 = "I will be launching the next HIT in about 15 minutes.  The game is set for 50 people. "+
                "If you encounter serious problems and things have obviously gone wrong, please enter the word \"BUG\" for a confirmation code " +
                "and email me to describe the problem. If I need to communicate with you during the game, I will use this email address. Thanks - Josh."

        String message5 = "I am concerned that not enough people are online yet this morning; I will give this another ten minutes, and then will run a set of "+
                "qualification HITs.  Thanks for your patience."


        String message6 = "Ok, unfortunately the room is not filling up.  Please click \"stop waiting\" and I will relaunch at 11am EST.  Thanks - Josh."

        String message7 = "Also - please make sure to submit the confirmation code to get credit for waiting!  Thanks - Josh."

        String message8 = "There is a bug!  Please leave the game and return the HIT!  Sorry.  Thanks - Josh."

        String message9 = "Sorry for the difficulties and thanks for your patience.  You can submit the word \"BUG\" as a completion code to get credit. "+
                "I will repost the task later today or tomorrow.  Thanks - Josh."

        String message10 = "Thanks for your patience with these HITs. Some of you encountered problems, and" +
                " I've already spoken with many of you. I will be taking care of all of you "+
                " who weren't able to get credit for your work.  It will take me a day or so to get this together, "
                " but I will email you when I've posted a HIT for this purpose. Thanks - Josh."

        String message11="Hi - you're receiving this note because you are qualified to participate in an upcoming HIT (the Loom game). I am planning on " +
                "launching this HIT in the next half an hour or so.  You can search for my last name, or the word 'loom'." +
                "I will only need 50 people for each game this time around, and you will only be able to participate once today, but I may do several runs."+
                "I am gearing up to run" +
                "a large number of these games over the next several weeks, so please do not despair if you don't get into the game today.  Also please note," +
                "you may encounter bugs.  If so, get in touch and let me know what happened.  I do try to take care of the people that work on my HITs.  " +
                "I may be a little slow to respond today " +
                "(I'm in a meeting) but will get back to you before the day is out.  I should also be able to assign bonuses before the day is done.  Thanks -- Josh"

        String message12="I fear there is a bug in the system right now.  Please submit your HIT with the code BUG.  I will launch another HIT today.  Thanks for your patience."

        String message13="Hi - thanks for your patience.  The system is fine, but there weren't enough people to make up a game." +
                "I am in the process of qualifying another 100 people. I will launch the game again in about 30 minutes.  Please try again!"

        String launchNotice = "Hi - I've just launched a HIT for the LOOM game; we just need a few more players!  If you are receiving this message, you can play. Join if you can https://www.mturk.com/mturk/searchbar?selectedSearchType=hitgroups&searchWords=introne&minReward=0.00&x=0&y=0#"

        String cancelNotice = "Sorry. Not enough players seem to be available right now. I am going to cancel the batch; if you are in the waiting room, please click" +
                "stop waiting and submit the code so I can compensate you for your time."

        String scheduledLaunchNotice = "Hi - I'll be launching a LOOM game at 10am Eastern, today March 8. I will need 50 players!  Join if you can https://www.mturk.com/mturk/searchbar?selectedSearchType=hitgroups&searchWords=introne&minReward=0.00&x=0&y=0#"



        String notification = "Hi - you are receiving this because you are qualified to participate in an upcoming HIT!  I will be running another Story Loom HIT "+
                "within the next hour. This will require 50 people; bonuses for this HIT will be awarded by tomorrow.  You will be able to"+
                "find the HIT by searching for my name (Introne) or the word \"LOOM\". I will send another announcement immediately after I launch the HIT."+
                "Thanks! Josh."

        String repaymentMessage = "Hi - you're receiving this because I owe you money for a HIT you returned. I am about to post a batch of HITs that only"+
                " those of you with the proper qualification can see.  Please only accept one of these HITs!  Thanks - Josh."

        String wrongLink = "Hi - you're receiving this because I owe you money for a HIT you returned. I am about to post a batch of HITs that only"+
                " those of you with the proper qualification can see.  Please only accept one of these HITs!  Thanks - Josh."


        try {

            def ws = {String s->
                String qid = Utils.getQualificationId(s, svc)
                if (qid) {
                    svc.getAllQualificationsForQualificationType(qid).collect {
                        it.subjectId
                    }
                } else {
                    []
                }
            }



            Set<String> workers = (withQual.sum { ws(it) } as Set) - (withoutQual.sum { ws(it) } as Set)
            println "We have ${workers.size()} workers"
            while (!workers.isEmpty()) {
                List<String> w = workers.take(100)
                workers -= w
                svc.notifyWorkers("Story Loom Announcement",cancelNotice, w as String[])
            }
        } catch (Exception e) {
            e.printStackTrace()
        }

    }

    public static void main(String[] args) {
        new MessageWorkersWithQualification(["LoomQualification2"],["Story-E"]);
    }
}
