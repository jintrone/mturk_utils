package scripts

import com.amazonaws.mturk.service.axis.RequesterService
import edu.msu.mi.mturk_utils.Utils
import groovy.util.logging.Log4j
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

/**
 * Created by josh on 6/12/16.
 */
@Log4j
class MessageWorkersFromCsvNardiff {


    public MessageWorkersFromCsvNardiff(File f) {

        RequesterService svc = Utils.requesterService
//        String message = "Thanks for participating in the 'Read a story' HIT last week.  Unfortunately, you may have noticed " +
//                "we didn't have a field for 'White / Caucasian' in the survey; without this, we won't be able publish our study " +
//                "in the journal we are targeting. Could you please take a few seconds to confirm your race for us?\n\n"
//                "I've put together a very simple form (https://goo.gl/htmc84 - please include your mturk id!) or you could just " +
//                "reply to this email and let me know if you're " +
//                "white or something else.\n\n" +
//                "Thanks so much for all your help,\n"
//                "Josh"

        String accept = "Congrats!  I've reviewed your submission on the qualifier and you got all of the questions right! I'll assign the "+
                "qualification soon.  Please note that I have close to 4000 HITs (estimated), and I'll need seven "+
                "people per hit.  Currently, about 10 people passed the qualifier, so there's a good chance you could make up to \$1000 "+
                "total, but it's going to be a lot of work - possibly about 100 hours.  Could you please email me and let me know if you're up for that? "+
                "If it seems like that's not going to work out, I'll go ahead and try to qualify more people.  \n\nThanks!\nJosh"

        String borderline = "Hi - I've reviewed your submission on the qualifier and you only missed one question. I'd still like to give you an "+
                "opportunity to qualify, but I want to review your mistake with you over email.  So, if you're still interested in participating, "+
                "please shoot me an email, and we can go over your mistake. \n\nThanks!\nJosh"

        String declined = "Hi - I've reviewed your submission on the qualifier, and regret to inform you that your performance was not good enough for me "+
                "to invite you to the upcoming batch.  If you are interested in what you got wrong, please get in touch! And note, I welcome your participation "+
                "in future HITs I post - this one requires a particular mode of analysis that probably doesn't have much to do with your general abilities. I do "+
                "appreciate your efforts.  \n\nThanks!\nJosh"
        //message = message?:"Whoops!  Looks like I forgot to add a message, Josh."

        CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.DEFAULT.withHeader())

        List waccepted = []
        List wdeclined = []
        List wborderline = []

        parser.each { row ->
            switch (row.message) {
                case "1":
                    waccepted<<row.workerid
                    break
                case "2":
                    wborderline<<row.workerid
                    break
                case "3":
                    wdeclined<<row.workerid

            }
        }

        try {

            log.info("${waccepted.size()} accepted")
            log.info("${wborderline.size()} borderline")
            log.info("${wdeclined.size()} declined")
            svc.notifyWorkers("Qualifer results",accept,waccepted as String[])
            svc.notifyWorkers("Qualifer results",declined,wdeclined as String[])
            svc.notifyWorkers("Qualifer results",borderline,wborderline as String[])


        } catch (Exception e) {
            e.printStackTrace()
        }

    }

    public static void main(String[] args) {
        String message1 = "Hi - you recently worked on / contacted me about a HIT (the story matching task) and I'm writing to invite you "+
                "to be part of a small group of MTurk workers to work "+
                "on a large batch of these.  I have about 4000 HITs that require 7 people each, and each assignment pays .25 (plus a performance bonus). I'm only "+
                "inviting about 20 of you here, so there's a good chance you can make a good bit of money. \n\nHowever, I need the performance on these HITs to be "+
                "a little higher than on the previous batch. To that end, I've set up a "+
                "qualifier task - you will only get one shot at the qualifier. I will post it soon, and will make sure that only those of you who receive this email will find the qualifier."+
                "The qualifier will be called Nardiff Qualifier and it shouldn't take you more than 5 minutes. I'll leave the qualifier up for a couple of days, but I'm "+
                "hoping we can begin the actual batch later this week (Friday).  If you're not interested in any of this, you can simply ignore it! "+
                "Many thanks for all your hard work.  \n\nBest,\nJosh."

        String message = "I have launched the qualifier HIT; it will expire in 3 days.  The HIT is titled Nardiff Qualifier.\n\nBest,\nJosh."
        new MessageWorkersFromCsvNardiff(new File(args[0]))
    }
}
