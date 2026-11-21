package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.data.model.CatalogType
import com.example.data.model.DefinitionItem
import com.example.data.model.GeneratedNotes
import com.example.data.model.NoteCategory
import com.example.data.model.NotesType
import com.example.data.model.StudyLanguage
import com.example.data.model.TableData

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val classLevel: String,
    val exam: String,
    val subject: String,
    val topic: String,
    val language: String,
    val notesType: String,
    val category: String,
    val detailedNotes: String,
    val shortNotesJoined: String,
    val easyExplanation: String,
    val definitionsJoined: String,
    val importantFactsJoined: String,
    val tableTitle: String,
    val tableHeadersJoined: String,
    val tableRowsJoined: String,
    val flowchartStepsJoined: String,
    val examplesJoined: String,
    val examPointsJoined: String,
    val quickRevisionJoined: String,
    val isFavorite: Boolean = false,
    val isPublishedByAdmin: Boolean = false,
    val isSamplePreview: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val exam: String,
    val subject: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val isDailyGk: Boolean = false,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // CatalogType.name
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "managed_users")
data class ManagedUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val email: String,
    val role: String, // "Student", "Educator", "Admin"
    val targetExam: String,
    val isActive: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val isUser: Boolean,
    val text: String,
    val isSamplePreview: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_questions")
data class FavoriteQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chapterName: String,
    val subject: String,
    val classLevel: String,
    val questionType: String,
    val marksLabel: String,
    val questionText: String,
    val answerText: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val subject: String,
    val examTag: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastStudiedAt: Long = 0L
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val deckId: Long,
    val term: String,
    val definition: String,
    val hint: String = "",
    val isMastered: Boolean = false,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class FlashcardDeckWithCards(
    @Embedded val deck: FlashcardDeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId"
    )
    val cards: List<FlashcardEntity>
) {
    val totalCards: Int get() = cards.size
    val masteredCount: Int get() = cards.count { it.isMastered }
    val unmasteredCount: Int get() = cards.count { !it.isMastered }
    val masteryPercent: Int
        get() = if (cards.isEmpty()) 0 else (masteredCount * 100) / cards.size
}

private const val LINE_SEP = "\n@@\n"
private const val PAIR_SEP = ":::"
private const val CELL_SEP = "|||"

fun GeneratedNotes.toEntity(
    category: NoteCategory = NoteCategory.AI_NOTES,
    isFavorite: Boolean = false,
    isPublishedByAdmin: Boolean = false
): StudyNoteEntity = StudyNoteEntity(
    title = title,
    classLevel = classLevel,
    exam = exam,
    subject = subject,
    topic = topic,
    language = language.name,
    notesType = notesType.name,
    category = category.name,
    detailedNotes = detailedNotes,
    shortNotesJoined = shortNotes.joinToString(LINE_SEP),
    easyExplanation = easyExplanation,
    definitionsJoined = definitions.joinToString(LINE_SEP) { "${it.term}$PAIR_SEP${it.definition}" },
    importantFactsJoined = importantFacts.joinToString(LINE_SEP),
    tableTitle = comparisonTable.title,
    tableHeadersJoined = comparisonTable.headers.joinToString(CELL_SEP),
    tableRowsJoined = comparisonTable.rows.joinToString(LINE_SEP) { it.joinToString(CELL_SEP) },
    flowchartStepsJoined = flowchartSteps.joinToString(LINE_SEP),
    examplesJoined = examples.joinToString(LINE_SEP),
    examPointsJoined = examOrientedPoints.joinToString(LINE_SEP),
    quickRevisionJoined = quickRevision.joinToString(LINE_SEP),
    isFavorite = isFavorite,
    isPublishedByAdmin = isPublishedByAdmin,
    isSamplePreview = isSamplePreview
)

fun StudyNoteEntity.toGeneratedNotes(): GeneratedNotes {
    val lang = StudyLanguage.entries.firstOrNull { it.name == language } ?: StudyLanguage.ENGLISH
    val nType = NotesType.entries.firstOrNull { it.name == notesType } ?: NotesType.COMPLETE_PACK

    fun splitLines(raw: String): List<String> =
        if (raw.isBlank()) emptyList() else raw.split(LINE_SEP).filter { it.isNotBlank() }

    val defs = splitLines(definitionsJoined).map { entry ->
        val parts = entry.split(PAIR_SEP)
        DefinitionItem(
            term = parts.getOrElse(0) { "Concept" },
            definition = parts.getOrElse(1) { entry }
        )
    }

    val headers = if (tableHeadersJoined.isBlank()) emptyList() else tableHeadersJoined.split(CELL_SEP)
    val rows = splitLines(tableRowsJoined).map { it.split(CELL_SEP) }

    return GeneratedNotes(
        title = title,
        classLevel = classLevel,
        exam = exam,
        subject = subject,
        topic = topic,
        language = lang,
        notesType = nType,
        detailedNotes = detailedNotes,
        shortNotes = splitLines(shortNotesJoined),
        easyExplanation = easyExplanation,
        definitions = defs,
        importantFacts = splitLines(importantFactsJoined),
        comparisonTable = TableData(
            title = tableTitle.ifBlank { "Key Comparison Table" },
            headers = headers,
            rows = rows
        ),
        flowchartSteps = splitLines(flowchartStepsJoined),
        examples = splitLines(examplesJoined),
        examOrientedPoints = splitLines(examPointsJoined),
        quickRevision = splitLines(quickRevisionJoined),
        isSamplePreview = isSamplePreview
    )
}

fun StudyNoteEntity.noteCategory(): NoteCategory =
    NoteCategory.entries.firstOrNull { it.name == category } ?: NoteCategory.AI_NOTES

fun CatalogItemEntity.catalogType(): CatalogType =
    CatalogType.entries.firstOrNull { it.name == type } ?: CatalogType.SUBJECT
