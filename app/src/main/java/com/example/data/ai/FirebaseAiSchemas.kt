package com.example.data.ai

import com.example.data.model.ChapterIntro
import com.example.data.model.ChapterQnARequest
import com.example.data.model.ChapterStudyPack
import com.example.data.model.DefinitionItem
import com.example.data.model.DiagramKind
import com.example.data.model.DiagramQuestionItem
import com.example.data.model.GeneratedNotes
import com.example.data.model.MatchPair
import com.example.data.model.McqQuestion
import com.example.data.model.MindMapBranch
import com.example.data.model.MindMapData
import com.example.data.model.NotesRequest
import com.example.data.model.ObjectiveKind
import com.example.data.model.ObjectiveQuestionItem
import com.example.data.model.QnAItem
import com.example.data.model.QnAType
import com.example.data.model.QuickRevisionQA
import com.example.data.model.SubjectiveCategory
import com.example.data.model.SubjectiveQuestionItem
import com.example.data.model.TableData
import com.google.firebase.ai.type.Schema
import org.json.JSONArray
import org.json.JSONObject

/**
 * Structured JSON schemas and direct JSONObject parsers for Firebase AI Logic responses.
 */
internal object FirebaseAiSchemas {

    private val definitionSchema = Schema.obj(
        properties = mapOf(
            "term" to Schema.string(description = "Key concept or technical term"),
            "definition" to Schema.string(description = "Clear student-friendly definition with exam precision")
        )
    )

    private val tableSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Table title"),
            "headers" to Schema.array(
                items = Schema.string(),
                description = "2 to 4 column headers"
            ),
            "rows" to Schema.array(
                items = Schema.array(items = Schema.string()),
                description = "3 to 6 comparison rows matching the header count"
            )
        )
    )

    val notesSchema: Schema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Descriptive chapter/topic notebook title"),
            "detailedNotes" to Schema.string(
                description = "Comprehensive, well-organized multi-paragraph detailed student notebook notes with numbered headings, explanations, sub-points, formulas/equations or constitutional articles where relevant, written clearly for exam preparation."
            ),
            "shortNotes" to Schema.array(
                items = Schema.string(),
                description = "6 to 8 concise high-yield bullet points for fast review"
            ),
            "easyExplanation" to Schema.string(
                description = "Simple teacher-style explanation with a relatable real-life Indian student analogy"
            ),
            "definitions" to Schema.array(
                items = definitionSchema,
                description = "4 to 6 important exam definitions"
            ),
            "importantFacts" to Schema.array(
                items = Schema.string(),
                description = "5 to 7 crisp factual points, dates, units, or key statistics"
            ),
            "comparisonTable" to tableSchema,
            "flowchartSteps" to Schema.array(
                items = Schema.string(),
                description = "4 to 6 ordered sequential steps explaining the core mechanism, timeline, or problem-solving process"
            ),
            "examples" to Schema.array(
                items = Schema.string(),
                description = "3 to 5 clear worked examples, reactions, or real-world illustrations"
            ),
            "examOrientedPoints" to Schema.array(
                items = Schema.string(),
                description = "4 to 6 V.V. IMP high-weightage exam scoring tips and common mistakes to avoid"
            ),
            "quickRevision" to Schema.array(
                items = Schema.string(),
                description = "5 to 7 rapid last-minute revision one-liners"
            )
        )
    )

    private val mcqItemSchema = Schema.obj(
        properties = mapOf(
            "question" to Schema.string(description = "Clear, accurate exam-standard question"),
            "options" to Schema.array(
                items = Schema.string(),
                description = "Exactly 4 distinct options"
            ),
            "correctIndex" to Schema.integer(description = "0-based index (0, 1, 2, or 3) of the correct option"),
            "explanation" to Schema.string(description = "Concise factual explanation of why the answer is correct"),
            "examTag" to Schema.string(description = "Relevant exam or chapter weightage tag")
        )
    )

    val mcqListSchema: Schema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = mcqItemSchema,
                description = "List of unique multiple choice questions"
            )
        )
    )

    val qnaListSchema: Schema = Schema.obj(
        properties = mapOf(
            "items" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "type" to Schema.enumeration(
                            values = QnAType.entries.map { it.name },
                            description = "Question answer category"
                        ),
                        "question" to Schema.string(description = "Exam-oriented question"),
                        "answer" to Schema.string(description = "Complete, well-structured student-friendly answer"),
                        "keyPoints" to Schema.array(
                            items = Schema.string(),
                            description = "3 to 5 scoring keywords or takeaway bullets"
                        )
                    )
                ),
                description = "List of generated question-answer items"
            )
        )
    )

    private val objectiveItemSchema = Schema.obj(
        properties = mapOf(
            "kind" to Schema.enumeration(
                values = ObjectiveKind.entries.map { it.name },
                description = "Objective question format"
            ),
            "question" to Schema.string(description = "Question stem"),
            "options" to Schema.array(
                items = Schema.string(),
                description = "4 options for MCQ, [True, False] for TRUE_FALSE, or empty for other kinds"
            ),
            "matchPairs" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "leftItem" to Schema.string(),
                        "rightItem" to Schema.string()
                    )
                ),
                description = "4 matching pairs when kind is MATCH_FOLLOWING, empty otherwise"
            ),
            "correctOptionIndex" to Schema.integer(description = "0-based correct option index for MCQ/TRUE_FALSE"),
            "correctAnswerText" to Schema.string(description = "Exact correct answer text"),
            "explanation" to Schema.string(description = "Clear explanation of the correct answer"),
            "marks" to Schema.integer(description = "Marks value, usually 1")
        ),
        optionalProperties = listOf("options", "matchPairs", "correctOptionIndex", "marks")
    )

    val moreObjectiveQuestionsSchema: Schema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = objectiveItemSchema,
                description = "Additional non-repeating chapter objective questions"
            )
        )
    )

    private val mindMapSchema = Schema.obj(
        properties = mapOf(
            "centralTopic" to Schema.string(),
            "branches" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(),
                        "points" to Schema.array(items = Schema.string())
                    )
                )
            )
        )
    )

    private val subjectiveItemSchema = Schema.obj(
        properties = mapOf(
            "category" to Schema.enumeration(
                values = SubjectiveCategory.entries.map { it.name },
                description = "Subjective question category"
            ),
            "marks" to Schema.integer(description = "Marks allocation between 1 and 6"),
            "question" to Schema.string(),
            "answerHeading" to Schema.string(description = "Red notebook heading summarizing the answer"),
            "answerParagraph" to Schema.string(description = "Detailed handwritten-style main answer paragraph"),
            "bulletPoints" to Schema.array(items = Schema.string(), description = "Structured exam bullet points"),
            "keywordsToHighlight" to Schema.array(items = Schema.string(), description = "3 to 6 key exam terms present in the answer to highlight in yellow"),
            "examples" to Schema.array(items = Schema.string()),
            "definitionBox" to definitionSchema,
            "comparisonTable" to tableSchema,
            "flowchartSteps" to Schema.array(items = Schema.string()),
            "mindMap" to mindMapSchema
        ),
        optionalProperties = listOf("examples", "definitionBox", "comparisonTable", "flowchartSteps", "mindMap")
    )

    val chapterStudyPackSchema: Schema = Schema.obj(
        properties = mapOf(
            "chapterIntro" to Schema.obj(
                properties = mapOf(
                    "overview" to Schema.string(),
                    "keyTopics" to Schema.array(items = Schema.string()),
                    "definitionBox" to definitionSchema,
                    "examWeightageTip" to Schema.string()
                )
            ),
            "objectiveQuestions" to Schema.array(items = objectiveItemSchema),
            "shortAnswers" to Schema.array(items = subjectiveItemSchema),
            "longAnswers" to Schema.array(items = subjectiveItemSchema),
            "diagramQuestions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "diagramKind" to Schema.enumeration(
                            values = DiagramKind.entries.map { it.name },
                            description = "Best matching diagram visual template"
                        ),
                        "diagramCaption" to Schema.string(),
                        "labels" to Schema.array(items = Schema.string()),
                        "question" to Schema.string(),
                        "answerPoints" to Schema.array(items = Schema.string()),
                        "marks" to Schema.integer()
                    )
                )
            ),
            "mostImportantExamQuestions" to Schema.array(items = subjectiveItemSchema),
            "quickRevisionQuestions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(),
                        "answer" to Schema.string()
                    )
                )
            )
        )
    )

    // --- Direct JSON Parsers ---

    fun parseGeneratedNotes(jsonString: String, request: NotesRequest): GeneratedNotes {
        val obj = JSONObject(jsonString)
        val defs = parseDefinitions(obj.optJSONArray("definitions"))
        val table = parseTable(obj.optJSONObject("comparisonTable"), request.topic)

        return GeneratedNotes(
            title = obj.optString("title").ifBlank { "${request.topic} — ${request.notesType.label}" },
            classLevel = request.classLevel,
            exam = request.exam,
            subject = request.subject,
            topic = request.topic,
            language = request.language,
            notesType = request.notesType,
            detailedNotes = obj.optString("detailedNotes").trim(),
            shortNotes = obj.optJSONArray("shortNotes").toStringList(),
            easyExplanation = obj.optString("easyExplanation").trim(),
            definitions = defs,
            importantFacts = obj.optJSONArray("importantFacts").toStringList(),
            comparisonTable = table,
            flowchartSteps = obj.optJSONArray("flowchartSteps").toStringList(),
            examples = obj.optJSONArray("examples").toStringList(),
            examOrientedPoints = obj.optJSONArray("examOrientedPoints").toStringList(),
            quickRevision = obj.optJSONArray("quickRevision").toStringList(),
            isSamplePreview = false
        )
    }

    fun parseMcqList(jsonString: String, defaultTag: String): List<McqQuestion> {
        val root = JSONObject(jsonString)
        val arr = root.optJSONArray("questions") ?: JSONArray()
        return (0 until arr.length()).mapNotNull { idx ->
            val item = arr.optJSONObject(idx) ?: return@mapNotNull null
            val options = item.optJSONArray("options").toStringList()
            if (options.size < 2) return@mapNotNull null
            val paddedOptions = if (options.size >= 4) options.take(4) else options + List(4 - options.size) { "None of the above" }
            val correctIndex = item.optInt("correctIndex", 0).coerceIn(0, paddedOptions.lastIndex)
            McqQuestion(
                id = idx + 1,
                question = item.optString("question").trim(),
                options = paddedOptions,
                correctIndex = correctIndex,
                explanation = item.optString("explanation").trim(),
                examTag = item.optString("examTag").ifBlank { defaultTag }
            )
        }
    }

    fun parseQnAList(jsonString: String): List<QnAItem> {
        val root = JSONObject(jsonString)
        val arr = root.optJSONArray("items") ?: JSONArray()
        return (0 until arr.length()).mapNotNull { idx ->
            val item = arr.optJSONObject(idx) ?: return@mapNotNull null
            val typeName = item.optString("type")
            val qnaType = QnAType.entries.firstOrNull { it.name.equals(typeName, ignoreCase = true) }
                ?: QnAType.SHORT
            QnAItem(
                id = idx + 1,
                type = qnaType,
                question = item.optString("question").trim(),
                answer = item.optString("answer").trim(),
                keyPoints = item.optJSONArray("keyPoints").toStringList()
            )
        }
    }

    fun parseObjectiveList(arr: JSONArray?, startNumber: Int = 1): List<ObjectiveQuestionItem> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { idx ->
            val item = arr.optJSONObject(idx) ?: return@mapNotNull null
            val kind = ObjectiveKind.entries.firstOrNull {
                it.name.equals(item.optString("kind"), ignoreCase = true)
            } ?: ObjectiveKind.MCQ
            val rawOptions = item.optJSONArray("options").toStringList()
            val options = when (kind) {
                ObjectiveKind.MCQ -> if (rawOptions.size >= 4) rawOptions.take(4) else rawOptions
                ObjectiveKind.TRUE_FALSE -> if (rawOptions.size >= 2) rawOptions.take(2) else listOf("True", "False")
                else -> rawOptions
            }
            val matchPairsArr = item.optJSONArray("matchPairs")
            val matchPairs = if (matchPairsArr != null) {
                (0 until matchPairsArr.length()).mapNotNull { pIdx ->
                    val pObj = matchPairsArr.optJSONObject(pIdx) ?: return@mapNotNull null
                    MatchPair(
                        leftItem = pObj.optString("leftItem").trim(),
                        rightItem = pObj.optString("rightItem").trim()
                    )
                }
            } else {
                emptyList()
            }
            val safeCorrectIndex = if (options.isNotEmpty()) {
                item.optInt("correctOptionIndex", 0).coerceIn(0, options.lastIndex)
            } else {
                0
            }
            ObjectiveQuestionItem(
                number = startNumber + idx,
                kind = kind,
                question = item.optString("question").trim(),
                options = options,
                matchPairs = matchPairs,
                correctOptionIndex = safeCorrectIndex,
                correctAnswerText = item.optString("correctAnswerText").trim(),
                explanation = item.optString("explanation").trim(),
                marks = item.optInt("marks", 1).coerceAtLeast(1)
            )
        }
    }

    fun parseChapterStudyPack(jsonString: String, request: ChapterQnARequest): ChapterStudyPack {
        val root = JSONObject(jsonString)
        val introObj = root.optJSONObject("chapterIntro") ?: JSONObject()
        val introDefObj = introObj.optJSONObject("definitionBox")
        val intro = ChapterIntro(
            overview = introObj.optString("overview").trim(),
            keyTopics = introObj.optJSONArray("keyTopics").toStringList(),
            definitionBox = DefinitionItem(
                term = introDefObj?.optString("term").orEmpty().ifBlank { request.chapterName },
                definition = introDefObj?.optString("definition").orEmpty()
            ),
            examWeightageTip = introObj.optString("examWeightageTip").trim()
        )

        val objectives = parseObjectiveList(root.optJSONArray("objectiveQuestions"), startNumber = 1)
        val shortAnswers = parseSubjectiveList(root.optJSONArray("shortAnswers"), request.chapterName)
        val longAnswers = parseSubjectiveList(root.optJSONArray("longAnswers"), request.chapterName)
        val importantAnswers = parseSubjectiveList(root.optJSONArray("mostImportantExamQuestions"), request.chapterName)

        val diagArr = root.optJSONArray("diagramQuestions") ?: JSONArray()
        val diagrams = (0 until diagArr.length()).mapNotNull { idx ->
            val d = diagArr.optJSONObject(idx) ?: return@mapNotNull null
            val kind = DiagramKind.entries.firstOrNull {
                it.name.equals(d.optString("diagramKind"), ignoreCase = true)
            } ?: DiagramKind.CONCEPT_PROCESS
            DiagramQuestionItem(
                number = idx + 1,
                diagramKind = kind,
                diagramCaption = d.optString("diagramCaption").ifBlank { kind.title },
                labels = d.optJSONArray("labels").toStringList(),
                question = d.optString("question").trim(),
                answerPoints = d.optJSONArray("answerPoints").toStringList(),
                marks = d.optInt("marks", 3).coerceIn(1, 6)
            )
        }

        val revArr = root.optJSONArray("quickRevisionQuestions") ?: JSONArray()
        val revisions = (0 until revArr.length()).mapNotNull { idx ->
            val r = revArr.optJSONObject(idx) ?: return@mapNotNull null
            QuickRevisionQA(
                number = idx + 1,
                question = r.optString("question").trim(),
                answer = r.optString("answer").trim()
            )
        }

        return ChapterStudyPack(
            request = request,
            chapterIntro = intro,
            objectiveQuestions = objectives,
            shortAnswers = shortAnswers,
            longAnswers = longAnswers,
            diagramQuestions = diagrams,
            mostImportantExamQuestions = importantAnswers,
            quickRevisionQuestions = revisions,
            isSamplePreview = false
        )
    }

    private fun parseSubjectiveList(arr: JSONArray?, fallbackTopic: String): List<SubjectiveQuestionItem> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { idx ->
            val item = arr.optJSONObject(idx) ?: return@mapNotNull null
            val category = SubjectiveCategory.entries.firstOrNull {
                it.name.equals(item.optString("category"), ignoreCase = true)
            } ?: SubjectiveCategory.SHORT

            val defObj = item.optJSONObject("definitionBox")
            val defBox = if (defObj != null && defObj.optString("term").isNotBlank()) {
                DefinitionItem(
                    term = defObj.optString("term").trim(),
                    definition = defObj.optString("definition").trim()
                )
            } else {
                null
            }

            val tblObj = item.optJSONObject("comparisonTable")
            val table = if (tblObj != null && (tblObj.optJSONArray("headers")?.length() ?: 0) >= 2) {
                parseTable(tblObj, fallbackTopic)
            } else {
                null
            }

            val mmObj = item.optJSONObject("mindMap")
            val mindMap = if (mmObj != null && mmObj.optString("centralTopic").isNotBlank()) {
                val bArr = mmObj.optJSONArray("branches") ?: JSONArray()
                val branches = (0 until bArr.length()).mapNotNull { bIdx ->
                    val b = bArr.optJSONObject(bIdx) ?: return@mapNotNull null
                    MindMapBranch(
                        title = b.optString("title").trim(),
                        points = b.optJSONArray("points").toStringList()
                    )
                }
                if (branches.isNotEmpty()) {
                    MindMapData(
                        centralTopic = mmObj.optString("centralTopic").trim(),
                        branches = branches
                    )
                } else null
            } else {
                null
            }

            SubjectiveQuestionItem(
                number = idx + 1,
                category = category,
                marks = item.optInt("marks", 3).coerceIn(1, 6),
                question = item.optString("question").trim(),
                answerHeading = item.optString("answerHeading").ifBlank { category.label },
                answerParagraph = item.optString("answerParagraph").trim(),
                bulletPoints = item.optJSONArray("bulletPoints").toStringList(),
                keywordsToHighlight = item.optJSONArray("keywordsToHighlight").toStringList(),
                examples = item.optJSONArray("examples").toStringList(),
                definitionBox = defBox,
                comparisonTable = table,
                flowchartSteps = item.optJSONArray("flowchartSteps").toStringList(),
                mindMap = mindMap
            )
        }
    }

    private fun parseDefinitions(arr: JSONArray?): List<DefinitionItem> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { idx ->
            val obj = arr.optJSONObject(idx) ?: return@mapNotNull null
            val term = obj.optString("term").trim()
            val def = obj.optString("definition").trim()
            if (term.isBlank() && def.isBlank()) null else DefinitionItem(term, def)
        }
    }

    private fun parseTable(obj: JSONObject?, fallbackTitle: String): TableData {
        if (obj == null) return TableData("Key Comparison — $fallbackTitle", emptyList(), emptyList())
        val headers = obj.optJSONArray("headers").toStringList()
        val rowsArr = obj.optJSONArray("rows") ?: JSONArray()
        val rows = (0 until rowsArr.length()).mapNotNull { idx ->
            val row = rowsArr.optJSONArray(idx).toStringList()
            if (row.isEmpty()) null else row
        }
        return TableData(
            title = obj.optString("title").ifBlank { "Key Comparison — $fallbackTitle" },
            headers = headers,
            rows = rows
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { idx ->
            optString(idx).trim().takeIf { it.isNotEmpty() }
        }
    }
}
