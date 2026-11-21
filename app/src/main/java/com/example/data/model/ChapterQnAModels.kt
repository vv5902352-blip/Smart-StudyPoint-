package com.example.data.model

enum class ObjectiveKind(val label: String, val shortTag: String) {
    MCQ("Multiple Choice (MCQ)", "MCQ"),
    TRUE_FALSE("True or False", "T / F"),
    FILL_BLANK("Fill in the Blanks", "Fill Blank"),
    MATCH_FOLLOWING("Match the Following", "Match"),
    ONE_WORD("One Word Answer", "1-Word")
}

enum class SubjectiveCategory(val label: String, val marksLabel: String, val wordLimit: String) {
    VERY_SHORT("Very Short Answer", "1 Mark", "15–25 words"),
    SHORT("Short Answer", "2–3 Marks", "45–75 words"),
    LONG("Long Answer", "4–6 Marks", "120–160 words"),
    IMPORTANT("Important Exam Q", "3–5 Marks", "80–120 words"),
    EXPLAIN_DETAIL("Explain in Detail", "5 Marks", "130–160 words"),
    DIFFERENCE_BETWEEN("Difference Between", "3 Marks", "Tabular (4–5 points)"),
    DEFINE_DESCRIBE("Define & Describe", "2–3 Marks", "50–80 words")
}

enum class MarksFilter(val label: String) {
    ALL("All Marks"),
    ONE_MARK("1 Mark"),
    TWO_THREE_MARKS("2–3 Marks"),
    FOUR_SIX_MARKS("4–6 Marks"),
    DIFFERENCES("Differences")
}

enum class DiagramKind(val title: String) {
    STOMATA("Open & Closed Stomatal Pore"),
    PHOTOSYNTHESIS_LEAF("Cross-Section of Leaf & Photosynthesis"),
    HUMAN_HEART_FLOW("Human Double Circulation Pathway"),
    ELECTRIC_CIRCUIT("Ohm's Law Simple Electric Circuit"),
    CONCEPT_PROCESS("Structured Chapter Concept Diagram")
}

data class ChapterQnARequest(
    val classLevel: String,
    val subject: String,
    val chapterName: String,
    val boardExam: String,
    val language: StudyLanguage,
    val objectiveCount: Int
)

data class ChapterIntro(
    val overview: String,
    val keyTopics: List<String>,
    val definitionBox: DefinitionItem,
    val examWeightageTip: String
)

data class MatchPair(
    val leftItem: String,
    val rightItem: String
)

data class ObjectiveQuestionItem(
    val number: Int,
    val kind: ObjectiveKind,
    val question: String,
    val options: List<String> = emptyList(), // 4 options for MCQ, ["True", "False"] for T/F, etc.
    val matchPairs: List<MatchPair> = emptyList(),
    val correctOptionIndex: Int = 0,
    val correctAnswerText: String,
    val explanation: String,
    val marks: Int = 1
)

data class MindMapBranch(
    val title: String,
    val points: List<String>
)

data class MindMapData(
    val centralTopic: String,
    val branches: List<MindMapBranch>
)

data class SubjectiveQuestionItem(
    val number: Int,
    val category: SubjectiveCategory,
    val marks: Int,
    val question: String,
    val answerHeading: String,
    val answerParagraph: String,
    val bulletPoints: List<String>,
    val keywordsToHighlight: List<String>,
    val examples: List<String> = emptyList(),
    val definitionBox: DefinitionItem? = null,
    val comparisonTable: TableData? = null,
    val flowchartSteps: List<String> = emptyList(),
    val mindMap: MindMapData? = null
)

data class DiagramQuestionItem(
    val number: Int,
    val diagramKind: DiagramKind,
    val diagramCaption: String,
    val labels: List<String>,
    val question: String,
    val answerPoints: List<String>,
    val marks: Int = 3
)

data class QuickRevisionQA(
    val number: Int,
    val question: String,
    val answer: String
)

data class ChapterStudyPack(
    val request: ChapterQnARequest,
    val chapterIntro: ChapterIntro,
    val objectiveQuestions: List<ObjectiveQuestionItem>,
    val shortAnswers: List<SubjectiveQuestionItem>,
    val longAnswers: List<SubjectiveQuestionItem>,
    val diagramQuestions: List<DiagramQuestionItem>,
    val mostImportantExamQuestions: List<SubjectiveQuestionItem>,
    val quickRevisionQuestions: List<QuickRevisionQA>,
    val isSamplePreview: Boolean = true
) {
    fun toPlainText(): String = buildString {
        appendLine("SMART STUDYPOINT — CHAPTER-WISE OBJECTIVE & SUBJECTIVE Q&A")
        appendLine("Chapter: ${request.chapterName}")
        appendLine("Class: ${request.classLevel} | Subject: ${request.subject} | Board/Exam: ${request.boardExam} | Language: ${request.language.label}")
        appendLine("============================================================")
        appendLine("\n1. CHAPTER INTRODUCTION")
        appendLine(chapterIntro.overview)
        appendLine("Definition — ${chapterIntro.definitionBox.term}: ${chapterIntro.definitionBox.definition}")
        appendLine("Key Topics: ${chapterIntro.keyTopics.joinToString(", ")}")

        appendLine("\n2. IMPORTANT OBJECTIVE QUESTIONS (${objectiveQuestions.size})")
        objectiveQuestions.forEach { q ->
            appendLine("Q${q.number}. [${q.kind.shortTag}] ${q.question}")
            if (q.options.isNotEmpty()) {
                val letters = listOf("A", "B", "C", "D")
                q.options.forEachIndexed { idx, opt ->
                    appendLine("   (${letters.getOrElse(idx) { "${idx + 1}" }}) $opt")
                }
            }
            if (q.matchPairs.isNotEmpty()) {
                q.matchPairs.forEachIndexed { idx, pair ->
                    appendLine("   ${idx + 1}. ${pair.leftItem}  <-->  ${pair.rightItem}")
                }
            }
            appendLine("   Answer: ${q.correctAnswerText}")
            appendLine("   Explanation: ${q.explanation}")
        }

        appendLine("\n3. OBJECTIVE ANSWER KEY")
        objectiveQuestions.forEach { q ->
            appendLine("Q${q.number}: ${q.correctAnswerText}")
        }

        appendLine("\n4. SHORT ANSWER QUESTIONS (1–3 MARKS)")
        shortAnswers.forEach { sq ->
            appendLine("Q${sq.number}. [${sq.category.marksLabel} | ${sq.category.wordLimit}] ${sq.question}")
            appendLine("Ans (${sq.answerHeading}): ${sq.answerParagraph}")
            sq.bulletPoints.forEach { appendLine("  • $it") }
            sq.comparisonTable?.let { tbl ->
                appendLine("  Table: ${tbl.title}")
                appendLine("  ${tbl.headers.joinToString(" | ")}")
                tbl.rows.forEach { appendLine("  ${it.joinToString(" | ")}") }
            }
            if (sq.examples.isNotEmpty()) appendLine("  Examples: ${sq.examples.joinToString("; ")}")
            appendLine()
        }

        appendLine("\n5. LONG ANSWER QUESTIONS (4–6 MARKS)")
        longAnswers.forEach { lq ->
            appendLine("Q${lq.number}. [${lq.category.marksLabel} | ${lq.category.wordLimit}] ${lq.question}")
            appendLine("Ans (${lq.answerHeading}): ${lq.answerParagraph}")
            lq.bulletPoints.forEach { appendLine("  • $it") }
            if (lq.flowchartSteps.isNotEmpty()) {
                appendLine("  Flowchart: ${lq.flowchartSteps.joinToString(" -> ")}")
            }
            if (lq.examples.isNotEmpty()) appendLine("  Examples: ${lq.examples.joinToString("; ")}")
            appendLine()
        }

        appendLine("\n6. DIAGRAM-BASED QUESTIONS")
        diagramQuestions.forEach { dq ->
            appendLine("Q${dq.number}. [${dq.marks} Marks] ${dq.question} (Diagram: ${dq.diagramCaption})")
            appendLine("  Key Labels: ${dq.labels.joinToString(", ")}")
            dq.answerPoints.forEach { appendLine("  • $it") }
        }

        appendLine("\n7. MOST IMPORTANT EXAM QUESTIONS")
        mostImportantExamQuestions.forEach { iq ->
            appendLine("Q${iq.number}. [${iq.marks} Marks] ${iq.question}")
            appendLine("  Ans: ${iq.answerParagraph}")
            iq.bulletPoints.forEach { appendLine("  • $it") }
        }

        appendLine("\n8. QUICK REVISION QUESTIONS")
        quickRevisionQuestions.forEach { rq ->
            appendLine("Q${rq.number}. ${rq.question}")
            appendLine("  Ans: ${rq.answer}")
        }

        appendLine("\n© Smart StudyPoint — Your Smart Partner in Exam Preparation.")
    }
}
