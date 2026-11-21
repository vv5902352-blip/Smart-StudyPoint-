package com.example.data.model

enum class StudyLanguage(val label: String, val localeTag: String) {
    ENGLISH("English", "en-IN"),
    HINDI("Hindi (हिन्दी)", "hi-IN"),
    HINGLISH("Hinglish", "en-IN")
}

enum class NotesType(val label: String) {
    HANDWRITTEN_DETAILED("Handwritten Detailed Notes"),
    COMPLETE_PACK("Complete Study Pack"),
    DETAILED("Detailed Notes"),
    SHORT_NOTES("Short Notes"),
    REVISION("Quick Revision Notes"),
    EXAM_FOCUSED("Exam-Oriented Booster"),
    PYQ_ANALYSIS("Previous Year Analysis")
}

enum class NoteCategory(val label: String) {
    AI_NOTES("AI Notes"),
    SHORT_NOTES("Short Notes"),
    IMPORTANT_QUESTIONS("Important Qs"),
    REVISION_NOTES("Revision Notes"),
    PYQ("PYQs"),
    ADMIN_PUBLISHED("Published")
}

enum class QnAType(val label: String, val marksHint: String) {
    VERY_SHORT("Very Short Answer", "1–2 Marks"),
    SHORT("Short Answer", "3–4 Marks"),
    LONG("Long Answer", "5–8 Marks"),
    OBJECTIVE("Objective Questions", "1 Mark"),
    SUBJECTIVE("Subjective Questions", "Descriptive"),
    IMPORTANT_EXAM("Important Exam Questions", "High Weightage")
}

enum class CatalogType(val label: String) {
    CLASS_LEVEL("Class"),
    EXAM("Exam"),
    SUBJECT("Subject")
}

data class NotesRequest(
    val classLevel: String,
    val exam: String,
    val subject: String,
    val topic: String,
    val language: StudyLanguage,
    val notesType: NotesType
)

data class DefinitionItem(
    val term: String,
    val definition: String
)

data class TableData(
    val title: String,
    val headers: List<String>,
    val rows: List<List<String>>
)

data class GeneratedNotes(
    val title: String,
    val classLevel: String,
    val exam: String,
    val subject: String,
    val topic: String,
    val language: StudyLanguage,
    val notesType: NotesType,
    val detailedNotes: String,
    val shortNotes: List<String>,
    val easyExplanation: String,
    val definitions: List<DefinitionItem>,
    val importantFacts: List<String>,
    val comparisonTable: TableData,
    val flowchartSteps: List<String>,
    val examples: List<String>,
    val examOrientedPoints: List<String>,
    val quickRevision: List<String>,
    val isSamplePreview: Boolean = true
) {
    fun toPlainText(): String = buildString {
        appendLine("SMART STUDYPOINT — STUDY NOTES")
        appendLine("Title: $title")
        appendLine("Class: $classLevel | Exam: $exam | Subject: $subject | Language: ${language.label}")
        appendLine("Type: ${notesType.label}")
        appendLine("--------------------------------------------------")
        appendLine("\n1. DETAILED NOTES\n$detailedNotes")
        appendLine("\n2. SHORT NOTES")
        shortNotes.forEach { appendLine("• $it") }
        appendLine("\n3. EASY EXPLANATION\n$easyExplanation")
        appendLine("\n4. IMPORTANT DEFINITIONS")
        definitions.forEach { appendLine("• ${it.term}: ${it.definition}") }
        appendLine("\n5. IMPORTANT FACTS")
        importantFacts.forEach { appendLine("• $it") }
        appendLine("\n6. ${comparisonTable.title.uppercase()}")
        appendLine(comparisonTable.headers.joinToString(" | "))
        comparisonTable.rows.forEach { appendLine(it.joinToString(" | ")) }
        appendLine("\n7. FLOWCHART / STEP-BY-STEP PROCESS")
        flowchartSteps.forEachIndexed { i, step -> appendLine("${i + 1}. $step") }
        appendLine("\n8. EXAMPLES")
        examples.forEach { appendLine("• $it") }
        appendLine("\n9. EXAM-ORIENTED POINTS")
        examOrientedPoints.forEach { appendLine("• $it") }
        appendLine("\n10. QUICK REVISION")
        quickRevision.forEach { appendLine("• $it") }
        appendLine("\n© Smart StudyPoint — Your Smart Partner in Exam Preparation.")
    }
}

data class McqRequest(
    val classLevel: String,
    val exam: String,
    val subject: String,
    val topic: String,
    val language: StudyLanguage,
    val count: Int
)

data class McqQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val examTag: String = ""
)

data class QnARequest(
    val classLevel: String,
    val exam: String,
    val subject: String,
    val topic: String,
    val language: StudyLanguage,
    val selectedTypes: Set<QnAType>
)

data class QnAItem(
    val id: Int,
    val type: QnAType,
    val question: String,
    val answer: String,
    val keyPoints: List<String>
)

data class ChatUiMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long,
    val isSamplePreview: Boolean = false
)

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
