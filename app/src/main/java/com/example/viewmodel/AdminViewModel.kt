package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CatalogItemEntity
import com.example.data.local.ManagedUserEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.noteCategory
import com.example.data.model.CatalogType
import com.example.data.model.GeneratedNotes
import com.example.data.model.NoteCategory
import com.example.data.model.NotesType
import com.example.data.model.StudyLanguage
import com.example.data.model.TableData
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminAnalytics(
    val totalNotes: Int = 0,
    val publishedNotes: Int = 0,
    val favoriteNotes: Int = 0,
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalQuizzesTaken: Int = 0,
    val averageQuizAccuracy: Int = 0,
    val notesByCategory: Map<NoteCategory, Int> = emptyMap()
)

class AdminViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    val allNotes: StateFlow<List<StudyNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val catalogItems: StateFlow<List<CatalogItemEntity>> = repository.catalogItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val managedUsers: StateFlow<List<ManagedUserEntity>> = repository.managedUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val analytics: StateFlow<AdminAnalytics> = combine(
        repository.allNotes,
        repository.managedUsers,
        repository.quizAttempts
    ) { notes, users, attempts ->
        val totalQ = attempts.sumOf { it.totalQuestions }
        val totalCorrect = attempts.sumOf { it.correctAnswers }
        val avgAcc = if (totalQ > 0) (totalCorrect * 100) / totalQ else 0
        val catCounts = NoteCategory.entries.associateWith { cat ->
            notes.count { it.noteCategory() == cat }
        }
        AdminAnalytics(
            totalNotes = notes.size,
            publishedNotes = notes.count { it.isPublishedByAdmin },
            favoriteNotes = notes.count { it.isFavorite },
            totalUsers = users.size,
            activeUsers = users.count { it.isActive },
            totalQuizzesTaken = attempts.size,
            averageQuizAccuracy = avgAcc,
            notesByCategory = catCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminAnalytics())

    fun publishImportantNote(
        title: String,
        classLevel: String,
        exam: String,
        subject: String,
        category: NoteCategory,
        language: StudyLanguage,
        content: String,
        quickBullets: String,
        onComplete: (Result<Unit>) -> Unit
    ) {
        if (title.isBlank() || content.isBlank()) {
            onComplete(Result.failure(IllegalArgumentException("Title and study content are required.")))
            return
        }
        viewModelScope.launch {
            val bullets = quickBullets.lines().map { it.trim() }.filter { it.isNotBlank() }
                .ifEmpty { listOf("Published by Smart StudyPoint Editorial Team for $exam.") }
            val notes = GeneratedNotes(
                title = title.trim(),
                classLevel = classLevel,
                exam = exam,
                subject = subject,
                topic = title.trim(),
                language = language,
                notesType = when (category) {
                    NoteCategory.SHORT_NOTES -> NotesType.SHORT_NOTES
                    NoteCategory.REVISION_NOTES -> NotesType.REVISION
                    NoteCategory.PYQ -> NotesType.PYQ_ANALYSIS
                    else -> NotesType.EXAM_FOCUSED
                },
                detailedNotes = content.trim(),
                shortNotes = bullets,
                easyExplanation = "Official curated study capsule published by Smart StudyPoint Admin for $exam ($subject).",
                definitions = emptyList(),
                importantFacts = bullets,
                comparisonTable = TableData(
                    title = "Exam Quick Facts",
                    headers = listOf("Attribute", "Details"),
                    rows = listOf(
                        listOf("Target Exam", exam),
                        listOf("Class / Level", classLevel),
                        listOf("Subject", subject),
                        listOf("Category", category.label)
                    )
                ),
                flowchartSteps = listOf("Read Official Summary", "Memorize Highlighted Facts", "Solve Related MCQs", "Revise Before Exam"),
                examples = emptyList(),
                examOrientedPoints = bullets,
                quickRevision = bullets,
                isSamplePreview = false
            )
            repository.saveGeneratedNotes(
                notes = notes,
                category = category,
                isFavorite = false,
                isPublishedByAdmin = true
            )
            onComplete(Result.success(Unit))
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    fun addCatalogItem(type: CatalogType, name: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.addCatalogItem(type, name))
        }
    }

    fun deleteCatalogItem(id: Long) {
        viewModelScope.launch { repository.deleteCatalogItem(id) }
    }

    fun addUser(
        fullName: String,
        email: String,
        role: String,
        targetExam: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(repository.addManagedUser(fullName, email, role, targetExam))
        }
    }

    fun toggleUserActive(user: ManagedUserEntity) {
        viewModelScope.launch { repository.toggleUserActive(user.id, user.isActive) }
    }

    fun cycleUserRole(user: ManagedUserEntity) {
        val nextRole = when (user.role) {
            "Student" -> "Educator"
            "Educator" -> "Admin"
            else -> "Student"
        }
        viewModelScope.launch { repository.updateUserRole(user.id, nextRole) }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch { repository.deleteManagedUser(id) }
    }

    companion object {
        fun factory(repository: StudyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminViewModel(repository) as T
                }
            }
    }
}
