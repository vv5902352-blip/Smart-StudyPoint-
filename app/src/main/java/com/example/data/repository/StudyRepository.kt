package com.example.data.repository

import com.example.data.ai.AiRepository
import com.example.data.local.CatalogItemEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardDeckWithCards
import com.example.data.local.FlashcardEntity
import com.example.data.local.ManagedUserEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.SeedDataProvider
import com.example.data.local.SmartStudyDatabase
import com.example.data.local.StudyNoteEntity
import com.example.data.local.toEntity
import com.example.data.model.CatalogType
import com.example.data.model.ChatUiMessage
import com.example.data.model.GeneratedNotes
import com.example.data.model.McqQuestion
import com.example.data.model.McqRequest
import com.example.data.model.NoteCategory
import com.example.data.model.NotesRequest
import com.example.data.model.QnAItem
import com.example.data.model.QnARequest
import com.example.data.model.StudyLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudyRepository(
    private val db: SmartStudyDatabase,
    private val preferences: PreferencesManager,
    val aiRepository: AiRepository
) {
    private val noteDao = db.studyNoteDao()
    private val searchDao = db.searchHistoryDao()
    private val quizDao = db.quizAttemptDao()
    private val catalogDao = db.catalogDao()
    private val userDao = db.managedUserDao()
    private val chatDao = db.chatMessageDao()
    private val favQuestionDao = db.favoriteQuestionDao()
    private val flashcardDao = db.flashcardDao()

    val allNotes: Flow<List<StudyNoteEntity>> = noteDao.observeAllNotes()
    val recentNotes: Flow<List<StudyNoteEntity>> = noteDao.observeRecentNotes()
    val favoriteNotes: Flow<List<StudyNoteEntity>> = noteDao.observeFavoriteNotes()
    val favoriteQuestions: Flow<List<com.example.data.local.FavoriteQuestionEntity>> = favQuestionDao.observeFavorites()
    val flashcardDecks: Flow<List<FlashcardDeckWithCards>> = flashcardDao.observeDecksWithCards()
    val searchHistory: Flow<List<SearchHistoryEntity>> = searchDao.observeHistory()
    val quizAttempts: Flow<List<QuizAttemptEntity>> = quizDao.observeAttempts()
    val catalogItems: Flow<List<CatalogItemEntity>> = catalogDao.observeCatalog()
    val managedUsers: Flow<List<ManagedUserEntity>> = userDao.observeUsers()
    val chatMessages: Flow<List<ChatUiMessage>> = chatDao.observeMessages().map { list ->
        list.map { entity ->
            ChatUiMessage(
                id = entity.id,
                isUser = entity.isUser,
                text = entity.text,
                timestamp = entity.timestamp,
                isSamplePreview = entity.isSamplePreview
            )
        }
    }

    val isDarkMode: Flow<Boolean> = preferences.isDarkMode
    val appUsageDayKeys: Flow<Set<String>> = preferences.appUsageDayKeys

    suspend fun ensureSeeded() {
        if (catalogDao.countItems() == 0) {
            catalogDao.insertItems(SeedDataProvider.defaultCatalogItems())
        }
        if (noteDao.countNotes() == 0) {
            noteDao.insertNotes(SeedDataProvider.defaultStudyNotes())
        }
        if (userDao.countUsers() == 0) {
            userDao.insertUsers(SeedDataProvider.defaultUsers())
        }
        if (quizDao.countAttempts() == 0) {
            SeedDataProvider.defaultQuizAttempts().forEach { quizDao.insertAttempt(it) }
        }
        if (flashcardDao.countDecks() == 0) {
            SeedDataProvider.defaultFlashcardDecks().forEach { (deck, cards) ->
                val deckId = flashcardDao.insertDeck(deck)
                flashcardDao.insertCards(cards.map { it.copy(deckId = deckId) })
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) = preferences.setDarkMode(enabled)

    suspend fun recordDailyAppVisit(dayKey: String) = preferences.recordAppUsageToday(dayKey)

    // AI Operations
    suspend fun generateNotes(request: NotesRequest): Result<GeneratedNotes> =
        aiRepository.generateNotes(request)

    suspend fun generateMcqs(request: McqRequest): Result<List<McqQuestion>> =
        aiRepository.generateMcqs(request)

    suspend fun generateQnA(request: QnARequest): Result<List<QnAItem>> =
        aiRepository.generateQnA(request)

    suspend fun generateDailyGkQuiz(language: StudyLanguage): Result<List<McqQuestion>> =
        aiRepository.generateDailyGkQuiz(language)

    suspend fun generateChapterStudyPack(
        request: com.example.data.model.ChapterQnARequest
    ): Result<com.example.data.model.ChapterStudyPack> =
        aiRepository.generateChapterStudyPack(request)

    suspend fun generateMoreChapterQuestions(
        existingPack: com.example.data.model.ChapterStudyPack,
        additionalCount: Int
    ): Result<List<com.example.data.model.ObjectiveQuestionItem>> =
        aiRepository.generateMoreChapterQuestions(existingPack, additionalCount)

    suspend fun toggleFavoriteQuestion(
        isCurrentlyFavorite: Boolean,
        item: com.example.data.local.FavoriteQuestionEntity
    ) {
        if (isCurrentlyFavorite) {
            favQuestionDao.deleteByQuestionText(item.questionText)
        } else {
            favQuestionDao.insert(item)
        }
    }

    suspend fun deleteFavoriteQuestion(id: Long) = favQuestionDao.deleteById(id)

    suspend fun sendAssistantMessage(
        history: List<ChatUiMessage>,
        userText: String,
        examContext: String,
        language: StudyLanguage
    ): Result<String> {
        chatDao.insertMessage(
            ChatMessageEntity(isUser = true, text = userText, isSamplePreview = false)
        )
        val result = aiRepository.sendChatMessage(history, userText, examContext, language)
        result.onSuccess { reply ->
            chatDao.insertMessage(
                ChatMessageEntity(
                    isUser = false,
                    text = reply,
                    isSamplePreview = !aiRepository.isLiveAiEnabled
                )
            )
        }
        return result
    }

    suspend fun clearChatHistory() = chatDao.clearChat()

    // Notes Persistence
    suspend fun saveGeneratedNotes(
        notes: GeneratedNotes,
        category: NoteCategory = NoteCategory.AI_NOTES,
        isFavorite: Boolean = false,
        isPublishedByAdmin: Boolean = false
    ): Long = noteDao.insertNote(
        notes.toEntity(
            category = category,
            isFavorite = isFavorite,
            isPublishedByAdmin = isPublishedByAdmin
        )
    )

    suspend fun toggleFavorite(noteId: Long, currentlyFavorite: Boolean) =
        noteDao.updateFavorite(noteId, !currentlyFavorite)

    suspend fun markNoteOpened(noteId: Long) = noteDao.touchLastOpened(noteId)

    suspend fun deleteNote(noteId: Long) = noteDao.deleteNoteById(noteId)

    // Search History
    suspend fun recordSearch(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        searchDao.deleteDuplicate(clean)
        searchDao.insert(SearchHistoryEntity(query = clean))
    }

    suspend fun deleteSearchEntry(id: Long) = searchDao.deleteById(id)

    suspend fun clearSearchHistory() = searchDao.clearAll()

    // Quiz Analytics
    suspend fun recordQuizAttempt(
        title: String,
        exam: String,
        subject: String,
        totalQuestions: Int,
        correctAnswers: Int,
        isDailyGk: Boolean
    ) {
        quizDao.insertAttempt(
            QuizAttemptEntity(
                title = title,
                exam = exam,
                subject = subject,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                isDailyGk = isDailyGk
            )
        )
    }

    // Admin Catalog Management
    suspend fun addCatalogItem(type: CatalogType, name: String): Result<Unit> {
        val clean = name.trim()
        if (clean.isEmpty()) return Result.failure(IllegalArgumentException("Name cannot be empty."))
        catalogDao.insertItem(CatalogItemEntity(type = type.name, name = clean))
        return Result.success(Unit)
    }

    suspend fun deleteCatalogItem(id: Long) = catalogDao.deleteById(id)

    // Admin User Management
    suspend fun addManagedUser(
        fullName: String,
        email: String,
        role: String,
        targetExam: String
    ): Result<Unit> {
        if (fullName.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid name and email address."))
        }
        userDao.insertUser(
            ManagedUserEntity(
                fullName = fullName.trim(),
                email = email.trim(),
                role = role,
                targetExam = targetExam.ifBlank { "CBSE Board" },
                isActive = true
            )
        )
        return Result.success(Unit)
    }

    suspend fun toggleUserActive(id: Long, currentlyActive: Boolean) =
        userDao.updateActiveStatus(id, !currentlyActive)

    suspend fun updateUserRole(id: Long, role: String) =
        userDao.updateRole(id, role)

    suspend fun deleteManagedUser(id: Long) = userDao.deleteUserById(id)

    // Flashcards (Room Local Storage)
    suspend fun createFlashcardDeck(
        title: String,
        subject: String,
        examTag: String,
        description: String
    ): Result<Long> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter a deck title."))
        }
        val id = flashcardDao.insertDeck(
            FlashcardDeckEntity(
                title = cleanTitle,
                subject = subject.trim().ifBlank { "General" },
                examTag = examTag.trim().ifBlank { "CBSE Board" },
                description = description.trim()
            )
        )
        return Result.success(id)
    }

    suspend fun deleteFlashcardDeck(deckId: Long) = flashcardDao.deleteDeckById(deckId)

    suspend fun addFlashcard(
        deckId: Long,
        term: String,
        definition: String,
        hint: String
    ): Result<Long> {
        val cleanTerm = term.trim()
        val cleanDef = definition.trim()
        if (cleanTerm.isEmpty() || cleanDef.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter both a term and its definition."))
        }
        val id = flashcardDao.insertCard(
            FlashcardEntity(
                deckId = deckId,
                term = cleanTerm,
                definition = cleanDef,
                hint = hint.trim()
            )
        )
        return Result.success(id)
    }

    suspend fun updateFlashcard(
        existing: FlashcardEntity,
        term: String,
        definition: String,
        hint: String
    ): Result<Unit> {
        val cleanTerm = term.trim()
        val cleanDef = definition.trim()
        if (cleanTerm.isEmpty() || cleanDef.isEmpty()) {
            return Result.failure(IllegalArgumentException("Term and definition cannot be empty."))
        }
        flashcardDao.updateCard(
            existing.copy(
                term = cleanTerm,
                definition = cleanDef,
                hint = hint.trim()
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteFlashcard(cardId: Long) = flashcardDao.deleteCardById(cardId)

    suspend fun recordFlashcardRecall(cardId: Long, deckId: Long, remembered: Boolean) {
        flashcardDao.recordCardRecall(
            cardId = cardId,
            isMastered = remembered,
            correctDelta = if (remembered) 1 else 0
        )
        flashcardDao.touchDeckStudied(deckId)
    }

    suspend fun resetDeckMastery(deckId: Long) = flashcardDao.resetDeckMastery(deckId)
}
