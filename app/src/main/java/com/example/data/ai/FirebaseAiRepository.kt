package com.example.data.ai

import android.util.Log
import com.example.data.model.ChapterQnARequest
import com.example.data.model.ChapterStudyPack
import com.example.data.model.ChatUiMessage
import com.example.data.model.GeneratedNotes
import com.example.data.model.McqQuestion
import com.example.data.model.McqRequest
import com.example.data.model.NotesRequest
import com.example.data.model.NotesType
import com.example.data.model.ObjectiveQuestionItem
import com.example.data.model.QnAItem
import com.example.data.model.QnARequest
import com.example.data.model.StudyLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "FirebaseAiRepository"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

/**
 * Production AI repository powered by Firebase AI Logic (Gemini 3.8 Flash) + App Check.
 * Generates detailed student handwritten-style notes, MCQs, Q&A banks, Chapter Study Packs,
 * Daily GK quizzes, and multi-turn AI Tutor chat responses.
 */
class FirebaseAiRepository : AiRepository {

    override val isLiveAiEnabled: Boolean = true

    private val notesModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.6f
                responseMimeType = "application/json"
                responseSchema = FirebaseAiSchemas.notesSchema
            },
            systemInstruction = content {
                text(
                    "You are an expert Indian school & competitive exam topper-mentor who writes deeply detailed, " +
                        "well-organized handwritten-style notebook notes for NCERT/CBSE, State Boards, UPSC, SSC, " +
                        "Railway, and BPSC students. Always provide factually accurate, exam-scoring explanations, " +
                        "formulas, dates, examples, tables, and revision pointers."
                )
            }
        )
    }

    private val mcqModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.5f
                responseMimeType = "application/json"
                responseSchema = FirebaseAiSchemas.mcqListSchema
            }
        )
    }

    private val qnaModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.55f
                responseMimeType = "application/json"
                responseSchema = FirebaseAiSchemas.qnaListSchema
            }
        )
    }

    private val chapterPackModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.55f
                responseMimeType = "application/json"
                responseSchema = FirebaseAiSchemas.chapterStudyPackSchema
            }
        )
    }

    private val moreObjectivesModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.6f
                responseMimeType = "application/json"
                responseSchema = FirebaseAiSchemas.moreObjectiveQuestionsSchema
            }
        )
    }

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                temperature = 0.65f
            },
            systemInstruction = content {
                text(
                    "You are Smart StudyPoint's friendly AI Study Tutor for Indian school (Class 6–12) and " +
                        "competitive exam students (CBSE, NCERT, UPSC, SSC, Railway, BPSC). Give clear, accurate, " +
                        "structured explanations using numbered points, examples, and exam tips."
                )
            }
        )
    }

    override suspend fun generateNotes(request: NotesRequest): Result<GeneratedNotes> {
        val styleGuidance = when (request.notesType) {
            NotesType.HANDWRITTEN_DETAILED ->
                "Write ultra-detailed, topper-style student handwritten notebook notes! Organize 'detailedNotes' into " +
                    "clear numbered sections with headings (e.g., '1. Introduction & Core Meaning', '2. Detailed Mechanism / Explanation', " +
                    "'3. Key Rules / Formulas / Articles', '4. Step-by-Step Analysis', '5. Applications & Significance'). " +
                    "Use clean paragraphs, bullet sub-points ('• '), arrows ('->'), and student memory tricks so it reads like " +
                    "a complete class-topper handwritten register."
            NotesType.DETAILED ->
                "Write comprehensive in-depth chapter notes covering every major concept, formula, cause-effect, and subtopic."
            NotesType.SHORT_NOTES ->
                "Focus on crisp, high-density bullet summaries and rapid recall cues."
            NotesType.REVISION ->
                "Prioritize last-day exam revision notes, memory mnemonics, formulas, and high-yield takeaways."
            NotesType.EXAM_FOCUSED ->
                "Highlight probable board/competitive exam questions, examiner marking keywords, and common traps."
            NotesType.PYQ_ANALYSIS ->
                "Emphasize previous year question patterns, frequently tested facts, and scoring presentation tips."
            NotesType.COMPLETE_PACK ->
                "Produce a balanced, thorough all-in-one study pack with detailed concepts, definitions, table, flowchart, and revision."
        }

        val prompt = buildString {
            appendLine("Generate complete, detailed student study notes for:")
            appendLine("- Class / Level: ${request.classLevel}")
            appendLine("- Target Exam / Board: ${request.exam}")
            appendLine("- Subject: ${request.subject}")
            appendLine("- Chapter / Topic: ${request.topic}")
            appendLine("- Notes Format: ${request.notesType.label}")
            appendLine("- Language Requirement: ${languageInstruction(request.language)}")
            appendLine()
            appendLine("Formatting & Depth Guidelines:")
            appendLine(styleGuidance)
            appendLine("Ensure 'detailedNotes' is genuinely thorough (at least 350-550 words across 4-5 clear numbered sections separated by blank lines).")
            appendLine("Ensure 'definitions' has 4-6 accurate definitions whose terms also appear in the detailed notes so they can be highlighted.")
            appendLine("Ensure 'comparisonTable' has 3 columns and 4-5 accurate rows relevant to the topic.")
            appendLine("Ensure 'flowchartSteps' has 5 logical sequential steps.")
            appendLine("Ensure 'examples', 'importantFacts', 'examOrientedPoints', 'shortNotes', and 'quickRevision' are factually verified and exam-ready.")
            appendLine("Do not invent false facts, dates, or formulas.")
        }

        return runAiCall("generateNotes") {
            val response = withTimeout(AI_TIMEOUT_MS) {
                notesModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("AI returned an empty response while creating notes.")
            FirebaseAiSchemas.parseGeneratedNotes(text, request)
        }
    }

    override suspend fun generateMcqs(request: McqRequest): Result<List<McqQuestion>> {
        val batchLimit = request.count.coerceIn(5, 30)
        val prompt = buildString {
            appendLine("Generate $batchLimit unique, factually accurate Multiple Choice Questions (MCQs) for Indian students:")
            appendLine("- Class: ${request.classLevel}")
            appendLine("- Exam / Board: ${request.exam}")
            appendLine("- Subject: ${request.subject}")
            appendLine("- Chapter / Topic: ${request.topic}")
            appendLine("- Language: ${languageInstruction(request.language)}")
            appendLine("Rules:")
            appendLine("1. Every question must have exactly 4 distinct options and 1 unambiguously correct answer (correctIndex 0..3).")
            appendLine("2. Cover conceptual, application, assertion/fact, and exam-style questions without repetition.")
            appendLine("3. Provide a clear 1-2 sentence explanation for every answer.")
        }

        return runAiCall("generateMcqs") {
            val response = withTimeout(AI_TIMEOUT_MS) {
                mcqModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("AI returned an empty response for MCQs.")
            val parsed = FirebaseAiSchemas.parseMcqList(text, "${request.exam} • ${request.subject}")
            if (parsed.isEmpty()) throw IllegalStateException("No MCQs could be parsed from AI response.")
            parsed
        }
    }

    override suspend fun generateQnA(request: QnARequest): Result<List<QnAItem>> {
        val types = if (request.selectedTypes.isEmpty()) {
            listOf("VERY_SHORT", "SHORT", "LONG", "IMPORTANT_EXAM")
        } else {
            request.selectedTypes.map { it.name }
        }

        val prompt = buildString {
            appendLine("Generate chapter-wise exam Question & Answers for:")
            appendLine("- Class: ${request.classLevel}")
            appendLine("- Exam / Board: ${request.exam}")
            appendLine("- Subject: ${request.subject}")
            appendLine("- Chapter / Topic: ${request.topic}")
            appendLine("- Language: ${languageInstruction(request.language)}")
            appendLine("- Requested Question Types: ${types.joinToString(", ")}")
            appendLine("Generate 2 to 3 high-quality questions per requested type with complete, exam-scoring answers and 3-5 keyPoints each.")
        }

        return runAiCall("generateQnA") {
            val response = withTimeout(AI_TIMEOUT_MS) {
                qnaModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("AI returned an empty response for Q&A.")
            val parsed = FirebaseAiSchemas.parseQnAList(text)
            if (parsed.isEmpty()) throw IllegalStateException("No Q&A items were returned.")
            parsed
        }
    }

    override suspend fun sendChatMessage(
        history: List<ChatUiMessage>,
        userMessage: String,
        examContext: String,
        language: StudyLanguage
    ): Result<String> {
        return runAiCall("sendChatMessage") {
            val chatHistory = history.takeLast(8).map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }
            val chat = chatModel.startChat(history = chatHistory)
            val contextualPrompt = buildString {
                appendLine("[Context: Exam/Class = $examContext | Language = ${languageInstruction(language)}]")
                append(userMessage.trim())
            }
            val response = withTimeout(AI_TIMEOUT_MS) {
                chat.sendMessage(contextualPrompt)
            }
            val reply = response.text?.trim()
            if (reply.isNullOrBlank()) {
                throw IllegalStateException("AI Tutor returned an empty response.")
            }
            reply
        }
    }

    override suspend fun generateDailyGkQuiz(language: StudyLanguage): Result<List<McqQuestion>> {
        val prompt = buildString {
            appendLine("Generate 10 high-yield Daily General Knowledge (GK) MCQs for Indian competitive exams (UPSC, SSC, Railway, BPSC, State PCS).")
            appendLine("Cover Indian Polity, Modern History, Indian Geography, Science & Tech, Indian Economy, and Static GK/Awards.")
            appendLine("Language: ${languageInstruction(language)}")
            appendLine("Each question must have 4 options, accurate correctIndex (0..3), clear explanation, and examTag.")
        }

        return runAiCall("generateDailyGkQuiz") {
            val response = withTimeout(AI_TIMEOUT_MS) {
                mcqModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("AI returned an empty response for Daily GK Quiz.")
            val parsed = FirebaseAiSchemas.parseMcqList(text, "Daily GK")
            if (parsed.isEmpty()) throw IllegalStateException("No Daily GK questions returned.")
            parsed
        }
    }

    override suspend fun generateChapterStudyPack(request: ChapterQnARequest): Result<ChapterStudyPack> {
        val objCount = request.objectiveCount.coerceIn(10, 25)
        val prompt = buildString {
            appendLine("Create a complete 8-section Chapter-wise Objective & Subjective Study Pack in student notebook style:")
            appendLine("- Class: ${request.classLevel}")
            appendLine("- Board / Exam: ${request.boardExam}")
            appendLine("- Subject: ${request.subject}")
            appendLine("- Chapter Name: ${request.chapterName}")
            appendLine("- Language: ${languageInstruction(request.language)}")
            appendLine()
            appendLine("Section Requirements:")
            appendLine("1. chapterIntro: Engaging overview, 5 keyTopics, important definitionBox, and examWeightageTip.")
            appendLine("2. objectiveQuestions: Exactly $objCount non-repeating objective questions mixing MCQ (4 options), TRUE_FALSE (options ['True','False']), FILL_BLANK, MATCH_FOLLOWING (4 matchPairs), and ONE_WORD. Every item must have correctAnswerText and explanation.")
            appendLine("3. shortAnswers: 4 subjective questions (1-3 marks) covering VERY_SHORT, SHORT, DIFFERENCE_BETWEEN (include comparisonTable!), and DEFINE_DESCRIBE (include definitionBox!).")
            appendLine("4. longAnswers: 2 detailed 5-6 mark answers (LONG, EXPLAIN_DETAIL) including flowchartSteps and a mindMap (centralTopic + 4 branches).")
            appendLine("5. diagramQuestions: 2 diagram-based questions selecting the closest diagramKind (STOMATA, PHOTOSYNTHESIS_LEAF, HUMAN_HEART_FLOW, ELECTRIC_CIRCUIT, or CONCEPT_PROCESS) with 4-6 labels and bullet answerPoints.")
            appendLine("6. mostImportantExamQuestions: 2 high-probability board/exam questions with full answers and keywordsToHighlight.")
            appendLine("7. quickRevisionQuestions: 5 rapid one-line Q&As for last-minute revision.")
            appendLine("Ensure keywordsToHighlight actually appear verbatim inside answerParagraph/bulletPoints.")
        }

        return runAiCall("generateChapterStudyPack") {
            val response = withTimeout(AI_TIMEOUT_MS) {
                chapterPackModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("AI returned an empty response for Chapter Study Pack.")
            FirebaseAiSchemas.parseChapterStudyPack(text, request)
        }
    }

    override suspend fun generateMoreChapterQuestions(
        existingPack: ChapterStudyPack,
        additionalCount: Int
    ): Result<List<ObjectiveQuestionItem>> {
        val count = additionalCount.coerceIn(5, 15)
        val existingStems = existingPack.objectiveQuestions.takeLast(12).joinToString(" | ") { it.question }
        val prompt = buildString {
            appendLine("Generate $count NEW, non-repeating objective questions for:")
            appendLine("- Class: ${existingPack.request.classLevel} | Subject: ${existingPack.request.subject} | Chapter: ${existingPack.request.chapterName}")
            appendLine("- Board/Exam: ${existingPack.request.boardExam} | Language: ${languageInstruction(existingPack.request.language)}")
            appendLine("Mix MCQ, TRUE_FALSE, FILL_BLANK, MATCH_FOLLOWING, and ONE_WORD.")
            appendLine("Do NOT repeat any of these existing questions: $existingStems")
        }

        return runAiCall("generateMoreChapterQuestions") {
            val response = withTimeout(AI_TIMEOUT_MS) {
                moreObjectivesModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("AI returned an empty response for additional questions.")
            val root = JSONObject(text)
            val startNum = existingPack.objectiveQuestions.size + 1
            val parsed = FirebaseAiSchemas.parseObjectiveList(root.optJSONArray("questions"), startNumber = startNum)
            if (parsed.isEmpty()) throw IllegalStateException("No additional questions were returned.")
            parsed
        }
    }

    private fun languageInstruction(language: StudyLanguage): String = when (language) {
        StudyLanguage.ENGLISH -> "Clear, simple Indian academic English."
        StudyLanguage.HINDI -> "Pure, natural Hindi in Devanagari script (हिन्दी देवनागरी लिपि) with standard NCERT/exam terms."
        StudyLanguage.HINGLISH -> "Student-friendly Hinglish (natural mix of simple Hindi in Roman script and English academic terms, the way Indian toppers write class notes)."
    }

    private suspend inline fun <T> runAiCall(operation: String, crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "$operation timed out after ${AI_TIMEOUT_MS / 1000}s", e)
            Result.failure(
                IllegalStateException("AI request timed out after 60 seconds. Check your internet connection and tap Retry.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "$operation failed", e)
            Result.failure(
                IllegalStateException(e.localizedMessage ?: "AI generation failed. Please check your connection and try again.", e)
            )
        }
    }
}
