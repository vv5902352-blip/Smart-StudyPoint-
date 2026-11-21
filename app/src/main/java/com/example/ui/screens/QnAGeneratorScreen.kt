package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.QnAItem
import com.example.data.model.QnARequest
import com.example.data.model.QnAType
import com.example.data.model.StudyLanguage
import com.example.data.model.UiState
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.BrandPillTag
import com.example.ui.components.DropdownPickerField
import com.example.ui.components.ErrorRetryBanner
import com.example.ui.components.SmartStudyFooter
import com.example.ui.components.StudyLoadingCard

private val qnaSuggestedTopics = listOf("Revolt of 1857", "Electricity & Circuits", "Indian Parliament", "Chemical Reactions")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QnAGeneratorScreen(
    isLiveAiEnabled: Boolean,
    classOptions: List<String>,
    examOptions: List<String>,
    subjectOptions: List<String>,
    qnaState: UiState<List<QnAItem>>,
    isSpeaking: Boolean,
    onGenerate: (QnARequest) -> Unit,
    onRetry: () -> Unit,
    onSaveToLibrary: (QnARequest, List<QnAItem>) -> Unit,
    onCopyAll: (String) -> Unit,
    onShareAll: (String, String) -> Unit,
    onExportPdf: (String, String, String) -> Unit,
    onToggleTts: (String, StudyLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    var selectedClass by rememberSaveable { mutableStateOf(classOptions.firstOrNull() ?: "Class 10th") }
    var selectedExam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "CBSE Board") }
    var selectedSubject by rememberSaveable { mutableStateOf(subjectOptions.firstOrNull() ?: "History") }
    var topic by rememberSaveable { mutableStateOf("") }
    var selectedLanguage by rememberSaveable { mutableStateOf(StudyLanguage.ENGLISH) }
    var selectedTypes by remember { mutableStateOf(QnAType.entries.toSet()) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var isFormExpanded by rememberSaveable { mutableStateOf(true) }

    val effectiveClass = if (selectedClass in classOptions) selectedClass else classOptions.firstOrNull().orEmpty()
    val effectiveExam = if (selectedExam in examOptions) selectedExam else examOptions.firstOrNull().orEmpty()
    val effectiveSubject = if (selectedSubject in subjectOptions) selectedSubject else subjectOptions.firstOrNull().orEmpty()
    val isLoading = qnaState is UiState.Loading

    val expandedMap = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(qnaState) {
        if (qnaState is UiState.Success) {
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
                            Text("Question Answer Generator", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = if (isFormExpanded) {
                                    "Very Short, Short, Long, Objective, Subjective & Important Exam Questions."
                                } else {
                                    "${topic.ifBlank { effectiveSubject }} • $effectiveExam • ${selectedLanguage.label}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (qnaState is UiState.Success) {
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
                                Text(if (isFormExpanded) "Hide" else "New Q&A")
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
                                placeholder = { Text("e.g., Revolt of 1857, Electricity, Democracy") },
                                singleLine = true,
                                isError = validationError != null,
                                supportingText = validationError?.let { err -> { Text(err) } },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("qna_topic_input")
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(qnaSuggestedTopics) { suggestion ->
                                    AssistChip(
                                        onClick = {
                                            topic = suggestion
                                            validationError = null
                                        },
                                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Text("Select Question Formats", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QnAType.entries.forEach { qType ->
                                    val isChosen = qType in selectedTypes
                                    FilterChip(
                                        selected = isChosen,
                                        onClick = {
                                            selectedTypes = if (isChosen && selectedTypes.size > 1) {
                                                selectedTypes - qType
                                            } else {
                                                selectedTypes + qType
                                            }
                                        },
                                        label = { Text(qType.label, style = MaterialTheme.typography.labelSmall) }
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

                            val emptyErr = stringResource(R.string.error_empty_topic)
                            Button(
                                onClick = {
                                    if (topic.trim().isEmpty()) {
                                        validationError = emptyErr
                                    } else {
                                        keyboardController?.hide()
                                        validationError = null
                                        expandedMap.clear()
                                        onGenerate(
                                            QnARequest(
                                                classLevel = effectiveClass,
                                                exam = effectiveExam,
                                                subject = effectiveSubject,
                                                topic = topic.trim(),
                                                language = selectedLanguage,
                                                selectedTypes = selectedTypes
                                            )
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_qna_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generating Q&A...", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Filled.QuestionAnswer, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generate Question & Answers", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        when (qnaState) {
            is UiState.Idle -> Unit
            is UiState.Loading -> {
                item {
                    StudyLoadingCard(
                        title = "Generating structured exam Q&As...",
                        subtitle = "Formatting answers with scoring key points and mark hints"
                    )
                }
            }

            is UiState.Error -> {
                item { ErrorRetryBanner(message = qnaState.message, onRetry = onRetry) }
            }

            is UiState.Success -> {
                val itemsList = qnaState.data
                val fullText = itemsList.joinToString("\n\n") { item ->
                    "[${item.type.label} - ${item.type.marksHint}]\nQ${item.id}: ${item.question}\nAnswer: ${item.answer}"
                }
                val currentReq = QnARequest(
                    classLevel = effectiveClass,
                    exam = effectiveExam,
                    subject = effectiveSubject,
                    topic = topic.ifBlank { "Study Topic" },
                    language = selectedLanguage,
                    selectedTypes = selectedTypes
                )

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSaveToLibrary(currentReq, itemsList) },
                                modifier = Modifier.testTag("qna_save_library_button")
                            ) {
                                Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save")
                            }
                            OutlinedButton(onClick = { onCopyAll(fullText) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Copy")
                            }
                            OutlinedButton(onClick = { onShareAll("${currentReq.topic} Q&A", fullText) }) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share")
                            }
                            OutlinedButton(
                                onClick = {
                                    onExportPdf(
                                        "${currentReq.topic} Q&A Bank",
                                        "${currentReq.classLevel} • ${currentReq.exam} • ${currentReq.subject}",
                                        fullText
                                    )
                                }
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("PDF")
                            }
                            OutlinedButton(onClick = { onToggleTts(fullText, selectedLanguage) }) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (isSpeaking) "Stop" else "Listen")
                            }
                        }
                    }
                }

                items(itemsList, key = { it.id }) { qna ->
                    val expanded = expandedMap[qna.id] ?: true
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedMap[qna.id] = !expanded }
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BrandPillTag(qna.type.label)
                                    BrandPillTag(
                                        text = qna.type.marksHint,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                IconButton(
                                    onClick = { expandedMap[qna.id] = !expanded },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Toggle answer"
                                    )
                                }
                            }
                            Text(
                                text = "Q${qna.id}. ${qna.question}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AnimatedVisibility(visible = expanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow
                                    ) {
                                        Text(
                                            text = qna.answer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    if (qna.keyPoints.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            qna.keyPoints.forEach { pt ->
                                                Text(
                                                    text = "✓ $pt",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}
