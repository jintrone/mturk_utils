package edu.msu.mi.mturk_utils

import com.amazonaws.mturk.dataschema.QuestionFormAnswers
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType
import com.amazonaws.mturk.requester.SearchQualificationTypesResult
import com.amazonaws.mturk.service.axis.RequesterService
import groovy.util.logging.Log4j

import javax.print.attribute.standard.RequestingUserName

/**
 * Created by josh on 3/1/16.
 */

@Log4j
class Utils {

    static RequesterService getRequesterService() {
        FilePropertiesConfig config
        try {
            config = new FilePropertiesConfig(getClass().getResourceAsStream("/local.mturk.properties"));
            new RequesterService(config);
        } catch (IOException e) {
            log.error("Could not read global properties file: local.mturk.properties");
            //config = new ClientConfig();
            System.exit(-1)
        }


    }

    static String getQualificationId(String qualificationName, RequesterService svc) {
        try {

            SearchQualificationTypesResult result = svc.searchQualificationTypes(qualificationName, false, true, null, null, null, null)
            if (result.numResults != 1) {
                println("Could not identify a unique qualification type")
            } else {
                return result.getQualificationType(0).qualificationTypeId
                //println "Would assign ${result.getQualificationType(0).qualificationTypeId} to $workerid"
            }
        } catch (
                Exception e
                ) {
            e.printStackTrace()
        }
        return null
    }

    public static String extractAnswer(String answer,String key) {
        QuestionFormAnswers answers = RequesterService.parseAnswers(answer);
        for (QuestionFormAnswersType.AnswerType a:(List< QuestionFormAnswersType.AnswerType>) answers.getAnswer()) {
            if (a.getQuestionIdentifier().equals(key)) return a.getFreeText();
        }
        return null;
    }

    public static Map<String,String> extractAllAnswers(String answer) {
        QuestionFormAnswers answers = RequesterService.parseAnswers(answer);
        answers.answer.collectEntries { QuestionFormAnswersType.AnswerType a->
            [a.questionIdentifier,a.freeText]
        }
    }


}
