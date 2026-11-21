package com.example.data.ai

import com.example.data.model.ChatUiMessage
import com.example.data.model.GeneratedNotes
import com.example.data.model.McqQuestion
import com.example.data.model.McqRequest
import com.example.data.model.NotesRequest
import com.example.data.model.QnAItem
import com.example.data.model.QnARequest
import com.example.data.model.StudyLanguage

/**
 * Single AI gateway interface for all generative study features in Smart StudyPoint.
 * When Firebase AI Logic is enabled, a FirebaseAiRepository implementation can be swapped
 * in here with zero changes to ViewModels or UI screens.
 */
interface AiRepository {
    val isLiveAiEnabled: Boolean

    suspend fun generateNotes(request: NotesRequest): Result<GeneratedNotes>

    suspend fun generateMcqs(request: McqRequest): Result<List<McqQuestion>>

    suspend fun generateQnA(request: QnARequest): Result<List<QnAItem>>

    suspend fun sendChatMessage(
        history: List<ChatUiMessage>,
        userMessage: String,
        examContext: String,
        language: StudyLanguage
    ): Result<String>

    suspend fun generateDailyGkQuiz(language: StudyLanguage): Result<List<McqQuestion>>

    suspend fun generateChapterStudyPack(request: com.example.data.model.ChapterQnARequest): Result<com.example.data.model.ChapterStudyPack>

    suspend fun generateMoreChapterQuestions(
        existingPack: com.example.data.model.ChapterStudyPack,
        additionalCount: Int
    ): Result<List<com.example.data.model.ObjectiveQuestionItem>>
}
