package edu.msu.mi.mturk_utils

import com.amazonaws.mturk.requester.CreateQualificationTypeRequest
import com.amazonaws.mturk.requester.QualificationType
import com.amazonaws.mturk.requester.QualificationTypeStatus
import com.amazonaws.mturk.service.axis.RequesterService
import groovy.util.logging.Log4j

/**
 * Created by josh on 1/27/16.
 */
@Log4j
class CreateQualification {



    RequesterService requesterService

    String questionForm
    String answerKey

    static {
        List.metaClass.collectWithIndex = {cls ->
            def i = 0;
            def arr = [];
            delegate.each{ obj ->
                arr << cls(obj,i++)
            }
            return arr
        }

    }

    public CreateQualification(RequesterService svc) {


    }

    def buildStandardCheckboxQuestionForm(String name, String description, String overview, List questions, List choices, List<List> answers) {
        questionForm = getQuestionPreamble(name,overview)+questions.collectWithIndex { x, idx-> buildCheckboxQuestion(idx,x,choices)}.join()+"\n</QuestionForm>"
        answerKey = getAnswerKeyPreamble()+answers.collectWithIndex {x, idx->buildCheckboxAnswer(idx,x)}.join("\n")+getAnswerKeyPostAmble(answers.size())
        this

    }

    def submit(String name, String desc, String keywords, int seconds=300) {
        if (questionForm && answerKey) {
            CreateQualificationTypeRequest request = new CreateQualificationTypeRequest();
            if (name != null)                 request.setName(name);
            if (answerKey != null)            request.setAnswerKey(answerKey);
            if (desc != null)          request.setDescription(desc);
            if (keywords != null)             request.setKeywords(keywords);
            request.setQualificationTypeStatus(QualificationTypeStatus.Active);
            request.setTest(questionForm);
            request.setTestDurationInSeconds(300);


            println request.toString()

            QualificationType type = requesterService.createQualificationType(name,keywords,desc,QualificationTypeStatus.Active,null,questionForm,answerKey,seconds,null,null)
            println type




        }
    }





    public static void main(String[] args) {
        String title = "Qualification Test for Classifying Post Content"
        String description = /Tests your understanding of and ability to apply a content analysis scheme. You will need to achieve a score of 92% or greater/

        CreateQualification cq = new CreateQualification(Utils.requesterService).buildStandardCheckboxQuestionForm(title,description,overview,questions,headings,answers)

        println cq.questionForm
        println cq.answerKey

        //cq.submit("SocialSupportCoding","content analysis, coding, qualification test","The qualification indicates that you have passed a test demonstrating you can reliably distinguish between the types of social support requested and provided in a social support forum",600)



    }


     public static String getQid(int qid) {
         "Q$qid"
     }

    public static String getAid(int aid) {
        "A$aid"
    }

     public static String buildSelection(int idx,String s) {
         """
<Selection>
      <SelectionIdentifier>${getAid(idx)}</SelectionIdentifier>
      <Text>${s}</Text>
</Selection>
"""
     }

    public static String buildCheckboxQuestion(int qid, String question, List headings) {
        return """\
<Question>
    <QuestionIdentifier>Q${qid}</QuestionIdentifier>
    <IsRequired>true</IsRequired>
    <QuestionContent>
      <Text>
        ${question}
      </Text>
    </QuestionContent>
    <AnswerSpecification>
      <SelectionAnswer>
        <StyleSuggestion>checkbox</StyleSuggestion>
        <Selections>
           ${headings.collectWithIndex {x,idx->buildSelection(idx,x)}.join()}
        </Selections>
      </SelectionAnswer>
    </AnswerSpecification>
  </Question>
"""
    }


    public static String buildCheckboxAnswer(int qid, List answers) {
        return """\
<Question>
    <QuestionIdentifier>${getQid(qid)}</QuestionIdentifier>
    <AnswerOption>
        ${answers.collect {
            "<SelectionIdentifier>${getAid(it)}</SelectionIdentifier>"
        }.join("\n")}
        <AnswerScore>1</AnswerScore>
    </AnswerOption>
  </Question>
"""
    }


    public static String getQuestionPreamble(String title, String text ) {
        return """\
<?xml version="1.0" encoding="UTF-8"?>
<QuestionForm xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd">
  <Overview>
    <Title>${title}</Title>
           ${text}
  </Overview>
"""
    }




    public static getAnswerKeyPreamble() {
        """\
<?xml version="1.0" encoding="UTF-8"?>
<AnswerKey xmlns="http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/AnswerKey.xsd">
"""
    }

   public static getAnswerKeyPostAmble(int num) {
        """<QualificationValueMapping>
    <PercentageMapping>
      <MaximumSummedScore>${num}</MaximumSummedScore>
    </PercentageMapping>
  </QualificationValueMapping>
</AnswerKey>"""
    }

    public static String overview = """\
<FormattedContent><![CDATA[
<p>Please read these instructions carefully. You will be asked to code posts taken from an online support message forum for the kind of social support they contain.  Some posts will provide or request multiple types of support. Some will not contain any support content. You must answer each question, and you must get at least 23/25 of the following correct to be granted this qualification.</p>
<h3>Support Types</h3>
<ol>
<li><b>Request Info Support</b> - The writer is trying to obtain to get advice, referrals, or knowledge about a disease. Note that tyring to find out more information about another poster (e.g. "Can you tell me more about your symptoms?") is <b>not</b> a request for informational support.</li>
<li><b>Provide Info Support</b> - Informational support messages provide advice, referrals or knowledge (related to the disease / condition in question). People sometimes provide informational support by relating something of their own experience.</li>
<li><b>Request Emo Support</b> - The writer is trying to get understanding, encouragement, affirmation, sympathy, or caring.  Requests may not be explicit; if a poster complains about being unhappy or in pain, it is likely that they are requesting information support.</li>
<li><b>Provide Emo Support</b> - Emotional support message provide understanding, encouragement, affirmation, sympathy or caring.</li>
<li><b>Community</b> - Community messages serve to strengthen a sense of community. They may include non-disease related chat, humor, and lighthearted banter. They may also emphasize group identity (e.g. "the people here are wonderful and will welcome you!"). Community support is distinct from emotional support which is focused on comfort and caring, but the two may occur together (e.g. "We are all pulling for you, and wish you the best of luck!!")</li>
</ol>
<p>
Please only code for support that is obvious and explicit - e.g. if "Hugs" is a common closing, it is not an expression of emotional support. If a post does not contain any identifiable support, please select "None".
</p>
<h3>Examples</h3>
<pre>I am trying to find a good Fibro Doc. in the northwest arkansas area. Please help.</pre>
<b>Support Types</b>: <i>Request Info Support</i> - the poster is requesting informational support related to his or her disease.
<br/>
<br/>
<pre>In order to really answer your question we are going to need some more information. You say you are  not
asthmatic...have you talked to your doctor about the 'attacks' you are getting? How  would you describe the
attacks...chest tightness, shortness of breath, etc etc? If you are having  symptoms of asthma attacks it could
be that you are indeed asthmatic but it hasn't been  diagnosed yet. High mold counts can indeed trigger asthma
attacks......particularly if you are  allergic to molds. You need to talk to your doctor about your symptoms
and get a good diagnosis.</pre>
<b>Support Types</b>: <i>Provides Info Support</i> - note that although the poster is providing informational support to someone else; they are also requesting info, but not informational support.
<br/>
<br/>
<pre>Hi, Congrats on finally getting it. I know the terrible feeling of feeling handicapped but you need it. I
"caved" in last year and haven't regretted it once. If you are at a point where you don't go places because you
can't walk from the parking lot (which sound like you have been for a while) then it is a shame not to get it.
If you are embarassed about it ever (which there is nothing to be embarassed about but.. you can always take it
down... ) Your work sounds accomodating and I hope that you can manage a slow transition back in. It is nice that
it is during the holiday week so you don't need to push for a whole week. I hope they work with you and don't
push you any more than you can do. I hope the side effect from the pred. will wear off soon and you will get back
to your self. Feel good.</pre>
<b>Support Types</b>: <i>Provides Emo Support</i> - the poster shares a little information, but this post is primarily designed to make the recipient feel better
<br/>
<br/>
<pre>Pretty impressive results, BetaTherapy. Never heard of Eleotin.</pre>
<b>Support Types</b>: <i>None</i> - without additional context, it doesn't seem that this poster is providing any specific kind of support.
<br/>
<br/>
<pre>good luck,al!! thinking of you! hugs,sara</pre>
<b>Support Types</b>: <i>Provides emo support</i> - this post is primarily about providing a sense of caring
and comfort.
<br/>
<br/>
<pre>You know how I love things that go BOOM, well in the South Pacific at an island called Tonga, they're
having a fairly major eruption - from under the sea, but it's making it all the way to the surface in a big
way. To see a video of the eruption, here's the link:
www.sciam.com/blog/60-second-science/post.cfm?id=tonga-volcano-spews-spectacular-plu-2009-03-19 Jim</pre>
<b>Support Types</b>: <i>Community</i> - the poster is providing a bit of off-topic chatter, which helps to
establish a sense of community.
<br/>
<br/>
<pre>Hi..........Where are you my dear ??? Are you ok ?? Please let me (us) know , I am concerned about you !!!
May the Lord's blessings be with you~~~ . ~Tess~</pre>
<b>Support Types</b>: <i>Provides Emo Support, Community</i> - the poster is asking after another's well-being, thus providing comfort and caring, and also expressing friendship and community interest in the recipient.
<br/>
<br/>
<pre>Anyone else out there just blah??? I am in pain even with taking meds My problem is that I was just thinking
to myself that I haven't had a mini personal pity party lately Woo hoo pity party!!! Doin' it!! I guess yesterday
with Doug's boating incident, I knew it would shoot me down. I will pick myself up and dust mysellf off tomorrow!!
It's a new day!! I am with heat and lovin' it!! I am working on my purses so today is not a total wash out. I am so
excited about how I am doing on them!!! Keep your chins up, it does get better, mentally and physically. I do have
to say that with my 3yr old I do not get to sit still too long. Brooklyn has been home all day and MAN this kid
is on me all the time!! On me and at me!!! I love it though!! My little love but with her twriling and craziness
Love you all like FMily YaYa</pre>
<em>Support Types</em>: <i>Request Emo Support, Community</i> - the poster is voicing pain and frustration, and so asking for some emotional support. At the same time, the poster is sharing personal details, helping to establish a sense of community.
]]></FormattedContent>"""

    public static List headings = ["Request info", "Provide info","Request emo","Provide emo","Community","None"]

    public static List questions = [
            """\
Another question: Does anyone else's asthma get super irritated if you were sitting still for a 
long period of time, and then get up and maybe, walk 100 feet and then you just can't breathe 
anymore? At what point do you go to the ER and tell them you can't breathe? I mean, I'm breathing, 
but it's not that easy and I've used all my inhaler doses for the day. Sorry to be so questioning,
I'm new to this whole thing...""",
            """\
My doctor did pulmonary function test including the methocholate study and says I don't have asthma
but rather bronchiectisis (sp?) - Advair and Symbicort both helped but eventually made me so hoarse
I couldn't talk. Now I'm using Foradil and it doesn't seem to be helping at all. Have tightness in
upper lungs and persistent upper resp. dry cough. No mucus at all. I can lay down and sleep okay but
when upright is nagging, chronic and driving me nuts. Anyone else in this boat -- what helps you?? Thanks.""",
            """\
Kristen- Be gentle here. We don't have the whole story. The national guidelines that I follow when prescribing
antibiotics do endorse treating cough with a negative chest xray under a variety of conditions, especily
in patients with asthma. I completely agree that there has been an excess in the use of antibiotics in the past,
but I don't want ChaCha here to worry unnecessarily. The provider that wrote for the drug has obviously looked
them in the eye and lung and we have not, they also have the whole story which we do not. Chacha-Hope that you
are feeling better. Have you noticed any improvement over the weekend? Drop a update when you get a chance.
""",
            """\
I noticed that I get weak, too. I never thought of breathing as hard work unless I'm really struggling, but
generally not breathing well is probably harder work than I realize. I don't eat much, either. The sleeping
problems most certainly adds to it because I'm so tired from not sleeping well at night! Albuterol makes me
so fidgety and I'm moving around (shaking my leg, etc) all day long...the calories that I do eat, I burn up
in Albuterol-induced-fidgeting. Fortunately, enough Albuterol seems to be getting me to a point where I'm
getting around and doing some stuff...I'm not just sitting on the couch browsing the web all day""",
            """\
Thanks so much for your response. I just got a call from the school nurse and had to pick him up. They ran the
timed mile in gym and it triggered an attack. He used the inhaler before the race and he still got an attack.
He even used it during running! This also triggered a migraine so he his now home from school! Great. I called
the doctor this a.m. telling them we need to get back in this week. The nurse called back and said she will
talk to the dr and get us in this week. This is so frustrating for my son. He is a high level swimmer and
runner and to see him not even be able to make it thru a race is horrible""",
            """\
I must go on record as saying I am so not a fan of HOT weather, and in the last few days we had a full blown
winter cold front blow thru which has been delightful. Dropped our temps from 100 to the 40's today and y
es even the mountains got some SNOW!! This is seasonably well below average but a lovely taste of fall has been
so nice. We had rain at our elevation (a rarity in the summer) and the air now smells like freshly washed
pine tree's!! FALL....glorious fall!!!!! Cooler damp air has slowed the progression of the closest fire,
but its still only 30 percent contained and they say it will likely burn in some places until winter really sets
in and wetter weather hits. So while everyone around is whining about how COLD It is I am doing a happy dance
and enjoying every minute!!! PS (Sue--Both Linda and Heather are picking on me and saying I am freak cuz I
Like this cold stuff...tell them to STOP) HE HE Vive Bene, Spesso L'Amore, Di Risata Molto (live well,
love much, laugh often)""",
            """\
Great job, Tom. Isn't it great to see your success? Keep up the good work.""",
            """\
Unintentional weight loss can be good and bad. My wife unintentionally lost 10 lbs. while not on food in the hospital
stay. Weak now, but happy about 10 lbs lighter. Not the way anyone should want to lose weight!""",
            """\
Whatever you think, Bonnie. DMH""",
            """\
Hello. It is very common for physical activity to raise blood glucose readings temporarily. You should wait 2
hours after playing soccer to test your blood sugar. That will give you a more accurate reading and should
result in a good reading. I have not heard of any activity that causes blood clots. My understanding it that
they are caused by inactivity such as a long plane trip or being inactive for any reason for extended periods
of time. I have a history of blood clots in my left leg, with part of the first traveling to my lung. That was
in 1997. My clots were caused by inactivity after a back injury plus genetic factors. You should discuss these
concerns with your doctor at your next appointment. Blessings, Dave
""",
            """\
Ain't it the truth. We often hear from people that the suspect that the primary job of an insurance company
nurse is to find ways and reasons to deny medication. Nothing could be further from the truth. Insurance
companies aren't stupid - and definitely understand the math. The math says that a diabetic on meds costs
them LESS than one who gets REAL sick because they fail to take meds - or eat right - or take advantage of
 wellness programs - or who hang up on nurses. Thank you for doing an extremely important job.""",
            """\
Thank you all very much for your support. I'm feeling well and hoping to start trying after July. We are
going to an Amusement park for my birthday so I'm going to wait until after then to try. It's true that a
lot of women have early miscarriages and never even know they are pregnant. I guess I am lucky that I knew
what was going on with my body! Best of luck to all of you! :lightsmile:""",
            """\
Hey Mikey, Enjoy your night out with Annette. Outback is great, I love the blooming onion! Have a great weekend! AL""",
            """
Hello Candy, Geez, you and hackwriter seem to have gotten a real groove going! But you know, Kim, you don't really
seem to be much of a hack when it comes to your writing skills. You have a way with words. You did a good job answering
all of Candy's questions. Dave""",
            """\
good luck bob!! thinking of you! hugs,sara""",
            """
Hi. I'm a 'newbie' but fell I"ve known you a long time! That sounds like a really tough month. The
sexual harassment charge is tough; and of course you b-in-law's situation. I hope and pray things take a turn for
the better soon! Hugs Camilla""",
            """\
You've chosen a great community for your first discussion board...Tho' we are sad to have to welcome anyone,
we will be most supportive and caring. We have all walked in your steps...The initial fears are only made worse by
the waiting! All the "what ifs"...all the terrible stoies we've ever heard...all the "what will I do abouts"
blanket your every thought! I described it as being totally overwhelming! What you are feeling is very understandable
and normal! It is rather idiotic to tell you not to worry, but I would advise that you try your best to distract
yourself during what will most likely be the longest weekend of your life! First things first! Please know that
a diagnosis of cancer is NOT a death warrant!!! Many people live full and long lives post diagnosis...Even those
whose cancer has metasticized and they are way beyond stage one! A lumpectomy is a rather straight forward
precedure...Done as an outpatient, an incision is made, and the tumor removed. Within a few hours you are on your
way home. It takes but a few days recovery before you return to normal activities. Regarding chemo or radiation,
it will depend on the type of cancer you have, whether it has spread and how aggressive it is. The pathology report
will answer much of this. You might also be sent for an Oncotype test which will help determine the necessity and
benefits of chemo. Radiation, on the other hand, is usually the next step. There are several types and methods.
Some take about 30 or so treatments...Very easy and they are so brief that you will spend the greater time driving
to and from the appointment than for the trxs! Another type is done within a week. Positioning is also something to
consider as most rad treatments are done with the patient on her back. There is, however, a face down method which
you will also want to discuss with your physician. Neither the surgery nor the rads will cause you to loose your hair.
Some, not all, chemo treatments will cause the loss of hair. HOWEVER...It grows back!!!! And you will be alive!!!
(Don't forget either!) You've a wide age range in your daughters so you will most likely approach telling each what
they and you feel is appropriate and necessary. It is important that they know you may not be up to doing quite so
much while under treatments, but it is also important that they not fear you will be leaving them in the near
future!! You will be amazed at how strong and how wise and how courageous you will be!! That is why we all consider
ourselves "WARRIORS"!! And you will lead your girls into following in their hero mother's footsteps! (Some of
those who have faced telling children will chime in and let you know how they handled it.) BUT....For now, it is
essential that you face the fact that for this moment you have not been diagnosed!!! and Monday you may well learn
that it is nothing you even have to concern yourself with! I promise you that is exactly what we are hoping for you!!!
So, hold tight, take it step by step, minute by minute, and know we are right beside you. Okay??? Please let us
know how you are doing. We care! Blessings. Rachael Just when the caterpillar thought her world was over,
she became a butterfly! Don't give up five minutes before the miracle!!""",
            """I am a bit upset right now, please bare with me, I am also in a LOT OF INCREASING PAIN IN MY FOOT!!
I have had this new problem for probably a month or more, I didnt get to ask my doc at last appt, cause family
members butted in and I didn't really get to ask him the things I had on my list. My memory is really bad and I
worked on the list over the months waiting for my appt. No insurance, so I try to get all I can in a 6 month appt
check-up. Actually this pain was really bothering me the day of the appt. It is the bottom of the foot on the heel
part. It is one "thing" that feels like there is a rock in the bottom of my foot when I walk on it!! At first it
was like 2 or 3 rocks in there, and it hurt.....BUT NOT like it does tonight! It went away and came back a few times.
This time it came this morning, and hurt, but just enough to be annoying again. As the day wore into eve it began
to cause me to avoid it by not putting weight on that heel when walking and standing......but it is the middle of
the night now, and I AINT walking-but it still hurts, actually radiating into my ankle and foot now, even throbbing!!
Now, I am not the kind to sit around and WONDER what is going on, so of course I GOOGLED IT. And I find that it is
either plantar faciitis and/or a heel spur!!!! Last year I was having plantar faciitis, and then it went away. The
reason I am upset is that this seems to be progressing, not getting better, so much so that I cannot walk on it at
all now. Just another PAINFUL problem! Does or has anyone here felt pain like a rock in your shoe on the heel part?
It is a teeny bit to the left of center.. Sorry, I just needed to vent this and whine about it to some people who
 possibly wont think I AM A FRIGGIN NUT!!""",
            """\
You had to bring those up, thats another thing I go and they have hurricanes...but then I never have been one to live
in fear..untill nowwwwwwww geeee thanks
""",
            """\
Hi This reflexologist is a mennonite doctor in the small town of Mcconnelsville. I am totally amazed at what he
accomplished today. As I said before I have been in the hospital 3 ER visits 1 ER visit to OSU and 2 neuro appts
no one could tell me what was wrong and what to do to stop the tremors. He fixed the tremors in 1 visit. If you are
interested in his name email me at --removed--@yahoo.com val
""",
            """\
Hugs to you Roz. Wish I had some good answers for you. Is there any chance of getting her into a care center?? I
don't know what you can possibly do when they are over 80....Not much...so yes, make sure you are taking care
of you. Cece
""",
            """\
Oh sure....leave me with the "F" word :sealed: Ummmm....lessee....what can I use where I won't get banned from
the board..... Ummm...Oh yeah, that Monster Movie from Israel...."FRANKENSCENCE"! (Relax Caprice ) :goofy:
""",
            """\
Dearest Julie~ You are in my thoughts too so much!! I pray you are doing ok.. I know this is such a hard time for
you right now.. So I am really hoping and praying you are doing ok.. I am here for you just remember that.. I will
be keeping your whole family in my thoughts and daily prayers.. Please take good extra care of YOU!!!!! Sending you
all my love and many soft teddy bear hugs to you... Miss you and will be waiting until you are better to come back..
We will always be here for you.... Love ya lots Julie!!!! Tara
""",
            """\
you really should tell your PCP about this, animal bites are nothing to play around with. I got what I thought was
a small bite, no biggie. I was trying to break up a dog fight while I was walking a dog. I did go to the emergency
walk in clinic, they cleaned it etc. the next morning I went back to the clinic, as the bandage had come off and
it was a hard one to rebandage. they did some more tests, and told me to get to the hospital immediately, they
were going to call ahead and tell them. I had to stay in the hospital for 3 days had some nerve damage that I
had to to a specialis so I could get surgery for, along with therapy. This really wasn't that big deal of a
bite. I've cut myself washing dishes worse than that. so, yep, go and have it checked out.
""",
            """\
Manoj, I disagree with you. If this person has a fasting of 106, indeed they may not need medication but who are you
to diagnose?!!! This sounds like a good wake up call and a good time to get a thourough check up to make sure there
are no other underlying problems and a referal to a dietician and possibly diabetes educator. I do agree to the point
they should be making changes in lifestyle and diet, assuming they have less than optimal habits at this time. Once,
again we do not have enough information or the proper licensing to diagnose! Unfortunately most people are not going
to make any dramatic changes in their lives based upon a faceless post on the internet.
""",
]

    public static List<List> answers = [[0],[0],[1,3],[1],[2],[4],[3],[1],[5],[1],[1],[4],[4],[4],[3],[3],[1,3,4],[0,2],[4],[1],[3],[4],[3,4],[1],[1]]



}
