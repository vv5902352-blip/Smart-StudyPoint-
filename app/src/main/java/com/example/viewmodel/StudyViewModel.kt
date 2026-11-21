package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CatalogItemEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.catalogType
import com.example.data.model.CatalogType
import com.example.data.model.ChatUiMessage
import com.example.data.model.GeneratedNotes
import com.example.data.model.McqQuestion
import com.example.data.model.McqRequest
import com.example.data.model.NoteCategory
import com.example.data.model.NotesRequest
import com.example.data.model.NotesType
import com.example.data.model.QnAItem
import com.example.data.model.QnARequest
import com.example.data.model.QnAType
import com.example.data.model.StudyLanguage
import com.example.data.model.UiState
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class McqSessionState(
    val questions: List<McqQuestion> = emptyList(),
    val selectedAnswers: Map<Int, Int> = emptyMap(), // questionId -> optionIndex
    val isPracticeTestMode: Boolean = true,
    val isSubmitted: Boolean = false,
    val currentIndex: Int = 0,
    val topic: String = "",
    val exam: String = "",
    val subject: String = ""
) {
    val answeredCount: Int get() = selectedAnswers.size
    val correctCount: Int
        get() = questions.count { q -> selectedAnswers[q.id] == q.correctIndex }
    val accuracyPercent: Int
        get() = if (questions.isEmpty()) 0 else (correctCount * 100) / questions.size
}

class StudyViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    val isLiveAiEnabled: Boolean = repository.aiRepository.isLiveAiEnabled

    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val appUsageDayKeys: StateFlow<Set<String>> = repository.appUsageDayKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val allNotes: StateFlow<List<StudyNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentNotes: StateFlow<List<StudyNoteEntity>> = repository.recentNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteNotes: StateFlow<List<StudyNoteEntity>> = repository.favoriteNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repository.quizAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val chatMessages: StateFlow<List<ChatUiMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val catalogFlow: StateFlow<List<CatalogItemEntity>> = repository.catalogItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val classOptions: StateFlow<List<String>> = catalogFlow
        .map { list ->
            list.filter { it.catalogType() == CatalogType.CLASS_LEVEL }.map { it.name }
                .ifEmpty { listOf("Class 6th", "Class 7th", "Class 8th", "Class 9th", "Class 10th", "Class 11th", "Class 12th", "Graduate / Competitive") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("Class 10th", "Class 12th", "Graduate / Competitive"))

    val examOptions: StateFlow<List<String>> = catalogFlow
        .map { list ->
            list.filter { it.catalogType() == CatalogType.EXAM }.map { it.name }
                .ifEmpty { listOf("CBSE Board", "UPSC CSE", "SSC CGL / CHSL", "Railway RRB NTPC", "BPSC", "State PCS / Exams") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("CBSE Board", "UPSC CSE", "SSC CGL / CHSL"))

    val subjectOptions: StateFlow<List<String>> = catalogFlow
        .map { list ->
            list.filter { it.catalogType() == CatalogType.SUBJECT }.map { it.name }
                .ifEmpty { listOf("Indian Polity", "History", "Geography", "General Science", "Physics", "Mathematics") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf("Indian Polity", "General Science", "History"))

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI Notes Generator State
    private val _notesState = MutableStateFlow<UiState<GeneratedNotes>>(UiState.Idle)
    val notesState: StateFlow<UiState<GeneratedNotes>> = _notesState.asStateFlow()
    private val _savedNoteIdForCurrent = MutableStateFlow<Long?>(null)
    val savedNoteIdForCurrent: StateFlow<Long?> = _savedNoteIdForCurrent.asStateFlow()
    private var lastNotesRequest: NotesRequest? = null

    // MCQ Generator & Practice State
    private val _mcqUiState = MutableStateFlow<UiState<McqSessionState>>(UiState.Idle)
    val mcqUiState: StateFlow<UiState<McqSessionState>> = _mcqUiState.asStateFlow()
    private var lastMcqRequest: McqRequest? = null

    // Q&A Generator State
    private val _qnaUiState = MutableStateFlow<UiState<List<QnAItem>>>(UiState.Idle)
    val qnaUiState: StateFlow<UiState<List<QnAItem>>> = _qnaUiState.asStateFlow()
    private var lastQnARequest: QnARequest? = null

    // Daily GK Quiz State
    private val _dailyGkState = MutableStateFlow<UiState<McqSessionState>>(UiState.Idle)
    val dailyGkState: StateFlow<UiState<McqSessionState>> = _dailyGkState.asStateFlow()
    private val _dailyGkLanguage = MutableStateFlow(StudyLanguage.ENGLISH)
    val dailyGkLanguage: StateFlow<StudyLanguage> = _dailyGkLanguage.asStateFlow()

    // Chatbot loading/error
    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()
    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            repository.recordDailyAppVisit(
                com.example.data.analytics.StudentProgressCalculator.toDayKey(System.currentTimeMillis())
            )
            loadDailyGkQuiz(StudyLanguage.ENGLISH)
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            repository.setDarkMode(!isDarkMode.value)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun submitSearch(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            repository.recordSearch(query)
        }
    }

    fun deleteSearchHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteSearchEntry(id) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    // --- AI Notes Generator ---
    fun generateNotes(request: NotesRequest) {
        lastNotesRequest = request
        _savedNoteIdForCurrent.value = null
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            try {
                val result = repository.generateNotes(request)
                result.fold(
                    onSuccess = { notes -> _notesState.value = UiState.Success(notes) },
                    onFailure = { err ->
                        _notesState.value = UiState.Error(err.message ?: "Unable to generate notes. Please retry.")
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _notesState.value = UiState.Error(e.localizedMessage ?: "Unexpected error generating notes.")
            }
        }
    }

    fun retryLastNotes() {
        lastNotesRequest?.let { generateNotes(it) }
    }

    fun saveCurrentGeneratedNotes(isFavorite: Boolean, onSaved: (Long) -> Unit = {}) {
        val current = (_notesState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val category = when (current.notesType) {
                NotesType.SHORT_NOTES -> NoteCategory.SHORT_NOTES
                NotesType.REVISION -> NoteCategory.REVISION_NOTES
                NotesType.PYQ_ANALYSIS -> NoteCategory.PYQ
                NotesType.EXAM_FOCUSED -> NoteCategory.IMPORTANT_QUESTIONS
                else -> NoteCategory.AI_NOTES
            }
            val id = repository.saveGeneratedNotes(current, category = category, isFavorite = isFavorite)
            _savedNoteIdForCurrent.value = id
            onSaved(id)
        }
    }

    // --- Saved / Recent / Favourite Notes ---
    fun toggleNoteFavorite(note: StudyNoteEntity) {
        viewModelScope.launch { repository.toggleFavorite(note.id, note.isFavorite) }
    }

    fun markNoteOpened(noteId: Long) {
        viewModelScope.launch { repository.markNoteOpened(noteId) }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch { repository.deleteNote(noteId) }
    }

    // --- MCQ Generator ---
    fun generateMcqs(request: McqRequest, practiceTestMode: Boolean) {
        lastMcqRequest = request
        viewModelScope.launch {
            _mcqUiState.value = UiState.Loading
            try {
                val result = repository.generateMcqs(request)
                result.fold(
                    onSuccess = { list ->
                        _mcqUiState.value = UiState.Success(
                            McqSessionState(
                                questions = list,
                                selectedAnswers = emptyMap(),
                                isPracticeTestMode = practiceTestMode,
                                isSubmitted = false,
                                currentIndex = 0,
                                topic = request.topic,
                                exam = request.exam,
                                subject = request.subject
                            )
                        )
                    },
                    onFailure = { err ->
                        _mcqUiState.value = UiState.Error(err.message ?: "Failed to generate MCQs.")
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mcqUiState.value = UiState.Error(e.localizedMessage ?: "Unexpected error generating MCQs.")
            }
        }
    }

    fun retryLastMcqs(practiceTestMode: Boolean) {
        lastMcqRequest?.let { generateMcqs(it, practiceTestMode) }
    }

    fun selectMcqOption(questionId: Int, optionIndex: Int) {
        val current = (_mcqUiState.value as? UiState.Success)?.data ?: return
        if (current.isSubmitted) return
        val updated = current.selectedAnswers.toMutableMap().apply { put(questionId, optionIndex) }
        _mcqUiState.value = UiState.Success(current.copy(selectedAnswers = updated))
    }

    fun setMcqIndex(index: Int) {
        val current = (_mcqUiState.value as? UiState.Success)?.data ?: return
        val safeIdx = index.coerceIn(0, (current.questions.size - 1).coerceAtLeast(0))
        _mcqUiState.value = UiState.Success(current.copy(currentIndex = safeIdx))
    }

    fun submitMcqTest() {
        val current = (_mcqUiState.value as? UiState.Success)?.data ?: return
        if (current.isSubmitted) return
        val submitted = current.copy(isSubmitted = true)
        _mcqUiState.value = UiState.Success(submitted)
        viewModelScope.launch {
            repository.recordQuizAttempt(
                title = "${submitted.topic} (${submitted.questions.size} MCQs)",
                exam = submitted.exam,
                subject = submitted.subject,
                totalQuestions = submitted.questions.size,
                correctAnswers = submitted.correctCount,
                isDailyGk = false
            )
        }
    }

    fun resetMcqTest() {
        val current = (_mcqUiState.value as? UiState.Success)?.data ?: return
        _mcqUiState.value = UiState.Success(
            current.copy(selectedAnswers = emptyMap(), isSubmitted = false, currentIndex = 0)
        )
    }

    // --- Q&A Generator ---
    fun generateQnA(request: QnARequest) {
        lastQnARequest = request
        viewModelScope.launch {
            _qnaUiState.value = UiState.Loading
            try {
                val result = repository.generateQnA(request)
                result.fold(
                    onSuccess = { list -> _qnaUiState.value = UiState.Success(list) },
                    onFailure = { err -> _qnaUiState.value = UiState.Error(err.message ?: "Failed to generate Q&A.") }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _qnaUiState.value = UiState.Error(e.localizedMessage ?: "Unexpected error generating Q&A.")
            }
        }
    }

    fun retryLastQnA() {
        lastQnARequest?.let { generateQnA(it) }
    }

    fun saveQnAToLibrary(
        request: QnARequest,
        items: List<QnAItem>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val formattedBody = items.joinToString("\n\n") { item ->
                "[${item.type.label} • ${item.type.marksHint}]\nQ${item.id}: ${item.question}\nAns: ${item.answer}"
            }
            val notes = GeneratedNotes(
                title = "${request.topic} — Q&A Bank (${items.size} Qs)",
                classLevel = request.classLevel,
                exam = request.exam,
                subject = request.subject,
                topic = request.topic,
                language = request.language,
                notesType = NotesType.EXAM_FOCUSED,
                detailedNotes = formattedBody,
                shortNotes = items.take(5).map { "Q${it.id}: ${it.question}" },
                easyExplanation = "Structured Question & Answer set tailored for ${request.classLevel} (${request.exam}).",
                definitions = emptyList(),
                importantFacts = items.flatMap { it.keyPoints }.distinct().take(6),
                comparisonTable = com.example.data.model.TableData(
                    title = "Q&A Weightage Breakdown",
                    headers = listOf("Category", "Marks", "Focus"),
                    rows = items.map { it.type }.distinct().map { listOf(it.label, it.marksHint, request.exam) }
                ),
                flowchartSteps = listOf("Read Question Carefully", "Identify Marks & Keywords", "Write Structured Points", "Review & Underline Key Terms"),
                examples = emptyList(),
                examOrientedPoints = listOf("Practice writing answers within word limits for ${request.exam}."),
                quickRevision = items.take(4).map { "${it.type.label}: ${it.question}" },
                isSamplePreview = !isLiveAiEnabled
            )
            repository.saveGeneratedNotes(notes, category = NoteCategory.IMPORTANT_QUESTIONS, isFavorite = false)
            onSaved()
        }
    }

    // --- Daily GK Quiz ---
    fun loadDailyGkQuiz(language: StudyLanguage) {
        _dailyGkLanguage.value = language
        viewModelScope.launch {
            _dailyGkState.value = UiState.Loading
            try {
                val res = repository.generateDailyGkQuiz(language)
                res.fold(
                    onSuccess = { qs ->
                        _dailyGkState.value = UiState.Success(
                            McqSessionState(
                                questions = qs,
                                selectedAnswers = emptyMap(),
                                isPracticeTestMode = true,
                                isSubmitted = false,
                                currentIndex = 0,
                                topic = "Daily GK Quiz",
                                exam = "UPSC / SSC / RRB / BPSC",
                                subject = "General Knowledge"
                            )
                        )
                    },
                    onFailure = { e -> _dailyGkState.value = UiState.Error(e.message ?: "Could not load Daily GK Quiz.") }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _dailyGkState.value = UiState.Error(e.localizedMessage ?: "Failed to load Daily GK Quiz.")
            }
        }
    }

    fun selectDailyGkOption(questionId: Int, optionIndex: Int) {
        val current = (_dailyGkState.value as? UiState.Success)?.data ?: return
        if (current.isSubmitted) return
        val updated = current.selectedAnswers.toMutableMap().apply { put(questionId, optionIndex) }
        _dailyGkState.value = UiState.Success(current.copy(selectedAnswers = updated))
    }

    fun submitDailyGkQuiz() {
        val current = (_dailyGkState.value as? UiState.Success)?.data ?: return
        if (current.isSubmitted) return
        val submitted = current.copy(isSubmitted = true)
        _dailyGkState.value = UiState.Success(submitted)
        viewModelScope.launch {
            repository.recordQuizAttempt(
                title = "Daily GK Quiz (${_dailyGkLanguage.value.label})",
                exam = "All Competitive Exams",
                subject = "General Knowledge",
                totalQuestions = submitted.questions.size,
                correctAnswers = submitted.correctCount,
                isDailyGk = true
            )
        }
    }

    // --- AI Study Assistant Chatbot ---
    fun sendChatMessage(message: String, examContext: String, language: StudyLanguage) {
        viewModelScope.launch {
            _chatError.value = null
            _isChatSending.value = true
            try {
                val res = repository.sendAssistantMessage(chatMessages.value, message, examContext, language)
                res.onFailure { err -> _chatError.value = err.message ?: "Unable to reply right now." }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatError.value = e.localizedMessage ?: "Assistant error."
            } finally {
                _isChatSending.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch { repository.clearChatHistory() }
    }

    companion object {
        fun factory(repository: StudyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudyViewModel(repository) as T
                }
            }
    }
}
