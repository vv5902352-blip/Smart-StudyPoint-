package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.McqQuestion
import com.example.data.model.McqRequest
import com.example.data.model.StudyLanguage
import com.example.data.model.UiState
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.BrandPillTag
import com.example.ui.components.DropdownPickerField
import com.example.ui.components.ErrorRetryBanner
import com.example.ui.components.SmartStudyFooter
import com.example.ui.components.StudyLoadingCard
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight
import com.example.viewmodel.McqSessionState

private val mcqCounts = listOf(10, 20, 30, 50, 100)
private val mcqSuggestedTopics = listOf("Indian Constitution", "Light & Optics", "Revolt of 1857", "Human Digestive System")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun McqGeneratorScreen(
    isLiveAiEnabled: Boolean,
    classOptions: List<String>,
    examOptions: List<String>,
    subjectOptions: List<String>,
    mcqState: UiState<McqSessionState>,
    onGenerate: (McqRequest, Boolean) -> Unit,
    onRetry: (Boolean) -> Unit,
    onSelectOption: (Int, Int) -> Unit,
    onSetIndex: (Int) -> Unit,
    onSubmitTest: () -> Unit,
    onResetTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    var selectedClass by rememberSaveable { mutableStateOf(classOptions.firstOrNull() ?: "Class 10th") }
    var selectedExam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "SSC CGL / CHSL") }
    var selectedSubject by rememberSaveable { mutableStateOf(subjectOptions.firstOrNull() ?: "Indian Polity") }
    var topic by rememberSaveable { mutableStateOf("") }
    var selectedLanguage by rememberSaveable { mutableStateOf(StudyLanguage.ENGLISH) }
    var selectedCount by rememberSaveable { mutableIntStateOf(10) }
    var practiceTestMode by rememberSaveable { mutableStateOf(true) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var isFormExpanded by rememberSaveable { mutableStateOf(true) }

    val effectiveClass = if (selectedClass in classOptions) selectedClass else classOptions.firstOrNull().orEmpty()
    val effectiveExam = if (selectedExam in examOptions) selectedExam else examOptions.firstOrNull().orEmpty()
    val effectiveSubject = if (selectedSubject in subjectOptions) selectedSubject else subjectOptions.firstOrNull().orEmpty()
    val isLoading = mcqState is UiState.Loading

    val generatedTopicKey = (mcqState as? UiState.Success)?.data?.let { "${it.topic}_${it.questions.size}" }
    LaunchedEffect(generatedTopicKey) {
        if (generatedTopicKey != null) {
            isFormExpanded = false
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isLiveAiEnabled) {
            item { AiSampleNoticeBanner() }
        }

        // Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MCQ Practice Test Generator",
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (!isFormExpanded) {
                                Text(
                                    text = "${topic.ifBlank { effectiveSubject }} • $selectedCount MCQs • $effectiveExam",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (mcqState is UiState.Success) {
                            OutlinedButton(
                                onClick = { isFormExpanded = !isFormExpanded },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFormExpanded) Icons.Filled.ExpandLess else Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (isFormExpanded) "Hide" else "New Test")
                            }
                        }
                    }

                    AnimatedVisibility(visible = isFormExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DropdownPickerField(
                                    label = "Class",
                                    options = classOptions,
                                    selectedOption = effectiveClass,
                                    onOptionSelected = { selectedClass = it },
                                    modifier = Modifier.weight(1f)
                                )
                                DropdownPickerField(
                                    label = "Exam",
                                    options = examOptions,
                                    selectedOption = effectiveExam,
                                    onOptionSelected = { selectedExam = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            DropdownPickerField(
                                label = "Subject",
                                options = subjectOptions,
                                selectedOption = effectiveSubject,
                                onOptionSelected = { selectedSubject = it }
                            )
                            OutlinedTextField(
                                value = topic,
                                onValueChange = {
                                    topic = it
                                    if (validationError != null) validationError = null
                                },
                                label = { Text("Chapter or Topic") },
                                placeholder = { Text("e.g., Indian Constitution, Light & Optics") },
                                singleLine = true,
                                isError = validationError != null,
                                supportingText = validationError?.let { err -> { Text(err) } },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mcq_topic_input")
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(mcqSuggestedTopics) { suggestion ->
                                    AssistChip(
                                        onClick = {
                                            topic = suggestion
                                            validationError = null
                                        },
                                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Text("Number of Questions", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mcqCounts.forEach { count ->
                                    FilterChip(
                                        selected = selectedCount == count,
                                        onClick = { selectedCount = count },
                                        label = { Text("$count MCQs") },
                                        modifier = Modifier.testTag("mcq_count_$count")
                                    )
                                }
                            }

                            Text("Language", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StudyLanguage.entries.forEach { lang ->
                                    FilterChip(
                                        selected = selectedLanguage == lang,
                                        onClick = { selectedLanguage = lang },
                                        label = { Text(lang.label) }
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Practice Test Mode", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = if (practiceTestMode) "Answers & score shown after submitting test" else "Instant explanation after each question",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = practiceTestMode,
                                        onCheckedChange = { practiceTestMode = it },
                                        modifier = Modifier.testTag("mcq_practice_mode_switch")
                                    )
                                }
                            }

                            val emptyErr = stringResource(R.string.error_empty_topic)
                            Button(
                                onClick = {
                                    if (topic.trim().isEmpty()) {
                                        validationError = emptyErr
                                    } else {
                                        keyboardController?.hide()
                                        validationError = null
                                        onGenerate(
                                            McqRequest(
                                                classLevel = effectiveClass,
                                                exam = effectiveExam,
                                                subject = effectiveSubject,
                                                topic = topic.trim(),
                                                language = selectedLanguage,
                                                count = selectedCount
                                            ),
                                            practiceTestMode
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_mcq_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generating MCQs...", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generate $selectedCount MCQs", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Output / Quiz Runner
        when (mcqState) {
            is UiState.Idle -> Unit
            is UiState.Loading -> {
                item {
                    StudyLoadingCard(
                        title = "Building $selectedCount exam-level MCQs...",
                        subtitle = "Preparing 4 options, answer key, and detailed explanations"
                    )
                }
            }

            is UiState.Error -> {
                item {
                    ErrorRetryBanner(
                        message = mcqState.message,
                        onRetry = { onRetry(practiceTestMode) }
                    )
                }
            }

            is UiState.Success -> {
                val session = mcqState.data
                if (session.questions.isNotEmpty()) {
                    // Score Summary Banner if Submitted
                    if (session.isSubmitted) {
                        item {
                            McqScoreSummaryCard(
                                session = session,
                                onRetake = onResetTest
                            )
                        }
                    }

                    // Progress & Question Navigator
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${session.currentIndex + 1} of ${session.questions.size}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Attempted: ${session.answeredCount}/${session.questions.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { session.answeredCount.toFloat() / session.questions.size.coerceAtLeast(1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                itemsIndexed(session.questions) { idx, q ->
                                    val chosen = session.selectedAnswers[q.id]
                                    val isCurrent = idx == session.currentIndex
                                    val pillBg = when {
                                        session.isSubmitted && chosen == q.correctIndex -> EmeraldSuccess
                                        session.isSubmitted && chosen != null -> CrimsonError
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        chosen != null -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                    val pillFg = when {
                                        session.isSubmitted && chosen != null -> PureWhite
                                        isCurrent -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable { onSetIndex(idx) },
                                        color = pillBg,
                                        border = if (isCurrent) BorderStroke(2.dp, NavyDeep) else null
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${idx + 1}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = pillFg,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Question Card
                    val activeQ = session.questions[session.currentIndex.coerceIn(0, session.questions.lastIndex)]
                    val revealNow = session.isSubmitted || (!session.isPracticeTestMode && session.selectedAnswers.containsKey(activeQ.id))
                    item(key = "active_q_${activeQ.id}") {
                        McqQuestionCard(
                            question = activeQ,
                            selectedOption = session.selectedAnswers[activeQ.id],
                            revealAnswer = revealNow,
                            onOptionSelected = { optIdx -> onSelectOption(activeQ.id, optIdx) }
                        )
                    }

                    // Navigation & Submit Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSetIndex(session.currentIndex - 1) },
                                enabled = session.currentIndex > 0,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Previous")
                            }
                            if (session.currentIndex < session.questions.lastIndex) {
                                Button(
                                    onClick = { onSetIndex(session.currentIndex + 1) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Next")
                                }
                            }
                            if (!session.isSubmitted) {
                                Button(
                                    onClick = onSubmitTest,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("submit_mcq_test_button")
                                ) {
                                    Text("Submit Test")
                                }
                            }
                        }
                    }

                    // After submission, show full answer key review list
                    if (session.isSubmitted) {
                        item {
                            Text(
                                text = "Complete Answer Key & Explanations",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        itemsIndexed(session.questions, key = { _, q -> "review_${q.id}" }) { _, q ->
                            McqQuestionCard(
                                question = q,
                                selectedOption = session.selectedAnswers[q.id],
                                revealAnswer = true,
                                onOptionSelected = {}
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
fun McqScoreSummaryCard(
    session: McqSessionState,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grade = when {
        session.accuracyPercent >= 85 -> "Excellent — Exam Ready!"
        session.accuracyPercent >= 65 -> "Good — Keep Polishing!"
        session.accuracyPercent >= 40 -> "Fair — Revise Key Concepts"
        else -> "Needs Practice — Review Explanations"
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
            .padding(20.dp)
            .testTag("mcq_score_summary_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = SkyLight, modifier = Modifier.size(28.dp))
                Column {
                    Text("Test Score Report", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                    Text(grade, style = MaterialTheme.typography.bodySmall, color = SkyLight)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScoreStatBox("Score", "${session.correctCount}/${session.questions.size}", Modifier.weight(1f))
                ScoreStatBox("Accuracy", "${session.accuracyPercent}%", Modifier.weight(1f))
                ScoreStatBox(
                    "Wrong",
                    "${(session.answeredCount - session.correctCount).coerceAtLeast(0)}",
                    Modifier.weight(1f)
                )
            }
            OutlinedButton(
                onClick = onRetake,
                border = BorderStroke(1.dp, SkyLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retake Practice Test", color = PureWhite)
            }
        }
    }
}

@Composable
private fun ScoreStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PureWhite.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = PureWhite)
            Text(label, style = MaterialTheme.typography.labelSmall, color = SkyLight)
        }
    }
}

@Composable
fun McqQuestionCard(
    question: McqQuestion,
    selectedOption: Int?,
    revealAnswer: Boolean,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val letters = listOf("A", "B", "C", "D")
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandPillTag("Q${question.id}")
                if (question.examTag.isNotBlank()) {
                    Text(
                        text = question.examTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEachIndexed { idx, optText ->
                    val isSelected = selectedOption == idx
                    val isCorrect = idx == question.correctIndex
                    val bgColor = when {
                        revealAnswer && isCorrect -> EmeraldSoft
                        revealAnswer && isSelected && !isCorrect -> CrimsonSoft
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    }
                    val borderColor = when {
                        revealAnswer && isCorrect -> EmeraldSuccess
                        revealAnswer && isSelected && !isCorrect -> CrimsonError
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    val textColor = when {
                        revealAnswer && (isCorrect || isSelected) -> NavyDeep
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !revealAnswer) { onOptionSelected(idx) }
                            .testTag("mcq_q${question.id}_opt_$idx"),
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(1.2.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letters.getOrElse(idx) { "${idx + 1}" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            if (revealAnswer && isCorrect) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Correct", tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                            } else if (revealAnswer && isSelected && !isCorrect) {
                                Icon(Icons.Filled.Close, contentDescription = "Incorrect", tint = CrimsonError, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            if (revealAnswer) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Explanation",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
