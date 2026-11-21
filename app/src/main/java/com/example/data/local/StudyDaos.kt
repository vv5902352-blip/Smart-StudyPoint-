package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes ORDER BY createdAt DESC")
    fun observeAllNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes ORDER BY lastOpenedAt DESC LIMIT 10")
    fun observeRecentNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isFavorite = 1 ORDER BY lastOpenedAt DESC")
    fun observeFavoriteNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): StudyNoteEntity?

    @Query("SELECT COUNT(*) FROM study_notes")
    suspend fun countNotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<StudyNoteEntity>)

    @Query("UPDATE study_notes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE study_notes SET lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun touchLastOpened(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 12")
    fun observeHistory(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE LOWER(query) = LOWER(:query)")
    suspend fun deleteDuplicate(query: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts ORDER BY completedAt DESC")
    fun observeAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    suspend fun countAttempts(): Int
}

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog_items ORDER BY id ASC")
    fun observeCatalog(): Flow<List<CatalogItemEntity>>

    @Query("SELECT COUNT(*) FROM catalog_items")
    suspend fun countItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CatalogItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ManagedUserDao {
    @Query("SELECT * FROM managed_users ORDER BY joinedAt DESC")
    fun observeUsers(): Flow<List<ManagedUserEntity>>

    @Query("SELECT COUNT(*) FROM managed_users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: ManagedUserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<ManagedUserEntity>)

    @Query("UPDATE managed_users SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean)

    @Query("UPDATE managed_users SET role = :role WHERE id = :id")
    suspend fun updateRole(id: Long, role: String)

    @Query("DELETE FROM managed_users WHERE id = :id")
    suspend fun deleteUserById(id: Long)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC, id ASC")
    fun observeMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface FavoriteQuestionDao {
    @Query("SELECT * FROM favorite_questions ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteQuestionEntity): Long

    @Query("DELETE FROM favorite_questions WHERE questionText = :questionText")
    suspend fun deleteByQuestionText(questionText: String)

    @Query("DELETE FROM favorite_questions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface FlashcardDao {
    @Transaction
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAt DESC, id DESC")
    fun observeDecksWithCards(): Flow<List<FlashcardDeckWithCards>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY createdAt ASC, id ASC")
    fun observeCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcard_decks")
    suspend fun countDecks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)

    @Query("UPDATE flashcard_decks SET lastStudiedAt = :timestamp WHERE id = :deckId")
    suspend fun touchDeckStudied(deckId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM flashcard_decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query(
        "UPDATE flashcards SET isMastered = :isMastered, reviewCount = reviewCount + 1, " +
            "correctCount = correctCount + :correctDelta WHERE id = :cardId"
    )
    suspend fun recordCardRecall(cardId: Long, isMastered: Boolean, correctDelta: Int)

    @Query("UPDATE flashcards SET isMastered = 0 WHERE deckId = :deckId")
    suspend fun resetDeckMastery(deckId: Long)
}
