package com.example.data.ai

import com.example.data.model.ChatUiMessage
import com.example.data.model.DefinitionItem
import com.example.data.model.GeneratedNotes
import com.example.data.model.McqQuestion
import com.example.data.model.McqRequest
import com.example.data.model.NotesRequest
import com.example.data.model.QnAItem
import com.example.data.model.QnARequest
import com.example.data.model.QnAType
import com.example.data.model.StudyLanguage
import com.example.data.model.TableData
import kotlinx.coroutines.delay

/**
 * Placeholder implementation of [AiRepository] used when Firebase AI Logic is not provisioned.
 * Produces structured, clearly labeled sample study material tailored to Indian school and
 * competitive exams across English, Hindi, and Hinglish.
 */
class PlaceholderAiRepository : AiRepository {
    override val isLiveAiEnabled: Boolean = false

    override suspend fun generateNotes(request: NotesRequest): Result<GeneratedNotes> {
        val topic = request.topic.trim()
        if (topic.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter a valid chapter or topic."))
        }
        delay(650L)

        val notes = when (request.language) {
            StudyLanguage.HINDI -> buildHindiSampleNotes(request, topic)
            StudyLanguage.HINGLISH -> buildHinglishSampleNotes(request, topic)
            StudyLanguage.ENGLISH -> buildEnglishSampleNotes(request, topic)
        }
        return Result.success(notes)
    }

    override suspend fun generateMcqs(request: McqRequest): Result<List<McqQuestion>> {
        val topic = request.topic.trim()
        if (topic.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter a chapter or topic for MCQs."))
        }
        delay(550L)

        val targetCount = request.count.coerceIn(5, 100)
        val templates = when (request.language) {
            StudyLanguage.HINDI -> hindiMcqTemplates(topic, request.subject, request.exam, request.classLevel)
            StudyLanguage.HINGLISH -> hinglishMcqTemplates(topic, request.subject, request.exam, request.classLevel)
            StudyLanguage.ENGLISH -> englishMcqTemplates(topic, request.subject, request.exam, request.classLevel)
        }

        val questions = (1..targetCount).map { number ->
            val template = templates[(number - 1) % templates.size]
            val setLabel = if (number <= templates.size) "" else " [Set ${(number - 1) / templates.size + 1}]"
            McqQuestion(
                id = number,
                question = "${template.question}$setLabel",
                options = template.options,
                correctIndex = template.correctIndex,
                explanation = "[Sample AI Preview] ${template.explanation}",
                examTag = "${request.exam} • ${request.subject}"
            )
        }
        return Result.success(questions)
    }

    override suspend fun generateQnA(request: QnARequest): Result<List<QnAItem>> {
        val topic = request.topic.trim()
        if (topic.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter a chapter or topic for Q&A."))
        }
        delay(550L)

        val types = if (request.selectedTypes.isEmpty()) QnAType.entries else request.selectedTypes.toList()
        val items = mutableListOf<QnAItem>()
        var nextId = 1

        types.forEach { qType ->
            items.add(buildSampleQnAItem(nextId++, qType, request, topic, variant = 1))
            items.add(buildSampleQnAItem(nextId++, qType, request, topic, variant = 2))
        }
        return Result.success(items)
    }

    override suspend fun sendChatMessage(
        history: List<ChatUiMessage>,
        userMessage: String,
        examContext: String,
        language: StudyLanguage
    ): Result<String> {
        val cleanMsg = userMessage.trim()
        if (cleanMsg.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please type a study question."))
        }
        delay(450L)

        val reply = when (language) {
            StudyLanguage.HINDI -> """
                [सैंपल AI उत्तर — लाइव Gemini के लिए Firebase AI Logic आवश्यक है]
                
                प्रश्न: "$cleanMsg" ($examContext संदर्भ)
                
                1. मुख्य अवधारणा: इस विषय को समझने के लिए पहले NCERT मूल सिद्धांतों और पिछली परीक्षा के पैटर्न को जोड़कर पढ़ें।
                2. परीक्षा रणनीति: $examContext में इस टॉपिक से तथ्यात्मक और विश्लेषणात्मक दोनों प्रकार के प्रश्न पूछे जाते हैं।
                3. त्वरित रिवीजन टिप: मुख्य परिभाषा, 3 प्रमुख बिंदु और 1 वास्तविक उदाहरण अपनी शॉर्ट नोटबुक में लिखें।
                
                क्या आप इस विषय पर 10 अभ्यास MCQ या शॉर्ट नोट्स बनाना चाहते हैं?
            """.trimIndent()

            StudyLanguage.HINGLISH -> """
                [Sample AI Preview — Live Gemini ke liye Firebase AI Logic enable karein]
                
                Aapka Doubt: "$cleanMsg" ($examContext Prep)
                
                • Core Concept: Is topic ko sabse pehle basic definition aur real-life example ke saath connect karein.
                • Exam Focus ($examContext): Previous year papers mein yahan se direct concept + application-based questions aate hain.
                • Smart Memory Trick: 3 key keywords note karein aur 24 hours ke andar ek baar quick revise zaroor karein.
                
                Aap AI Notes Generator ya MCQ Practice Test use karke is chapter ko aur strong kar sakte hain!
            """.trimIndent()

            StudyLanguage.ENGLISH -> """
                [Sample AI Preview — Enable Firebase AI Logic for live Gemini responses]
                
                Study Guidance for "$cleanMsg" ($examContext):
                
                1. Core Foundation: Break this concept into definition, mechanism/process, and real-world Indian syllabus examples (NCERT-aligned).
                2. High-Yield Exam Angle: For $examContext, focus on conceptual clarity, key dates/formulas/articles, and common distractor traps in objective questions.
                3. 3-Step Revision Plan:
                   • Read the summary points actively.
                   • Write one concise 50-word explanation in your own words.
                   • Attempt at least 10 timed MCQs on this topic.
                
                Tip: Switch to the AI Studio tab to generate full notes or a timed practice test on this topic.
            """.trimIndent()
        }
        return Result.success(reply)
    }

    override suspend fun generateDailyGkQuiz(language: StudyLanguage): Result<List<McqQuestion>> {
        delay(350L)
        return Result.success(
            if (language == StudyLanguage.HINDI) dailyGkHindiQuestions() else dailyGkEnglishQuestions()
        )
    }

    override suspend fun generateChapterStudyPack(
        request: com.example.data.model.ChapterQnARequest
    ): Result<com.example.data.model.ChapterStudyPack> {
        if (request.chapterName.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter a chapter name."))
        }
        delay(550L)
        return Result.success(ChapterPackGenerator.buildPack(request))
    }

    override suspend fun generateMoreChapterQuestions(
        existingPack: com.example.data.model.ChapterStudyPack,
        additionalCount: Int
    ): Result<List<com.example.data.model.ObjectiveQuestionItem>> {
        delay(350L)
        return Result.success(ChapterPackGenerator.generateMoreObjectives(existingPack, additionalCount))
    }

    private fun buildEnglishSampleNotes(req: NotesRequest, topic: String): GeneratedNotes {
        return GeneratedNotes(
            title = "$topic — ${req.notesType.label}",
            classLevel = req.classLevel,
            exam = req.exam,
            subject = req.subject,
            topic = topic,
            language = req.language,
            notesType = req.notesType,
            detailedNotes = """
                [Sample AI Preview — Enable Firebase AI Logic for live Gemini-generated notes]
                
                $topic is a cornerstone chapter in ${req.subject} for ${req.classLevel} students and ${req.exam} aspirants. Understanding its structure requires linking foundational NCERT concepts with analytical exam applications.
                
                1. Overview & Significance:
                $topic explains how underlying principles govern real-world phenomena and institutional or scientific systems. Examiners in ${req.exam} frequently frame assertion-reason and case-based questions from this unit.
                
                2. Core Framework & Components:
                • Primary Mechanism: Establishes the cause-and-effect relationship central to $topic.
                • Structural Classification: Divides the concept into primary, secondary, and applied dimensions for crisp recall.
                • Inter-topic Linkage: Connects directly with adjacent chapters in ${req.subject}, helping eliminate wrong options in competitive tests.
                
                3. Analytical Perspective for ${req.exam}:
                Always support written answers with labeled diagrams, chronological anchors, or formula derivations where applicable.
            """.trimIndent(),
            shortNotes = listOf(
                "$topic forms a high-weightage segment in ${req.subject} (${req.exam}).",
                "Core principle connects foundational theory with practical exam-oriented applications.",
                "Memorize standard definitions, key classifications, and exception cases.",
                "Revise comparison tables before attempting objective or descriptive papers.",
                "Frequently tested via 1-mark MCQs, 3-mark short notes, and 5-mark structured answers."
            ),
            easyExplanation = "Think of $topic like a well-organized railway junction: every core rule acts as a track directing information or processes cleanly toward the right outcome. Once you remember the 3 fundamental rules behind $topic, even tricky ${req.exam} questions become easy to solve step by step.",
            definitions = listOf(
                DefinitionItem(
                    term = "Core Concept of $topic",
                    definition = "The fundamental principle in ${req.subject} that defines how elements within $topic interact and produce measurable outcomes."
                ),
                DefinitionItem(
                    term = "Primary Classification",
                    definition = "Systematic grouping of concepts in $topic based on structure, function, or historical/scientific criteria."
                ),
                DefinitionItem(
                    term = "Applied Principle",
                    definition = "Direct application of $topic rules to solve numerical, analytical, or policy-oriented problems in ${req.exam}."
                )
            ),
            importantFacts = listOf(
                "Consistently carries 6%–10% weightage in ${req.exam} and ${req.classLevel} board/screening tests.",
                "NCERT textbook summary boxes and highlighted keywords are the #1 source of direct objective questions.",
                "Pay special attention to exceptions and contrasting pairs—examiners routinely test these.",
                "Visual flowcharts improve recall speed by over 40% during last-minute revision."
            ),
            comparisonTable = TableData(
                title = "Key Comparison Matrix — $topic",
                headers = listOf("Dimension", "Foundational Aspect", "Exam Application (${req.exam})"),
                rows = listOf(
                    listOf("Core Focus", "Definitions & basic laws", "Direct 1-mark & assertion MCQs"),
                    listOf("Process / Flow", "Sequential cause-effect steps", "Diagram & flowchart questions"),
                    listOf("Exceptions", "Boundary cases & special rules", "Tricky elimination options"),
                    listOf("Revision Aid", "One-line summary formulas", "Last-day rapid recall sheet")
                )
            ),
            flowchartSteps = listOf(
                "Step 1: Identify core definition & scope of $topic",
                "Step 2: Break down primary components & governing rules",
                "Step 3: Trace cause → mechanism → observable outcome",
                "Step 4: Map real-world Indian curriculum examples",
                "Step 5: Verify against ${req.exam} Previous Year Questions"
            ),
            examples = listOf(
                "Textbook Example: Standard NCERT illustration demonstrating $topic in ${req.subject}.",
                "Everyday Application: Practical scenario showing how $topic operates in daily life or Indian governance/science.",
                "Exam Model Case: Sample multi-statement question integrating $topic with related ${req.subject} principles."
            ),
            examOrientedPoints = listOf(
                "Underline keywords and technical terms in descriptive ${req.classLevel} answers.",
                "For ${req.exam} prelims/tier-1, watch for extreme words like 'only', 'never', or 'always'.",
                "Draw a neat 4-box flowchart whenever a 5-mark question asks to 'Explain the process of $topic'.",
                "Practice at least 20 MCQs immediately after finishing these notes."
            ),
            quickRevision = listOf(
                "Topic: $topic (${req.subject} • ${req.classLevel} • ${req.exam})",
                "3 Must-Know Pillars: Definition + Classification + Key Exceptions.",
                "Golden Rule: Revise tables & flowchart once tonight and once after 7 days.",
                "Exam Readiness Check: Can you summarize $topic in 5 crisp bullet points without looking?"
            ),
            isSamplePreview = true
        )
    }

    private fun buildHindiSampleNotes(req: NotesRequest, topic: String): GeneratedNotes {
        return GeneratedNotes(
            title = "$topic — ${req.notesType.label} (हिन्दी)",
            classLevel = req.classLevel,
            exam = req.exam,
            subject = req.subject,
            topic = topic,
            language = req.language,
            notesType = req.notesType,
            detailedNotes = """
                [सैंपल AI प्रीव्यू — लाइव Gemini नोट्स के लिए Firebase AI Logic सक्रिय करें]
                
                '$topic' विषय ${req.subject} (${req.classLevel}) तथा ${req.exam} परीक्षा के लिए अत्यंत महत्वपूर्ण अध्याय है। इसमें मूल सिद्धांतों, वर्गीकरण और व्यावहारिक अनुप्रयोगों का गहराई से अध्ययन किया जाता है।
                
                1. परिचय और महत्व:
                $topic हमें यह समझने में मदद करता है कि मुख्य नियम और प्रक्रियाएँ वास्तविक परिस्थितियों में कैसे कार्य करती हैं। ${req.exam} में इस अध्याय से प्रत्यक्ष तथ्यात्मक और अवधारणात्मक दोनों प्रश्न पूछे जाते हैं।
                
                2. प्रमुख घटक:
                • आधारभूत सिद्धांत: यह विषय की नींव और कार्यप्रणाली को स्पष्ट करता है।
                • वर्गीकरण: मुख्य प्रकारों व उनके अंतर को क्रमबद्ध रूप से समझना आवश्यक है।
                • परीक्षा दृष्टिकोण: NCERT के प्रमुख शब्द, तिथियाँ/सूत्र और अपवाद विशेष ध्यान देने योग्य हैं।
            """.trimIndent(),
            shortNotes = listOf(
                "$topic अध्याय ${req.exam} और ${req.classLevel} परीक्षा में उच्च वेटेज रखता है।",
                "परिभाषा, प्रमुख नियम और अपवादों को संक्षिप्त बिंदुओं में याद करें।",
                "उत्तर लिखते समय तालिका और फ्लोचार्ट का उपयोग करने से अतिरिक्त अंक मिलते हैं।",
                "अध्याय पूरा करने के बाद कम से कम 20 वस्तुनिष्ठ प्रश्नों का अभ्यास करें।"
            ),
            easyExplanation = "$topic को एक सरल उदाहरण से समझें: जैसे किसी मशीन के सभी पुर्जे एक निश्चित क्रम में मिलकर सही परिणाम देते हैं, वैसे ही $topic के मूल नियम एक-दूसरे से जुड़कर पूरी प्रक्रिया को स्पष्ट करते हैं।",
            definitions = listOf(
                DefinitionItem("$topic की मूल परिभाषा", "${req.subject} की वह प्रमुख अवधारणा जो इसके सिद्धांतों और परिणामों को क्रमबद्ध ढंग से समझाती है।"),
                DefinitionItem("वर्गीकरण (Classification)", "$topic के विभिन्न रूपों को उनके गुणधर्म और उपयोग के आधार पर विभाजित करना।"),
                DefinitionItem("व्यावहारिक अनुप्रयोग", "परीक्षा और दैनिक जीवन की समस्याओं को हल करने में $topic के नियमों का सीधा प्रयोग।")
            ),
            importantFacts = listOf(
                "NCERT के बॉक्स और सारांश से ${req.exam} में सीधे प्रश्न पूछे जाते हैं।",
                "तुलनात्मक तालिका से दो समान अवधारणाओं के बीच भ्रम दूर होता है।",
                "पिछले वर्षों के प्रश्नपत्रों में इस टॉपिक की नियमित पुनरावृत्ति देखी गई है।"
            ),
            comparisonTable = TableData(
                title = "$topic — तुलनात्मक तालिका",
                headers = listOf("आयाम", "मूल बिंदु", "परीक्षा उपयोग (${req.exam})"),
                rows = listOf(
                    listOf("परिभाषा", "स्पष्ट व सटीक शब्दावली", "अति लघु उत्तरीय प्रश्न"),
                    listOf("प्रक्रिया", "क्रमबद्ध चरण (फ्लोचार्ट)", "दीर्घ उत्तरीय प्रश्न"),
                    listOf("मुख्य अपवाद", "विशेष स्थितियाँ व नियम", "बहुविकल्पीय (MCQ) प्रश्न")
                )
            ),
            flowchartSteps = listOf(
                "चरण 1: $topic की मुख्य परिभाषा और उद्देश्य समझें",
                "चरण 2: इसके प्रमुख प्रकारों और नियमों की सूची बनाएं",
                "चरण 3: कारण और प्रभाव का संबंध फ्लोचार्ट में जोड़ें",
                "चरण 4: दैनिक जीवन व पाठ्यपुस्तक के उदाहरण याद करें",
                "चरण 5: ${req.exam} के पिछले वर्षों के प्रश्न हल करें"
            ),
            examples = listOf(
                "पाठ्यपुस्तक उदाहरण: NCERT ${req.subject} में वर्णित प्रमुख मानक उदाहरण।",
                "दैनिक जीवन उदाहरण: भारतीय संदर्भ में $topic का प्रत्यक्ष व्यावहारिक उपयोग।"
            ),
            examOrientedPoints = listOf(
                "उत्तर में प्रमुख तकनीकी शब्दों को रेखांकित (underline) करें।",
                "वस्तुनिष्ठ प्रश्नों में 'केवल', 'कभी नहीं' जैसे शब्दों पर सावधानी रखें।",
                "कम से कम एक स्वच्छ आरेख या फ्लोचार्ट अवश्य बनाएं।"
            ),
            quickRevision = listOf(
                "अध्याय: $topic (${req.subject} • ${req.exam})",
                "3 स्तंभ: परिभाषा + वर्गीकरण + अपवाद।",
                "रिवीजन सूत्र: आज रात 5 मिनट और 7 दिन बाद पुनः दोहराएं।"
            ),
            isSamplePreview = true
        )
    }

    private fun buildHinglishSampleNotes(req: NotesRequest, topic: String): GeneratedNotes {
        return GeneratedNotes(
            title = "$topic — ${req.notesType.label} (Hinglish)",
            classLevel = req.classLevel,
            exam = req.exam,
            subject = req.subject,
            topic = topic,
            language = req.language,
            notesType = req.notesType,
            detailedNotes = """
                [Sample AI Preview — Live Gemini notes ke liye Firebase AI Logic enable karein]
                
                $topic (${req.subject}) ${req.classLevel} aur ${req.exam} preparation ke liye ek super high-scoring topic hai. Agar aap iske basic logic aur exam keywords samajh lein toh full marks laana kaafi easy ho jata hai.
                
                1. Basic Overview:
                $topic ka main aim hai core rules ko step-by-step explain karna aur unhe real-world situations se connect karna. ${req.exam} mein yahan se direct MCQs aur conceptual descriptive questions dono aate hain.
                
                2. Important Pillars:
                • Concept Clarity: Sabse pehle exact definition aur mechanism samjhein.
                • Types & Differences: Comparison table ke through confusion clear rakhein.
                • Smart Exam Approach: Exceptions aur NCERT highlights par extra focus karein.
            """.trimIndent(),
            shortNotes = listOf(
                "$topic ${req.exam} ke high-yield chapters mein se ek hai.",
                "Exact definition + 3 key properties + 1 example yaad rakhna best strategy hai.",
                "Flowchart aur table format se answer presentation kaafi impressive banta hai.",
                "Revision ke baad turant 10–20 MCQs practice karein."
            ),
            easyExplanation = "$topic ko simple bhasha mein aise samjho: jaise ek GPS aapko step-by-step sahi destination tak le jata hai, waise hi $topic ke core rules aapko complex ${req.subject} problems ko easily solve karne mein help karte hain.",
            definitions = listOf(
                DefinitionItem("$topic Definition", "${req.subject} ka wo fundamental concept jo batata hai ki system ke components kaise work karte hain."),
                DefinitionItem("Key Classification", "$topic ko structure aur function ke basis par alag-alag categories mein divide karna."),
                DefinitionItem("Exam Application", "${req.exam} ke numerical, objective aur case-study questions mein is concept ka direct use.")
            ),
            importantFacts = listOf(
                "NCERT ke bold keywords aur summary points se direct MCQs bante hain.",
                "Previous Year Questions (PYQs) mein is chapter ka pattern har saal repeat hota hai.",
                "Exceptions par based statements sabse zyada confusing hote hain — inhe highlight karein."
            ),
            comparisonTable = TableData(
                title = "$topic — Quick Comparison Table",
                headers = listOf("Point", "Basic Concept", "${req.exam} Focus"),
                rows = listOf(
                    listOf("Definition", "Simple crisp meaning", "1–2 Mark Very Short Qs"),
                    listOf("Mechanism", "Step-by-step flow", "Long Answer + Flowchart"),
                    listOf("Exceptions", "Special conditions", "Tricky MCQ Options")
                )
            ),
            flowchartSteps = listOf(
                "Step 1: $topic ki crisp definition samjho",
                "Step 2: Core rules aur classification yaad karo",
                "Step 3: Cause → Process → Result chain trace karo",
                "Step 4: Real-life + NCERT example link karo",
                "Step 5: ${req.exam} PYQs aur MCQs solve karo"
            ),
            examples = listOf(
                "NCERT Example: ${req.classLevel} ${req.subject} ka standard chapter illustration.",
                "Daily Life Example: Real Indian context mein $topic ka practical use case."
            ),
            examOrientedPoints = listOf(
                "Descriptive answers mein headings aur bullet points use karein.",
                "Objective test mein elimination technique apply karein.",
                "Last minute revision ke liye Quick Revision box zaroor dekhein."
            ),
            quickRevision = listOf(
                "Chapter: $topic | Exam: ${req.exam} | Subject: ${req.subject}",
                "Formula for Full Marks: Definition + Table + Flowchart + Example.",
                "24-Hour Rule: Kal phir 3 minute mein short notes revise karein!"
            ),
            isSamplePreview = true
        )
    }

    private data class McqTemplate(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String
    )

    private fun englishMcqTemplates(topic: String, subject: String, exam: String, classLevel: String): List<McqTemplate> = listOf(
        McqTemplate(
            question = "Which statement most accurately reflects the core objective of studying $topic in $subject ($exam)?",
            options = listOf(
                "Understanding underlying principles, classifications, and practical applications",
                "Memorizing unrelated trivia without conceptual linkage",
                "Ignoring standard NCERT definitions during exam answers",
                "Replacing analytical reasoning with guesswork"
            ),
            correctIndex = 0,
            explanation = "Strong exam performance in $exam requires conceptual clarity, accurate definitions, and applied understanding of $topic."
        ),
        McqTemplate(
            question = "In the context of $topic ($classLevel $subject), which study method best prevents confusion between closely related concepts?",
            options = listOf(
                "Skipping comparison tables entirely",
                "Constructing a structured comparison matrix highlighting similarities and exceptions",
                "Reading only the chapter title once",
                "Avoiding previous year questions"
            ),
            correctIndex = 1,
            explanation = "Comparison matrices isolate key distinguishing features and boundary exceptions frequently tested in $exam."
        ),
        McqTemplate(
            question = "When solving assertion–reason or multi-statement MCQs on $topic for $exam, what should you verify first?",
            options = listOf(
                "Length of the question sentence",
                "Whether Option D is always correct",
                "Accuracy of each statement independently and the logical cause-effect link",
                "Alphabetical order of options"
            ),
            correctIndex = 2,
            explanation = "Checking each statement independently and then evaluating causal linkage eliminates common distractor traps."
        ),
        McqTemplate(
            question = "Which visual representation is most effective for explaining sequential mechanisms within $topic?",
            options = listOf(
                "Unstructured paragraph without headings",
                "Random bullet list with no order",
                "Blank page margin",
                "A step-by-step labeled flowchart from input/cause to final outcome"
            ),
            correctIndex = 3,
            explanation = "Flowcharts clearly show directional causality and earn high examiner clarity scores in $classLevel and $exam."
        ),
        McqTemplate(
            question = "Why are NCERT summary boxes and definitions emphasized for $topic in $exam?",
            options = listOf(
                "Exam questions frequently lift exact terminology and exceptions directly from standard NCERT text",
                "They are never included in syllabus guidelines",
                "They only apply to art competitions",
                "They contradict standard textbooks"
            ),
            correctIndex = 0,
            explanation = "Standard Indian school and competitive exams closely benchmark factual accuracy against NCERT terminology."
        ),
        McqTemplate(
            question = "Which revision schedule produces the highest long-term retention for $topic?",
            options = listOf(
                "Cramming only 10 minutes before entering the exam hall",
                "Spaced revision: Day 1 summary, Day 3 active recall, and Day 7 MCQ practice",
                "Never reviewing mistakes made in mock tests",
                "Passive re-reading without self-testing"
            ),
            correctIndex = 1,
            explanation = "Spaced repetition paired with active MCQ testing strengthens memory retrieval under exam time pressure."
        ),
        McqTemplate(
            question = "For a 5-mark descriptive answer on $topic in $classLevel $subject, what structure maximizes marks?",
            options = listOf(
                "Writing one continuous unformatted block of text",
                "Omitting definitions and examples",
                "Crisp definition + labeled flowchart/table + key points + relevant example",
                "Repeating the question sentence four times"
            ),
            correctIndex = 2,
            explanation = "Structured answers with underlined keywords, diagrams, and examples match official board marking schemes."
        ),
        McqTemplate(
            question = "What is the smartest way to analyze errors after attempting a practice test on $topic?",
            options = listOf(
                "Ignore wrong answers immediately",
                "Delete all study notes",
                "Stop studying $subject completely",
                "Log missed concepts in a quick revision sheet and re-attempt them after 48 hours"
            ),
            correctIndex = 3,
            explanation = "Tracking mistakes converts weak areas in $topic into reliable scoring opportunities in $exam."
        ),
        McqTemplate(
            question = "In $exam objective exams with negative marking, how should you handle an unfamiliar question on $topic?",
            options = listOf(
                "Use logical elimination if 2 options are clearly ruled out; otherwise skip blindly guessing",
                "Always guess randomly on every question",
                "Leave the entire paper blank",
                "Mark all four options simultaneously"
            ),
            correctIndex = 0,
            explanation = "Calculated elimination protects accuracy while preventing unnecessary negative marks."
        ),
        McqTemplate(
            question = "Which pairing best describes an exam-ready mastery checklist for $topic?",
            options = listOf(
                "Zero notes + zero practice",
                "Core definitions + important facts + PYQ patterns + timed MCQ practice",
                "Memorizing page numbers only",
                "Studying unrelated chapters instead"
            ),
            correctIndex = 1,
            explanation = "Combining conceptual notes with PYQs and timed quizzes builds both speed and accuracy for $exam."
        )
    )

    private fun hindiMcqTemplates(topic: String, subject: String, exam: String, classLevel: String): List<McqTemplate> = listOf(
        McqTemplate(
            question = "$subject ($classLevel) में '$topic' अध्याय को $exam के लिए तैयार करने का सबसे प्रभावी तरीका क्या है?",
            options = listOf(
                "मूल परिभाषा, वर्गीकरण और व्यावहारिक उदाहरणों को एक साथ समझना",
                "बिना समझे केवल शीर्षक रटना",
                "NCERT शब्दावली को नज़रअंदाज़ करना",
                "अभ्यास प्रश्नों को बिल्कुल हल न करना"
            ),
            correctIndex = 0,
            explanation = "$exam में उच्च अंक के लिए '$topic' की अवधारणात्मक स्पष्टता और NCERT आधारित समझ आवश्यक है।"
        ),
        McqTemplate(
            question = "'$topic' से जुड़े दो समान सिद्धांतों के बीच अंतर स्पष्ट रखने के लिए कौन-सा साधन सर्वश्रेष्ठ है?",
            options = listOf(
                "अव्यवस्थित पैराग्राफ",
                "तुलनात्मक तालिका (Comparison Table) और मुख्य अपवादों की सूची",
                "केवल अध्याय संख्या याद करना",
                "पिछले वर्षों के प्रश्न न देखना"
            ),
            correctIndex = 1,
            explanation = "तुलनात्मक तालिका परीक्षा के समय त्वरित स्मरण और विकल्प उन्मूलन (elimination) में मदद करती है।"
        ),
        McqTemplate(
            question = "$exam में '$topic' पर आधारित क्रमबद्ध प्रक्रिया को दर्शाने का सर्वोत्तम तरीका क्या है?",
            options = listOf(
                "बिना शीर्षक का लंबा लेख",
                "अप्रासंगिक वाक्य जोड़ना",
                "चरण-दर-चरण स्वच्छ फ्लोचार्ट (Flowchart)",
                "केवल अंतिम निष्कर्ष लिखना"
            ),
            correctIndex = 2,
            explanation = "फ्लोचार्ट कारण-प्रभाव श्रृंखला को स्पष्ट करता है और परीक्षक पर सकारात्मक प्रभाव डालता है।"
        ),
        McqTemplate(
            question = "$topic के दीर्घकालिक स्मरण के लिए कौन-सी रिवीजन रणनीति सबसे अधिक वैज्ञानिक है?",
            options = listOf(
                "परीक्षा से 5 मिनट पहले पहली बार पढ़ना",
                "गलत प्रश्नों का विश्लेषण न करना",
                "बिना अभ्यास के केवल पन्ने पलटना",
                "अंतराल रिवीजन (1 दिन, 3 दिन, 7 दिन) के साथ MCQ अभ्यास"
            ),
            correctIndex = 3,
            explanation = "अंतराल रिवीजन और सक्रिय परीक्षण (Active Recall) स्मृति को स्थायी बनाते हैं।"
        ),
        McqTemplate(
            question = "$exam की नकारात्मक अंकन (Negative Marking) परीक्षा में कठिन प्रश्न पर क्या करना चाहिए?",
            options = listOf(
                "तार्किक उन्मूलन (Elimination) से 2 विकल्प हटें तो विचार करें, अन्यथा अंधाधुंध तुक्का न लगाएं",
                "हर अनजान प्रश्न पर तुक्का लगाएं",
                "पूरा सेक्शन छोड़ दें",
                "दो विकल्प एक साथ चुनें"
            ),
            correctIndex = 0,
            explanation = "सटीकता बनाए रखना नकारात्मक अंकन वाली प्रतियोगी परीक्षाओं में सफलता की कुंजी है।"
        )
    )

    private fun hinglishMcqTemplates(topic: String, subject: String, exam: String, classLevel: String): List<McqTemplate> = listOf(
        McqTemplate(
            question = "$exam aur $classLevel ($subject) ke liye '$topic' ko master karne ka sabse smart tareeka kaunsa hai?",
            options = listOf(
                "Concept clarity + NCERT keywords + Flowchart + PYQ practice",
                "Sirf chapter ka naam ratna",
                "Practice test skip karna",
                "Exceptions ko ignore karna"
            ),
            correctIndex = 0,
            explanation = "Concept clarity ke saath structured revision aur PYQs solve karne se exam confidence boost hota hai."
        ),
        McqTemplate(
            question = "'$topic' ke tricky MCQs mein confusion avoid karne ke liye kya sabse useful hota hai?",
            options = listOf(
                "Random guessing",
                "Comparison table aur key exceptions ka quick revision card",
                "Sirf long paragraphs padhna",
                "Mock test analysis na karna"
            ),
            correctIndex = 1,
            explanation = "Comparison tables aur exceptions aapko similar-looking options ke beech turant difference dikhate hain."
        ),
        McqTemplate(
            question = "Long answer questions on '$topic' mein full marks score karne ka best format kya hai?",
            options = listOf(
                "Ek hi bada paragraph bina headings ke",
                "Sirf 1 line likh dena",
                "Crisp Definition + Flowchart/Diagram + Bullet Points + Example",
                "Question ko dobara copy karna"
            ),
            correctIndex = 2,
            explanation = "Neat presentation with headings, flowchart, aur underlined keywords board aur mains dono mein top marks dilate hain."
        ),
        McqTemplate(
            question = "Practice test dene ke baad score improve karne ka sabse important step kya hai?",
            options = listOf(
                "Score dekh kar test bhool jana",
                "Naye chapters bina revision start karna",
                "Galat questions ko skip karna",
                "Wrong answers ka explanation padh kar mistake notebook mein add karna"
            ),
            correctIndex = 3,
            explanation = "Mistake notebook se aap apni weak concepts ko high-accuracy topics mein convert kar sakte hain."
        ),
        McqTemplate(
            question = "$exam ke previous year questions (PYQs) '$topic' ke liye kyun zaroori hain?",
            options = listOf(
                "Inse actual exam weightage, difficulty level aur repeated patterns samajh aate hain",
                "Inka syllabus se koi lena-dena nahi hota",
                "Ye sirf time waste karte hain",
                "Inse concepts weak hote hain"
            ),
            correctIndex = 0,
            explanation = "PYQs exam ke exact trend aur high-yield sub-topics ko identify karne ka sabse reliable source hain."
        )
    )

    private fun buildSampleQnAItem(
        id: Int,
        type: QnAType,
        req: QnARequest,
        topic: String,
        variant: Int
    ): QnAItem {
        val isHindi = req.language == StudyLanguage.HINDI
        val isHinglish = req.language == StudyLanguage.HINGLISH

        val (q, a, pts) = when (type) {
            QnAType.VERY_SHORT -> if (isHindi) {
                Triple(
                    if (variant == 1) "$topic को एक वाक्य में परिभाषित कीजिए।" else "$topic का कोई एक प्रमुख उदाहरण लिखिए।",
                    if (variant == 1) "$topic, ${req.subject} की वह मूल अवधारणा है जो इसके प्रमुख नियमों और व्यावहारिक परिणामों को स्पष्ट करती है।" else "NCERT ${req.classLevel} ${req.subject} में वर्णित दैनिक जीवन का मानक उदाहरण $topic को प्रत्यक्ष रूप से दर्शाता है।",
                    listOf("सटीक परिभाषा (1–2 अंक)", "मुख्य तकनीकी शब्द शामिल करें")
                )
            } else if (isHinglish) {
                Triple(
                    if (variant == 1) "$topic ko 1–2 crisp lines mein define karein." else "$topic ka ek key exam keyword aur example likhein.",
                    if (variant == 1) "$topic ${req.subject} ka core concept hai jo fundamental rules aur unke practical outcomes ko explain karta hai." else "Real-world Indian syllabus context mein $topic ka standard NCERT illustration sabse accurate example mana jata hai.",
                    listOf("Crisp 25–30 word answer", "Underline exact term")
                )
            } else {
                Triple(
                    if (variant == 1) "Define $topic concisely in 1–2 sentences." else "State one primary characteristic and one example of $topic.",
                    if (variant == 1) "$topic is a foundational concept in ${req.subject} (${req.classLevel}) that describes the governing principles, structure, and measurable effects of the topic in exam contexts." else "A primary characteristic of $topic is its structured cause-and-effect relationship, illustrated through standard NCERT ${req.subject} applications.",
                    listOf("1–2 Marks Target", "Direct keyword precision", "Ideal for quick recall")
                )
            }

            QnAType.SHORT -> if (isHindi) {
                Triple(
                    if (variant == 1) "$topic की तीन प्रमुख विशेषताएँ संक्षेप में लिखिए।" else "${req.exam} के संदर्भ में $topic का महत्व समझाइए।",
                    "1. यह ${req.subject} के आधारभूत सिद्धांतों को सुव्यवस्थित रूप से जोड़ता है।\n2. इसके वर्गीकरण से जटिल समस्याओं को सरल चरणों में हल किया जा सकता है।\n3. परीक्षा में लघु उत्तरीय और वस्तुनिष्ठ दोनों प्रश्नों में इसकी सीधी उपयोगिता है।",
                    listOf("3–4 स्पष्ट बिंदु", "शीर्षक और उदाहरण जोड़ें")
                )
            } else {
                Triple(
                    if (variant == 1) "Explain three key features of $topic relevant to ${req.classLevel} ${req.subject}." else "Differentiate between core theory and practical application in $topic.",
                    "1. Foundational Rule: Establishes the core framework of $topic.\n2. Systematic Structure: Organizes sub-concepts cleanly to avoid overlap.\n3. Exam Relevance (${req.exam}): Frequently tested in 3–4 mark structured questions requiring concise points and an example.",
                    listOf("Write in 3–4 numbered points", "Keep around 60–80 words", "Add 1 concise example")
                )
            }

            QnAType.LONG -> if (isHindi) {
                Triple(
                    if (variant == 1) "$topic की संरचना, कार्यप्रणाली और महत्व का विस्तारपूर्वक वर्णन कीजिए।" else "$topic से संबंधित प्रमुख नियमों और उनके व्यावहारिक अनुप्रयोगों की विवेचना कीजिए।",
                    "भूमिका: $topic, ${req.subject} का अत्यंत महत्वपूर्ण अध्याय है।\n\nमुख्य भाग:\n• सिद्धांत एवं परिभाषा: इसके मूल नियमों को क्रमबद्ध ढंग से समझा जाता है।\n• कार्यप्रणाली (फ्लोचार्ट): कारण → प्रक्रिया → परिणाम के क्रम में विश्लेषण करें।\n• उदाहरण एवं अपवाद: NCERT आधारित उदाहरणों के साथ विशेष स्थितियाँ स्पष्ट करें।\n\nनिष्कर्ष: ${req.classLevel} एवं ${req.exam} में उत्कृष्ट अंकों के लिए आरेख और मुख्य बिंदुओं का समावेश अनिवार्य है।",
                    listOf("भूमिका + मुख्य बिंदु + फ्लोचार्ट + निष्कर्ष", "5–8 अंक के लिए आदर्श संरचना")
                )
            } else {
                Triple(
                    if (variant == 1) "Discuss $topic in detail, including its core principles, mechanism, and real-world applications." else "Critically analyze the significance of $topic in ${req.subject} with suitable examples and a diagram/flowchart.",
                    "Introduction:\n$topic plays a vital role in ${req.subject} for ${req.classLevel} and ${req.exam} examinations.\n\nDetailed Analysis:\n1. Core Framework: Define the scope and governing laws clearly.\n2. Step-by-Step Mechanism: Trace the sequential flow from initial condition to outcome.\n3. Comparative Insights: Highlight how primary categories differ and note key exceptions.\n4. Practical Examples: Cite two curriculum-aligned examples.\n\nConclusion:\nSummarize the broader academic and competitive exam significance of $topic in 2 crisp lines.",
                    listOf("Introduction + Body + Flowchart + Conclusion", "150–200 words for 5–8 Marks", "Highlight technical vocabulary")
                )
            }

            QnAType.OBJECTIVE -> Triple(
                if (isHindi) "वस्तुनिष्ठ प्रश्न ($variant): $topic के संदर्भ में सर्वाधिक सटीक कथन कौन-सा है?" else "Objective Q$variant: Which statement accurately identifies the core principle of $topic in ${req.subject}?",
                if (isHindi) "सही उत्तर: (A) यह NCERT आधारित मूल नियम, वर्गीकरण और व्यावहारिक अनुप्रयोग को एकीकृत करता है।\nव्याख्या: ${req.exam} में प्रत्यक्ष तथ्य और अवधारणा की स्पष्टता से सही विकल्प चुना जाता है।" else "Correct Answer: (A) It integrates foundational definitions, structured classification, and exam-relevant applications.\nExplanation: Objective papers in ${req.exam} test precise conceptual distinctions and NCERT definitions.",
                listOf("1 Mark Objective", "Check distractors carefully")
            )

            QnAType.SUBJECTIVE -> Triple(
                if (isHindi) "विषयनिष्ठ प्रश्न ($variant): अपने शब्दों में स्पष्ट कीजिए कि $topic का ज्ञान ${req.subject} के अन्य अध्यायों को समझने में कैसे सहायक है?" else "Subjective Q$variant: Evaluate how mastering $topic strengthens analytical problem-solving in ${req.subject}.",
                if (isHindi) "$topic केवल एक पृथक अध्याय नहीं है, बल्कि यह ${req.subject} के अन्य विषयों के साथ गहराई से जुड़ा हुआ है। इसके सिद्धांतों की स्पष्ट समझ से विद्यार्थी विश्लेषणात्मक प्रश्नों का तार्किक व संतुलित उत्तर लिख सकते हैं।" else "Mastering $topic builds a transferable conceptual lens across ${req.subject}. Students can connect definitions with higher-order case studies, construct evidence-backed arguments, and answer interdisciplinary ${req.exam} questions confidently.",
                listOf("Analytical depth", "Balanced arguments + conclusion")
            )

            QnAType.IMPORTANT_EXAM -> Triple(
                if (isHindi) "महत्वपूर्ण परीक्षा प्रश्न ($variant • ${req.exam}): $topic से बार-बार पूछे जाने वाले प्रमुख बिंदु और अपवाद कौन-से हैं?" else "High-Weightage ${req.exam} Question ($variant): Explain the most frequently examined concepts and exception rules in $topic.",
                if (isHindi) "${req.exam} के विगत वर्षों के प्रश्नपत्रों के अनुसार $topic से (1) मानक परिभाषा, (2) तुलनात्मक अंतर, (3) क्रमबद्ध प्रक्रिया और (4) विशेष अपवाद सबसे अधिक पूछे जाते हैं। इन्हें बुलेट बिंदुओं और स्वच्छ आरेख के साथ प्रस्तुत करें।" else "Based on recurring ${req.exam} trends, examiners focus heavily on: (1) exact NCERT definitions, (2) comparative distinctions, (3) cause-effect flowcharts, and (4) boundary exception cases in $topic. Presenting these in structured bullet points secures top marks.",
                listOf("High Weightage (${req.exam})", "PYQ-aligned pattern", "Must-revise before exam")
            )
        }

        return QnAItem(
            id = id,
            type = type,
            question = q,
            answer = "[Sample AI Preview] $a",
            keyPoints = pts
        )
    }

    private fun dailyGkEnglishQuestions(): List<McqQuestion> = listOf(
        McqQuestion(
            id = 1,
            question = "Which Article of the Indian Constitution is famously termed the 'Heart and Soul of the Constitution' by Dr. B. R. Ambedkar?",
            options = listOf("Article 14", "Article 19", "Article 32", "Article 44"),
            correctIndex = 2,
            explanation = "Article 32 grants the Right to Constitutional Remedies, enabling citizens to move the Supreme Court directly for enforcing Fundamental Rights.",
            examTag = "UPSC • SSC • Polity"
        ),
        McqQuestion(
            id = 2,
            question = "Which river is known as the 'Dakshin Ganga' (Ganga of the South) in Indian Geography?",
            options = listOf("Krishna", "Godavari", "Kaveri", "Mahanadi"),
            correctIndex = 1,
            explanation = "The Godavari is the longest river of Peninsular India (approx. 1,465 km) and has the largest drainage basin in the Deccan.",
            examTag = "SSC • Railway • Geography"
        ),
        McqQuestion(
            id = 3,
            question = "The first Battle of Panipat (1526) was fought between Babur and which ruler of the Delhi Sultanate?",
            options = listOf("Ibrahim Lodi", "Sikandar Lodi", "Sher Shah Suri", "Rana Sanga"),
            correctIndex = 0,
            explanation = "Babur defeated Ibrahim Lodi in April 1526 at Panipat, establishing the Mughal Empire in India.",
            examTag = "BPSC • SSC • History"
        ),
        McqQuestion(
            id = 4,
            question = "Which organelle is known as the 'Powerhouse of the Cell' due to ATP synthesis?",
            options = listOf("Ribosome", "Golgi Apparatus", "Lysosome", "Mitochondria"),
            correctIndex = 3,
            explanation = "Mitochondria generate most of the cell's supply of adenosine triphosphate (ATP) through cellular respiration.",
            examTag = "CBSE • Railway • Biology"
        ),
        McqQuestion(
            id = 5,
            question = "Who was the first Chairman of the Planning Commission of India (established in 1950)?",
            options = listOf("Dr. Rajendra Prasad", "Jawaharlal Nehru", "Sardar Vallabhbhai Patel", "C. D. Deshmukh"),
            correctIndex = 1,
            explanation = "The Prime Minister served as the ex-officio Chairman of the Planning Commission, so Pandit Jawaharlal Nehru was its first Chairman.",
            examTag = "UPSC • State PCS • Economy"
        ),
        McqQuestion(
            id = 6,
            question = "What is the SI unit of electrical resistance?",
            options = listOf("Volt", "Ampere", "Ohm", "Watt"),
            correctIndex = 2,
            explanation = "Electrical resistance is measured in Ohms (Ω), named after German physicist Georg Simon Ohm.",
            examTag = "CBSE • Railway • Physics"
        ),
        McqQuestion(
            id = 7,
            question = "The ancient Nalanda Mahavihara (University) in Bihar was founded during the reign of which Gupta ruler?",
            options = listOf("Kumaragupta I", "Samudragupta", "Chandragupta I", "Skandagupta"),
            correctIndex = 0,
            explanation = "Kumaragupta I of the Gupta dynasty founded the renowned Nalanda University in the 5th century CE.",
            examTag = "BPSC • UPSC • History"
        ),
        McqQuestion(
            id = 8,
            question = "Which planet in our Solar System rotates from East to West (retrograde rotation) along with Uranus?",
            options = listOf("Mars", "Mercury", "Jupiter", "Venus"),
            correctIndex = 3,
            explanation = "Venus rotates clockwise (East to West) on its axis, making the Sun appear to rise in the west on Venus.",
            examTag = "SSC • CBSE • Science"
        ),
        McqQuestion(
            id = 9,
            question = "Which constitutional amendment lowered the voting age in India from 21 years to 18 years?",
            options = listOf("42nd Amendment", "44th Amendment", "61st Amendment", "73rd Amendment"),
            correctIndex = 2,
            explanation = "The 61st Constitutional Amendment Act of 1988 amended Article 326 to lower the voting age to 18 years.",
            examTag = "UPSC • SSC • Polity"
        ),
        McqQuestion(
            id = 10,
            question = "Which gas is primarily responsible for the sharp smell around electrical discharges and protects Earth from UV rays in the stratosphere?",
            options = listOf("Nitrogen", "Ozone", "Argon", "Methane"),
            correctIndex = 1,
            explanation = "Ozone (O₃) in the stratosphere absorbs harmful ultraviolet (UV-B and UV-C) solar radiation.",
            examTag = "CBSE • Environment • GK"
        )
    )

    private fun dailyGkHindiQuestions(): List<McqQuestion> = listOf(
        McqQuestion(
            id = 1,
            question = "डॉ. बी. आर. अंबेडकर ने भारतीय संविधान के किस अनुच्छेद को 'संविधान का हृदय और आत्मा' कहा था?",
            options = listOf("अनुच्छेद 14", "अनुच्छेद 19", "अनुच्छेद 32", "अनुच्छेद 44"),
            correctIndex = 2,
            explanation = "अनुच्छेद 32 संवैधानिक उपचारों का अधिकार देता है, जिसके तहत नागरिक मौलिक अधिकारों की रक्षा के लिए सीधे सर्वोच्च न्यायालय जा सकते हैं।",
            examTag = "UPSC • SSC • राजव्यवस्था"
        ),
        McqQuestion(
            id = 2,
            question = "भारत की किस नदी को 'दक्षिण गंगा' के नाम से जाना जाता है?",
            options = listOf("कृष्णा", "गोदावरी", "कावेरी", "महानदी"),
            correctIndex = 1,
            explanation = "गोदावरी प्रायद्वीपीय भारत की सबसे लंबी नदी (लगभग 1,465 किमी) है।",
            examTag = "SSC • रेलवे • भूगोल"
        ),
        McqQuestion(
            id = 3,
            question = "पानीपत का प्रथम युद्ध (1526) बाबर और दिल्ली सल्तनत के किस शासक के बीच लड़ा गया था?",
            options = listOf("इब्राहिम लोदी", "सिकंदर लोदी", "शेरशाह सूरी", "राणा सांगा"),
            correctIndex = 0,
            explanation = "अप्रैल 1526 में बाबर ने इब्राहिम लोदी को पराजित कर भारत में मुगल साम्राज्य की नींव रखी।",
            examTag = "BPSC • SSC • इतिहास"
        ),
        McqQuestion(
            id = 4,
            question = "कोशिका का 'पावरहाउस' (ऊर्जा गृह) किस कोशिकांग को कहा जाता है?",
            options = listOf("राइबोसोम", "गॉल्जीकाय", "लाइसोसोम", "माइटोकॉन्ड्रिया"),
            correctIndex = 3,
            explanation = "माइटोकॉन्ड्रिया कोशिकीय श्वसन द्वारा एटीपी (ATP) के रूप में ऊर्जा उत्पन्न करता है।",
            examTag = "CBSE • रेलवे • जीव विज्ञान"
        ),
        McqQuestion(
            id = 5,
            question = "बिहार में स्थित प्राचीन नालंदा विश्वविद्यालय की स्थापना किस गुप्त शासक ने की थी?",
            options = listOf("कुमारगुप्त प्रथम", "समुद्रगुप्त", "चंद्रगुप्त प्रथम", "स्कंदगुप्त"),
            correctIndex = 0,
            explanation = "गुप्त वंश के शासक कुमारगुप्त प्रथम ने 5वीं शताब्दी ईस्वी में नालंदा महाविहार की स्थापना की थी।",
            examTag = "BPSC • UPSC • इतिहास"
        ),
        McqQuestion(
            id = 6,
            question = "विद्युत प्रतिरोध (Electrical Resistance) का SI मात्रक क्या है?",
            options = listOf("वोल्ट", "एम्पीयर", "ओम (Ohm)", "वाट"),
            correctIndex = 2,
            explanation = "विद्युत प्रतिरोध का SI मात्रक ओम (Ω) है।",
            examTag = "CBSE • रेलवे • भौतिकी"
        ),
        McqQuestion(
            id = 7,
            question = "किस संविधान संशोधन द्वारा भारत में मतदान की आयु 21 वर्ष से घटाकर 18 वर्ष कर दी गई थी?",
            options = listOf("42वाँ संशोधन", "44वाँ संशोधन", "61वाँ संशोधन", "73वाँ संशोधन"),
            correctIndex = 2,
            explanation = "61वें संविधान संशोधन अधिनियम (1988) द्वारा अनुच्छेद 326 में संशोधन कर मतदान आयु 18 वर्ष की गई।",
            examTag = "UPSC • SSC • राजव्यवस्था"
        ),
        McqQuestion(
            id = 8,
            question = "सौरमंडल का कौन-सा ग्रह पूर्व से पश्चिम दिशा में घूर्णन करता है?",
            options = listOf("मंगल", "बुध", "बृहस्पति", "शुक्र (Venus)"),
            correctIndex = 3,
            explanation = "शुक्र और अरुण (Uranus) पूर्व से पश्चिम की ओर घूर्णन करते हैं।",
            examTag = "SSC • CBSE • विज्ञान"
        ),
        McqQuestion(
            id = 9,
            question = "भारत के योजना आयोग (1950) के प्रथम अध्यक्ष कौन थे?",
            options = listOf("डॉ. राजेंद्र प्रसाद", "पंडित जवाहरलाल नेहरू", "सरदार पटेल", "सी. डी. देशमुख"),
            correctIndex = 1,
            explanation = "भारत के प्रधानमंत्री योजना आयोग के पदेन अध्यक्ष होते थे; अतः पंडित नेहरू प्रथम अध्यक्ष थे।",
            examTag = "UPSC • अर्थव्यवस्था"
        ),
        McqQuestion(
            id = 10,
            question = "समताप मंडल में कौन-सी गैस पृथ्वी को हानिकारक पराबैंगनी (UV) किरणों से बचाती है?",
            options = listOf("नाइट्रोजन", "ओज़ोन (O₃)", "आर्गन", "मीथेन"),
            correctIndex = 1,
            explanation = "समताप मंडल में स्थित ओज़ोन परत सूर्य की हानिकारक पराबैंगनी किरणों को अवशोषित करती है।",
            examTag = "CBSE • पर्यावरण • GK"
        )
    )
}
