package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatUiMessage
import com.example.data.model.StudyLanguage
import com.example.data.model.UiState
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.ErrorRetryBanner
import com.example.ui.components.SmartStudyFooter
import com.example.ui.components.StudyLoadingCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight
import com.example.viewmodel.McqSessionState

enum class QuizChatTab(val label: String) {
    DAILY_GK("Daily GK Quiz"),
    AI_ASSISTANT("AI Study Chatbot")
}

private val quickChatPrompts = listOf(
    "Explain Fundamental Rights for UPSC",
    "Tips to score 95% in CBSE Class 10 Science",
    "Difference between Lok Sabha & Rajya Sabha",
    "Short trick for Trigonometry identities",
    "BPSC Bihar History revision strategy"
)

@Composable
fun AssistantAndQuizScreen(
    selectedTab: QuizChatTab,
    onSelectTab: (QuizChatTab) -> Unit,
    isLiveAiEnabled: Boolean,
    dailyGkState: UiState<McqSessionState>,
    dailyGkLanguage: StudyLanguage,
    onLoadDailyGk: (StudyLanguage) -> Unit,
    onSelectDailyGkOption: (Int, Int) -> Unit,
    onSubmitDailyGk: () -> Unit,
    chatMessages: List<ChatUiMessage>,
    isChatSending: Boolean,
    chatError: String?,
    examOptions: List<String>,
    isSpeaking: Boolean,
    onSendChat: (String, String, StudyLanguage) -> Unit,
    onClearChat: () -> Unit,
    onCopyMessage: (String) -> Unit,
    onToggleTts: (String, StudyLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            QuizChatTab.entries.forEachIndexed { idx, tab ->
                SegmentedButton(
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    shape = SegmentedButtonDefaults.itemShape(idx, QuizChatTab.entries.size),
                    icon = {
                        Icon(
                            imageVector = if (tab == QuizChatTab.DAILY_GK) Icons.Filled.EmojiEvents else Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                ) {
                    Text(tab.label)
                }
            }
        }

        when (selectedTab) {
            QuizChatTab.DAILY_GK -> DailyGkQuizContent(
                isLiveAiEnabled = isLiveAiEnabled,
                state = dailyGkState,
                language = dailyGkLanguage,
                onSwitchLanguage = onLoadDailyGk,
                onSelectOption = onSelectDailyGkOption,
                onSubmit = onSubmitDailyGk,
                onRetake = { onLoadDailyGk(dailyGkLanguage) },
                modifier = Modifier.weight(1f)
            )

            QuizChatTab.AI_ASSISTANT -> AiStudyChatbotContent(
                isLiveAiEnabled = isLiveAiEnabled,
                messages = chatMessages,
                isSending = isChatSending,
                errorMessage = chatError,
                examOptions = examOptions,
                isSpeaking = isSpeaking,
                onSend = onSendChat,
                onClearChat = onClearChat,
                onCopyMessage = onCopyMessage,
                onToggleTts = onToggleTts,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DailyGkQuizContent(
    isLiveAiEnabled: Boolean,
    state: UiState<McqSessionState>,
    language: StudyLanguage,
    onSwitchLanguage: (StudyLanguage) -> Unit,
    onSelectOption: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily GK & Competitive Quiz", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                    Text(
                        text = "10 high-yield Indian Polity, History, Geography, Science & Economy questions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyLight
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(StudyLanguage.ENGLISH, StudyLanguage.HINDI).forEach { lang ->
                            FilterChip(
                                selected = language == lang,
                                onClick = { onSwitchLanguage(lang) },
                                label = { Text(lang.label) }
                            )
                        }
                    }
                }
            }
        }

        if (!isLiveAiEnabled) {
            item { AiSampleNoticeBanner() }
        }

        when (state) {
            is UiState.Idle, is UiState.Loading -> {
                item {
                    StudyLoadingCard(
                        title = "Loading today's GK quiz...",
                        subtitle = "Preparing 10 Indian exam questions with explanations"
                    )
                }
            }

            is UiState.Error -> {
                item {
                    ErrorRetryBanner(message = state.message, onRetry = onRetake)
                }
            }

            is UiState.Success -> {
                val session = state.data
                if (session.isSubmitted) {
                    item {
                        McqScoreSummaryCard(session = session, onRetake = onRetake)
                    }
                }

                items(session.questions, key = { it.id }) { q ->
                    McqQuestionCard(
                        question = q,
                        selectedOption = session.selectedAnswers[q.id],
                        revealAnswer = session.isSubmitted,
                        onOptionSelected = { opt -> onSelectOption(q.id, opt) }
                    )
                }

                if (!session.isSubmitted) {
                    item {
                        Button(
                            onClick = onSubmit,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_daily_gk_button")
                        ) {
                            Text(
                                text = "Submit Daily GK Quiz (${session.answeredCount}/${session.questions.size} Answered)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}

@Composable
private fun AiStudyChatbotContent(
    isLiveAiEnabled: Boolean,
    messages: List<ChatUiMessage>,
    isSending: Boolean,
    errorMessage: String?,
    examOptions: List<String>,
    isSpeaking: Boolean,
    onSend: (String, String, StudyLanguage) -> Unit,
    onClearChat: () -> Unit,
    onCopyMessage: (String) -> Unit,
    onToggleTts: (String, StudyLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var selectedExam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "UPSC CSE") }
    var selectedLang by rememberSaveable { mutableStateOf(StudyLanguage.ENGLISH) }

    Column(modifier = modifier.fillMaxSize()) {
        // Controls strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(StudyLanguage.entries) { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { selectedLang = lang },
                        label = { Text(lang.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear chat history",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isLiveAiEnabled) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                AiSampleNoticeBanner()
            }
        }

        // Chat Stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Ask your Smart StudyPoint AI Tutor",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Get concept breakdowns, NCERT exam strategies, memory tricks, and quick revision help in English, Hindi, or Hinglish.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Target Exam Context", style = MaterialTheme.typography.labelMedium)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(examOptions.take(6)) { exam ->
                                    FilterChip(
                                        selected = selectedExam == exam,
                                        onClick = { selectedExam = exam },
                                        label = { Text(exam, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Text("Try a Quick Prompt", style = MaterialTheme.typography.labelMedium)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickChatPrompts.forEach { prompt ->
                                    AssistChip(
                                        onClick = { onSend(prompt, selectedExam, selectedLang) },
                                        label = { Text(prompt, style = MaterialTheme.typography.bodySmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.88f),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        ),
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isUser) "You" else "Smart StudyPoint Tutor",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUser) SkyLight else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (!isUser) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onCopyMessage(msg.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy reply",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onToggleTts(msg.text, selectedLang) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Read reply aloud",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Tutor is preparing your study answer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    ErrorRetryBanner(
                        message = errorMessage,
                        onRetry = {
                            if (inputText.isNotBlank()) onSend(inputText, selectedExam, selectedLang)
                        }
                    )
                }
            }
        }

        // Composer
        Surface(
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask any exam doubt or concept...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )
                Button(
                    onClick = {
                        val trimmed = inputText.trim()
                        if (trimmed.isNotEmpty() && !isSending) {
                            onSend(trimmed, selectedExam, selectedLang)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send question")
                }
            }
        }
    }
}
