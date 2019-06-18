package scripts

import edu.msu.mi.mturk_utils.Bonuser
import edu.msu.mi.mturk_utils.Utils
import groovy.util.logging.Log4j

/**
 * Created by josh on 3/1/16.
 */
@Log4j
public class BonusCoderTest {


    static {
        List.metaClass.multParallel = { list ->
            list.eachWithIndex { Object entry, int i ->
                delegate[i] *= entry
            }
        }

    }

    public static void main(String[] args) {
        def stddev = { l ->
            float m = l.sum()/l.size()
            (l.sum{(it-m)**2}/(l.size()-1))**0.5
        }
        new Bonuser(Utils.requesterService, {entries ->

            List ranks = entries.collect {it["Answer.storyRating"] as Integer}
            //;println ranks
            float m = ranks.sum() / ranks.size()
            float s = stddev(ranks)
            ranks.collect {
                Math.max(0,(1 - Math.abs(it - m) / (!s?1:s)))*0.05
            }



        }, "Aggregated bonus as promised for accurate coding; thanks for your help and patience!",false).bonusFromCsvByHIT(new File(args[0]),true)
    }


}