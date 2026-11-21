package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteQuestionEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.model.ChapterQnARequest
import com.example.data.model.ChapterStudyPack
import com.example.data.model.MarksFilter
import com.example.data.model.ObjectiveKind
import com.example.data.model.UiState
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChapterTestReport(
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val accuracyPercent: Int,
    val breakdownByKind: Map<ObjectiveKind, Pair<Int, Int>> // kind -> (correct, total)
)

class ChapterQnAViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    val isLiveAiEnabled: Boolean = repository.aiRepository.isLiveAiEnabled

    val favoriteQuestions: StateFlow<List<FavoriteQuestionEntity>> = repository.favoriteQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val performanceHistory: StateFlow<List<QuizAttemptEntity>> = repository.quizAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _packState = MutableStateFlow<UiState<ChapterStudyPack>>(UiState.Idle)
    val packState: StateFlow<UiState<ChapterStudyPack>> = _packState.asStateFlow()

    private val _isPracticeMode = MutableStateFlow(false) // false = Study Mode, true = Practice Mode
    val isPracticeMode: StateFlow<Boolean> = _isPracticeMode.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _isTestSubmitted = MutableStateFlow(false)
    val isTestSubmitted: StateFlow<Boolean> = _isTestSubmitted.asStateFlow()

    private val _isGeneratingMore = MutableStateFlow(false)
    val isGeneratingMore: StateFlow<Boolean> = _isGeneratingMore.asStateFlow()

    private val _marksFilter = MutableStateFlow(MarksFilter.ALL)
    val marksFilter: StateFlow<MarksFilter> = _marksFilter.asStateFlow()

    private var lastRequest: ChapterQnARequest? = null

    fun generateChapterPack(request: ChapterQnARequest) {
        lastRequest = request
        _selectedAnswers.value = emptyMap()
        _isTestSubmitted.value = false
        viewModelScope.launch {
            _packState.value = UiState.Loading
            try {
                val res = repository.generateChapterStudyPack(request)
                res.fold(
                    onSuccess = { pack -> _packState.value = UiState.Success(pack) },
                    onFailure = { err ->
                        _packState.value = UiState.Error(err.message ?: "Failed to generate chapter Q&A.")
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _packState.value = UiState.Error(e.localizedMessage ?: "Unexpected generation error.")
            }
        }
    }

    fun retryLastGeneration() {
        lastRequest?.let { generateChapterPack(it) }
    }

    fun generateMoreQuestions(additionalCount: Int = 10, onDone: (Int) -> Unit = {}) {
        val currentPack = (_packState.value as? UiState.Success)?.data ?: return
        if (_isGeneratingMore.value) return
        viewModelScope.launch {
            _isGeneratingMore.value = true
            try {
                val res = repository.generateMoreChapterQuestions(currentPack, additionalCount)
                res.onSuccess { extra ->
                    val updated = currentPack.copy(
                        objectiveQuestions = currentPack.objectiveQuestions + extra
                    )
                    _packState.value = UiState.Success(updated)
                    _isTestSubmitted.value = false
                    onDone(extra.size)
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                _isGeneratingMore.value = false
            }
        }
    }

    fun setPracticeMode(practiceMode: Boolean) {
        _isPracticeMode.value = practiceMode
        if (!practiceMode) {
            _isTestSubmitted.value = false
        }
    }

    fun selectObjectiveAnswer(questionNumber: Int, optionIndex: Int) {
        if (_isTestSubmitted.value) return
        _selectedAnswers.value = _selectedAnswers.value.toMutableMap().apply {
            put(questionNumber, optionIndex)
        }
    }

    fun submitChapterPracticeTest(): ChapterTestReport? {
        val pack = (_packState.value as? UiState.Success)?.data ?: return null
        val answers = _selectedAnswers.value
        val total = pack.objectiveQuestions.size
        val correct = pack.objectiveQuestions.count { q -> answers[q.number] == q.correctOptionIndex }
        val accuracy = if (total == 0) 0 else (correct * 100) / total
        val breakdown = ObjectiveKind.entries.associateWith { kind ->
            val subset = pack.objectiveQuestions.filter { it.kind == kind }
            val subsetCorrect = subset.count { q -> answers[q.number] == q.correctOptionIndex }
            subsetCorrect to subset.size
        }.filterValues { it.second > 0 }

        _isTestSubmitted.value = true
        viewModelScope.launch {
            repository.recordQuizAttempt(
                title = "${pack.request.chapterName} (${pack.request.classLevel})",
                exam = pack.request.boardExam,
                subject = pack.request.subject,
                totalQuestions = total,
                correctAnswers = correct,
                isDailyGk = false
            )
        }
        return ChapterTestReport(
            totalQuestions = total,
            attemptedCount = answers.size,
            correctCount = correct,
            accuracyPercent = accuracy,
            breakdownByKind = breakdown
        )
    }

    fun resetPracticeTest() {
        _selectedAnswers.value = emptyMap()
        _isTestSubmitted.value = false
    }

    fun computeCurrentReport(): ChapterTestReport? {
        val pack = (_packState.value as? UiState.Success)?.data ?: return null
        val answers = _selectedAnswers.value
        val total = pack.objectiveQuestions.size
        val correct = pack.objectiveQuestions.count { q -> answers[q.number] == q.correctOptionIndex }
        val accuracy = if (total == 0) 0 else (correct * 100) / total
        val breakdown = ObjectiveKind.entries.associateWith { kind ->
            val subset = pack.objectiveQuestions.filter { it.kind == kind }
            val subsetCorrect = subset.count { q -> answers[q.number] == q.correctOptionIndex }
            subsetCorrect to subset.size
        }.filterValues { it.second > 0 }
        return ChapterTestReport(total, answers.size, correct, accuracy, breakdown)
    }

    fun setMarksFilter(filter: MarksFilter) {
        _marksFilter.value = filter
    }

    fun toggleFavoriteQuestion(
        chapterName: String,
        subject: String,
        classLevel: String,
        questionType: String,
        marksLabel: String,
        questionText: String,
        answerText: String
    ) {
        val exists = favoriteQuestions.value.any { it.questionText == questionText }
        viewModelScope.launch {
            repository.toggleFavoriteQuestion(
                isCurrentlyFavorite = exists,
                item = FavoriteQuestionEntity(
                    chapterName = chapterName,
                    subject = subject,
                    classLevel = classLevel,
                    questionType = questionType,
                    marksLabel = marksLabel,
                    questionText = questionText,
                    answerText = answerText
                )
            )
        }
    }

    fun deleteFavoriteQuestion(id: Long) {
        viewModelScope.launch { repository.deleteFavoriteQuestion(id) }
    }

    companion object {
        fun factory(repository: StudyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChapterQnAViewModel(repository) as T
                }
            }
    }
}
