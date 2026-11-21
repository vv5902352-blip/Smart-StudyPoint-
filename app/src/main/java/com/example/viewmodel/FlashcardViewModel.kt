package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FlashcardDeckWithCards
import com.example.data.local.FlashcardEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlashcardStudySession(
    val deckId: Long,
    val deckTitle: String,
    val subject: String,
    val cards: List<FlashcardEntity>,
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val showHint: Boolean = false,
    val rememberedIds: Set<Long> = emptySet(),
    val needsReviewIds: Set<Long> = emptySet(),
    val isCompleted: Boolean = false
) {
    val currentCard: FlashcardEntity?
        get() = cards.getOrNull(currentIndex)

    val reviewedCount: Int
        get() = rememberedIds.size + needsReviewIds.size

    val accuracyPercent: Int
        get() = if (cards.isEmpty()) 0 else (rememberedIds.size * 100) / cards.size

    val missedCards: List<FlashcardEntity>
        get() = cards.filter { it.id in needsReviewIds }
}

class FlashcardViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    val decks: StateFlow<List<FlashcardDeckWithCards>> = repository.flashcardDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDeckId = MutableStateFlow<Long?>(null)
    val selectedDeckId: StateFlow<Long?> = _selectedDeckId.asStateFlow()

    private val _studySession = MutableStateFlow<FlashcardStudySession?>(null)
    val studySession: StateFlow<FlashcardStudySession?> = _studySession.asStateFlow()

    fun openDeck(deckId: Long) {
        _selectedDeckId.value = deckId
    }

    fun closeDeck() {
        _selectedDeckId.value = null
    }

    fun createDeck(
        title: String,
        subject: String,
        examTag: String,
        description: String,
        onResult: (Result<Long>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.createFlashcardDeck(title, subject, examTag, description)
            res.onSuccess { newId ->
                _selectedDeckId.value = newId
            }
            onResult(res)
        }
    }

    fun deleteDeck(deckId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (_selectedDeckId.value == deckId) {
                _selectedDeckId.value = null
            }
            if (_studySession.value?.deckId == deckId) {
                _studySession.value = null
            }
            repository.deleteFlashcardDeck(deckId)
            onDone()
        }
    }

    fun addCard(
        deckId: Long,
        term: String,
        definition: String,
        hint: String,
        onResult: (Result<Long>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.addFlashcard(deckId, term, definition, hint)
            onResult(res)
        }
    }

    fun updateCard(
        existing: FlashcardEntity,
        term: String,
        definition: String,
        hint: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.updateFlashcard(existing, term, definition, hint)
            onResult(res)
        }
    }

    fun deleteCard(cardId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteFlashcard(cardId)
            onDone()
        }
    }

    fun resetDeckMastery(deckId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetDeckMastery(deckId)
            onDone()
        }
    }

    fun startStudySession(
        deckWithCards: FlashcardDeckWithCards,
        onlyUnmastered: Boolean = false,
        shuffle: Boolean = false
    ): Boolean {
        val base = if (onlyUnmastered) {
            deckWithCards.cards.filter { !it.isMastered }.ifEmpty { deckWithCards.cards }
        } else {
            deckWithCards.cards
        }
        if (base.isEmpty()) return false

        val ordered = if (shuffle) base.shuffled() else base
        _studySession.value = FlashcardStudySession(
            deckId = deckWithCards.deck.id,
            deckTitle = deckWithCards.deck.title,
            subject = deckWithCards.deck.subject,
            cards = ordered
        )
        return true
    }

    fun flipCard() {
        val session = _studySession.value ?: return
        if (session.isCompleted) return
        _studySession.value = session.copy(isFlipped = !session.isFlipped)
    }

    fun toggleHint() {
        val session = _studySession.value ?: return
        _studySession.value = session.copy(showHint = !session.showHint)
    }

    fun rateCurrentCard(remembered: Boolean) {
        val session = _studySession.value ?: return
        val card = session.currentCard ?: return

        viewModelScope.launch {
            repository.recordFlashcardRecall(
                cardId = card.id,
                deckId = session.deckId,
                remembered = remembered
            )
        }

        val updatedRemembered = if (remembered) session.rememberedIds + card.id else session.rememberedIds - card.id
        val updatedReview = if (!remembered) session.needsReviewIds + card.id else session.needsReviewIds - card.id
        val nextIndex = session.currentIndex + 1
        val finished = nextIndex >= session.cards.size

        _studySession.value = session.copy(
            currentIndex = if (finished) session.currentIndex else nextIndex,
            isFlipped = false,
            showHint = false,
            rememberedIds = updatedRemembered,
            needsReviewIds = updatedReview,
            isCompleted = finished
        )
    }

    fun restartMissedCardsOnly() {
        val session = _studySession.value ?: return
        val missed = session.missedCards
        if (missed.isEmpty()) return
        _studySession.value = FlashcardStudySession(
            deckId = session.deckId,
            deckTitle = session.deckTitle,
            subject = session.subject,
            cards = missed.shuffled()
        )
    }

    fun restartCurrentSession(shuffle: Boolean = false) {
        val session = _studySession.value ?: return
        val ordered = if (shuffle) session.cards.shuffled() else session.cards
        _studySession.value = FlashcardStudySession(
            deckId = session.deckId,
            deckTitle = session.deckTitle,
            subject = session.subject,
            cards = ordered
        )
    }

    fun exitStudySession() {
        _studySession.value = null
    }

    companion object {
        fun factory(repository: StudyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FlashcardViewModel(repository) as T
                }
            }
    }
}
