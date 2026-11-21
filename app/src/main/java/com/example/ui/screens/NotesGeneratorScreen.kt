package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GeneratedNotes
import com.example.data.model.NotesRequest
import com.example.data.model.NotesType
import com.example.data.model.StudyLanguage
import com.example.data.model.UiState
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.DropdownPickerField
import com.example.ui.components.ErrorRetryBanner
import com.example.ui.components.GeneratedNotesView
import com.example.ui.components.SmartStudyFooter
import com.example.ui.components.StudyLoadingCard

private val suggestedTopics = listOf(
    "Fundamental Rights (Art. 12-35)",
    "Photosynthesis & Respiration",
    "Newton's Laws of Motion",
    "Revolt of 1857",
    "Indian Monsoon System",
    "Quadratic Equations"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesGeneratorScreen(
    isLiveAiEnabled: Boolean,
    classOptions: List<String>,
    examOptions: List<String>,
    subjectOptions: List<String>,
    notesState: UiState<GeneratedNotes>,
    isSaved: Boolean,
    isSpeaking: Boolean,
    onGenerate: (NotesRequest) -> Unit,
    onRetry: () -> Unit,
    onSaveNotes: (Boolean) -> Unit,
    onCopyNotes: (GeneratedNotes) -> Unit,
    onShareNotes: (GeneratedNotes) -> Unit,
    onExportPdf: (GeneratedNotes) -> Unit,
    onToggleTts: (GeneratedNotes) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    var selectedClass by rememberSaveable { mutableStateOf(classOptions.firstOrNull() ?: "Class 10th") }
    var selectedExam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "CBSE Board") }
    var selectedSubject by rememberSaveable { mutableStateOf(subjectOptions.firstOrNull() ?: "General Science") }
    var topic by rememberSaveable { mutableStateOf("") }
    var selectedLanguage by rememberSaveable { mutableStateOf(StudyLanguage.ENGLISH) }
    var selectedNotesType by rememberSaveable { mutableStateOf(NotesType.HANDWRITTEN_DETAILED) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var markedFavorite by rememberSaveable { mutableStateOf(false) }
    var isFormExpanded by rememberSaveable { mutableStateOf(true) }

    val effectiveClass = if (selectedClass in classOptions) selectedClass else classOptions.firstOrNull().orEmpty()
    val effectiveExam = if (selectedExam in examOptions) selectedExam else examOptions.firstOrNull().orEmpty()
    val effectiveSubject = if (selectedSubject in subjectOptions) selectedSubject else subjectOptions.firstOrNull().orEmpty()
    val isLoading = notesState is UiState.Loading

    // Auto-collapse form & scroll into view when notes succeed so results appear immediately on screen
    LaunchedEffect(notesState) {
        when (notesState) {
            is UiState.Success -> {
                isFormExpanded = false
                listState.animateScrollToItem(0)
            }
            is UiState.Loading -> {
                listState.animateScrollToItem(if (isLiveAiEnabled) 1 else 2)
            }
            else -> Unit
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
                            Text(
                                text = "AI Handwritten Notes Generator",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isFormExpanded) {
                                    "Detailed topper-style handwritten notebook notes with red headings, highlights, definitions, tables & flowcharts."
                                } else {
                                    "${topic.ifBlank { effectiveSubject }} • $effectiveExam • ${selectedLanguage.label}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (notesState is UiState.Success) {
                            OutlinedButton(
                                onClick = { isFormExpanded = !isFormExpanded },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("notes_toggle_form_button")
                            ) {
                                Icon(
                                    imageVector = if (isFormExpanded) Icons.Filled.ExpandLess else Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (isFormExpanded) "Hide" else "New Topic")
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
                                    label = "Class (6th–12th)",
                                    options = classOptions,
                                    selectedOption = effectiveClass,
                                    onOptionSelected = { selectedClass = it },
                                    testTag = "notes_class_dropdown",
                                    modifier = Modifier.weight(1f)
                                )
                                DropdownPickerField(
                                    label = "Exam",
                                    options = examOptions,
                                    selectedOption = effectiveExam,
                                    onOptionSelected = { selectedExam = it },
                                    testTag = "notes_exam_dropdown",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            DropdownPickerField(
                                label = "Subject",
                                options = subjectOptions,
                                selectedOption = effectiveSubject,
                                onOptionSelected = { selectedSubject = it },
                                testTag = "notes_subject_dropdown"
                            )

                            OutlinedTextField(
                                value = topic,
                                onValueChange = {
                                    topic = it
                                    if (validationError != null) validationError = null
                                },
                                label = { Text("Chapter or Topic") },
                                placeholder = { Text("e.g., Fundamental Rights, Photosynthesis, Ohm's Law") },
                                singleLine = true,
                                isError = validationError != null,
                                supportingText = validationError?.let { err -> { Text(err) } },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("notes_topic_input")
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(suggestedTopics) { suggestion ->
                                    AssistChip(
                                        onClick = {
                                            topic = suggestion
                                            validationError = null
                                        },
                                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Text(
                                text = "Language",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StudyLanguage.entries.forEach { lang ->
                                    FilterChip(
                                        selected = selectedLanguage == lang,
                                        onClick = { selectedLanguage = lang },
                                        label = { Text(lang.label) }
                                    )
                                }
                            }

                            DropdownPickerField(
                                label = "Notes Type",
                                options = NotesType.entries.map { it.label },
                                selectedOption = selectedNotesType.label,
                                onOptionSelected = { label ->
                                    selectedNotesType = NotesType.entries.firstOrNull { it.label == label } ?: NotesType.HANDWRITTEN_DETAILED
                                },
                                testTag = "notes_type_dropdown"
                            )

                            Spacer(Modifier.height(4.dp))

                            val emptyTopicMsg = stringResource(R.string.error_empty_topic)
                            Button(
                                onClick = {
                                    if (topic.trim().isEmpty()) {
                                        validationError = emptyTopicMsg
                                    } else {
                                        keyboardController?.hide()
                                        validationError = null
                                        markedFavorite = false
                                        onGenerate(
                                            NotesRequest(
                                                classLevel = effectiveClass,
                                                exam = effectiveExam,
                                                subject = effectiveSubject,
                                                topic = topic.trim(),
                                                language = selectedLanguage,
                                                notesType = selectedNotesType
                                            )
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_notes_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Writing Handwritten Notes...", style = MaterialTheme.typography.labelLarge)
                                } else {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generate Handwritten Notes", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        when (notesState) {
            is UiState.Idle -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ready to write your detailed handwritten notebook",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Type any chapter above or tap a quick suggestion chip to generate detailed student handwritten notes in English, Hindi, or Hinglish — complete with ruled notebook pages, yellow keyword highlights, tables, flowcharts, and handwritten PDF export.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is UiState.Loading -> {
                item {
                    StudyLoadingCard(
                        title = "Writing your detailed handwritten notes...",
                        subtitle = "Preparing ruled notebook pages, definitions, highlights, comparison table & flowchart"
                    )
                }
            }

            is UiState.Error -> {
                item {
                    ErrorRetryBanner(
                        message = notesState.message,
                        onRetry = onRetry
                    )
                }
            }

            is UiState.Success -> {
                item {
                    GeneratedNotesView(
                        notes = notesState.data,
                        isSaved = isSaved,
                        isFavorite = markedFavorite,
                        isSpeaking = isSpeaking,
                        onSaveClick = { onSaveNotes(markedFavorite) },
                        onFavoriteClick = {
                            markedFavorite = !markedFavorite
                            onSaveNotes(markedFavorite)
                        },
                        onCopyClick = { onCopyNotes(notesState.data) },
                        onShareClick = { onShareNotes(notesState.data) },
                        onPdfClick = { onExportPdf(notesState.data) },
                        onTtsClick = { onToggleTts(notesState.data) }
                    )
                }
            }
        }

        item { SmartStudyFooter() }
    }
}
