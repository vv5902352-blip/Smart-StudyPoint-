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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.FavoriteQuestionEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.model.ChapterQnARequest
import com.example.data.model.ChapterStudyPack
import com.example.data.model.MarksFilter
import com.example.data.model.ObjectiveQuestionItem
import com.example.data.model.StudyLanguage
import com.example.data.model.SubjectiveCategory
import com.example.data.model.SubjectiveQuestionItem
import com.example.data.model.UiState
import com.example.ui.components.AiSampleNoticeBanner
import com.example.ui.components.BrandPillTag
import com.example.ui.components.DropdownPickerField
import com.example.ui.components.ErrorRetryBanner
import com.example.ui.components.HandwrittenBlackQuestion
import com.example.ui.components.HandwrittenBlueAnswer
import com.example.ui.components.HandwrittenRedHeading
import com.example.ui.components.LabeledScientificDiagram
import com.example.ui.components.NotebookComparisonTable
import com.example.ui.components.NotebookDefinitionBox
import com.example.ui.components.NotebookFlowchart
import com.example.ui.components.NotebookMindMap
import com.example.ui.components.NotebookPageCard
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
import com.example.viewmodel.ChapterTestReport

private val chapterQuantityOptions = listOf(10, 20, 30, 50, 100)
private val featuredSampleChapters = listOf(
    Triple("Class 10th", "Biology", "Life Processes"),
    Triple("Class 10th", "Physics", "Electricity"),
    Triple("Class 10th", "General Science", "जैव प्रक्रम (Life Processes)"),
    Triple("Class 12th", "Indian Polity", "Indian Constitution & Rights")
)

private enum class ChapterSectionTab(val label: String) {
    INTRO("1. Intro"),
    OBJECTIVES("2. Objective Qs"),
    ANSWER_KEY("3. Answer Key"),
    SHORT_ANS("4. Short Q&A"),
    LONG_ANS("5. Long Q&A"),
    DIAGRAMS("6. Diagrams"),
    IMPORTANT("7. Important"),
    REVISION("8. Revision"),
    FAVORITES("★ Saved Qs")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterQnAScreen(
    isLiveAiEnabled: Boolean,
    classOptions: List<String>,
    examOptions: List<String>,
    subjectOptions: List<String>,
    packState: UiState<ChapterStudyPack>,
    isPracticeMode: Boolean,
    selectedAnswers: Map<Int, Int>,
    isTestSubmitted: Boolean,
    isGeneratingMore: Boolean,
    marksFilter: MarksFilter,
    favoriteQuestions: List<FavoriteQuestionEntity>,
    performanceHistory: List<QuizAttemptEntity>,
    currentReport: ChapterTestReport?,
    onGeneratePack: (ChapterQnARequest) -> Unit,
    onRetry: () -> Unit,
    onGenerateMore: () -> Unit,
    onSetPracticeMode: (Boolean) -> Unit,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmitTest: () -> Unit,
    onResetTest: () -> Unit,
    onSetMarksFilter: (MarksFilter) -> Unit,
    onToggleFavoriteQuestion: (String, String, String, String, String, String, String) -> Unit,
    onDeleteFavoriteQuestion: (Long) -> Unit,
    onCopyText: (String, String) -> Unit,
    onShareText: (String, String) -> Unit,
    onExportPdf: (ChapterStudyPack) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val schoolClasses = classOptions.filter { it.startsWith("Class") }.ifEmpty {
        listOf("Class 6th", "Class 7th", "Class 8th", "Class 9th", "Class 10th", "Class 11th", "Class 12th")
    }
    var selectedClass by rememberSaveable { mutableStateOf("Class 10th") }
    var selectedSubject by rememberSaveable { mutableStateOf("General Science") }
    var selectedBoard by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "CBSE Board") }
    var chapterName by rememberSaveable { mutableStateOf("Life Processes") }
    var selectedLanguage by rememberSaveable { mutableStateOf(StudyLanguage.ENGLISH) }
    var selectedQty by rememberSaveable { mutableIntStateOf(10) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var isFormExpanded by rememberSaveable { mutableStateOf(true) }
    var activeSection by rememberSaveable { mutableStateOf(ChapterSectionTab.INTRO) }

    val effectiveClass = if (selectedClass in schoolClasses) selectedClass else schoolClasses.first()
    val effectiveSubject = if (selectedSubject in subjectOptions) selectedSubject else subjectOptions.firstOrNull() ?: "Science"
    val effectiveBoard = if (selectedBoard in examOptions) selectedBoard else examOptions.firstOrNull() ?: "CBSE Board"
    val isLoading = packState is UiState.Loading

    val favoriteQuestionSet = favoriteQuestions.map { it.questionText }.toSet()

    val packKey = (packState as? UiState.Success)?.data?.request?.let {
        "${it.chapterName}_${it.language}_${it.objectiveCount}"
    }
    LaunchedEffect(packKey) {
        if (packKey != null) {
            isFormExpanded = false
            activeSection = ChapterSectionTab.OBJECTIVES
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!isLiveAiEnabled) {
            item { AiSampleNoticeBanner() }
        }

        // 1. Student Input Form Card
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
                                text = "Chapter-wise Objective & Subjective Q&A",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isFormExpanded) {
                                    "Notebook-style answers with highlights, tables, flowcharts, mind maps & labeled diagrams."
                                } else {
                                    "$chapterName • $effectiveClass • $effectiveSubject (${selectedLanguage.label})"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (packState is UiState.Success) {
                            OutlinedButton(
                                onClick = { isFormExpanded = !isFormExpanded },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("chapter_toggle_form_button")
                            ) {
                                Icon(
                                    imageVector = if (isFormExpanded) Icons.Filled.ExpandLess else Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (isFormExpanded) "Hide" else "Change")
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
                                    label = "Class (6–12)",
                                    options = schoolClasses,
                                    selectedOption = effectiveClass,
                                    onOptionSelected = { selectedClass = it },
                                    modifier = Modifier.weight(1f)
                                )
                                DropdownPickerField(
                                    label = "Board / Exam",
                                    options = examOptions,
                                    selectedOption = effectiveBoard,
                                    onOptionSelected = { selectedBoard = it },
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
                                value = chapterName,
                                onValueChange = {
                                    chapterName = it
                                    if (validationError != null) validationError = null
                                },
                                label = { Text("Chapter Name") },
                                placeholder = { Text("e.g., Life Processes, Electricity, Acid Bases & Salts") },
                                singleLine = true,
                                isError = validationError != null,
                                supportingText = validationError?.let { err -> { Text(err) } },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chapter_name_input")
                            )

                            // Quick Sample Chapter Chips (including Class 10 Science!)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(featuredSampleChapters) { (cls, subj, chap) ->
                                    AssistChip(
                                        onClick = {
                                            selectedClass = cls
                                            if (subj in subjectOptions) selectedSubject = subj
                                            chapterName = chap
                                            if (chap.contains("जैव")) selectedLanguage = StudyLanguage.HINDI
                                            validationError = null
                                        },
                                        label = { Text("$cls: $chap", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Text("Language", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(StudyLanguage.ENGLISH, StudyLanguage.HINDI).forEach { lang ->
                                    FilterChip(
                                        selected = selectedLanguage == lang,
                                        onClick = { selectedLanguage = lang },
                                        label = { Text(lang.label) },
                                        modifier = Modifier.testTag("chapter_lang_${lang.name.lowercase()}")
                                    )
                                }
                            }

                            Text("Objective Questions Quantity", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chapterQuantityOptions.forEach { qty ->
                                    FilterChip(
                                        selected = selectedQty == qty,
                                        onClick = { selectedQty = qty },
                                        label = { Text("$qty Qs") },
                                        modifier = Modifier.testTag("chapter_qty_$qty")
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (chapterName.trim().isEmpty()) {
                                        validationError = "Please enter a chapter name to generate questions."
                                    } else {
                                        keyboardController?.hide()
                                        validationError = null
                                        onGeneratePack(
                                            ChapterQnARequest(
                                                classLevel = effectiveClass,
                                                subject = effectiveSubject,
                                                chapterName = chapterName.trim(),
                                                boardExam = effectiveBoard,
                                                language = selectedLanguage,
                                                objectiveCount = selectedQty
                                            )
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_chapter_qna_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generating Chapter Pack...")
                                } else {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generate Chapter Q&A Pack ($selectedQty Obj + Subj)")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loading / Error / Results
        when (packState) {
            is UiState.Idle -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Try Class 10 Science — Life Processes right away!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tap 'Generate Chapter Q&A Pack' above to explore all 8 chapter sections: MCQs, True/False, Fill in the Blanks, Match the Following, One-Word Answers, Notebook-style Subjective Answers with Yellow Highlights, Tables, Flowcharts, Mind Maps & Labeled Diagrams.",
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
                        title = "Preparing Chapter-wise Notebook Q&A...",
                        subtitle = "Generating $selectedQty Objective Qs + Subjective Answers, Diagrams & Mind Maps"
                    )
                }
            }

            is UiState.Error -> {
                item { ErrorRetryBanner(message = packState.message, onRetry = onRetry) }
            }

            is UiState.Success -> {
                val pack = packState.data

                // Export & Mode Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = !isPracticeMode,
                                onClick = { onSetPracticeMode(false) },
                                shape = SegmentedButtonDefaults.itemShape(0, 2),
                                modifier = Modifier.testTag("chapter_study_mode_btn")
                            ) {
                                Text("Study Mode")
                            }
                            SegmentedButton(
                                selected = isPracticeMode,
                                onClick = { onSetPracticeMode(true) },
                                shape = SegmentedButtonDefaults.itemShape(1, 2),
                                modifier = Modifier.testTag("chapter_practice_mode_btn")
                            ) {
                                Text("Practice Mode")
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onExportPdf(pack) },
                                modifier = Modifier.testTag("chapter_pdf_button")
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Download PDF")
                            }
                            OutlinedButton(
                                onClick = { onCopyText(pack.request.chapterName, pack.toPlainText()) }
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Copy All")
                            }
                            OutlinedButton(
                                onClick = { onShareText("${pack.request.chapterName} Q&A", pack.toPlainText()) }
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share")
                            }
                            OutlinedButton(
                                onClick = onGenerateMore,
                                enabled = !isGeneratingMore,
                                modifier = Modifier.testTag("chapter_generate_more_btn")
                            ) {
                                Icon(Icons.Filled.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isGeneratingMore) "Adding..." else "+10 More Qs")
                            }
                        }
                    }
                }

                // 8-Section + Saved Favourites Navigation Pills
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ChapterSectionTab.entries) { tab ->
                            val badge = when (tab) {
                                ChapterSectionTab.OBJECTIVES -> " (${pack.objectiveQuestions.size})"
                                ChapterSectionTab.FAVORITES -> " (${favoriteQuestions.size})"
                                else -> ""
                            }
                            FilterChip(
                                selected = activeSection == tab,
                                onClick = { activeSection = tab },
                                label = { Text("${tab.label}$badge", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("chapter_sec_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                // Render Active Chapter Section
                when (activeSection) {
                    ChapterSectionTab.INTRO -> {
                        item {
                            NotebookPageCard {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    HandwrittenRedHeading("1. Chapter Introduction — ${pack.request.chapterName}")
                                    HandwrittenBlueAnswer(
                                        text = pack.chapterIntro.overview,
                                        keywords = pack.chapterIntro.keyTopics
                                    )
                                    NotebookDefinitionBox(pack.chapterIntro.definitionBox)
                                    Text(
                                        text = "Key Topics Covered:",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pack.chapterIntro.keyTopics.forEach { kt -> BrandPillTag(kt) }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    ) {
                                        Text(
                                            text = pack.chapterIntro.examWeightageTip,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ChapterSectionTab.OBJECTIVES -> {
                        if (isPracticeMode && isTestSubmitted && currentReport != null) {
                            item {
                                ChapterPracticeReportCard(
                                    report = currentReport,
                                    onRetake = onResetTest
                                )
                            }
                        }

                        items(pack.objectiveQuestions, key = { "obj_${it.number}" }) { q ->
                            val reveal = !isPracticeMode || isTestSubmitted
                            val isFav = q.question in favoriteQuestionSet
                            ObjectiveQuestionNotebookCard(
                                item = q,
                                selectedOption = selectedAnswers[q.number],
                                revealAnswer = reveal,
                                isFavorite = isFav,
                                onSelectOption = { opt -> onSelectAnswer(q.number, opt) },
                                onToggleFavorite = {
                                    onToggleFavoriteQuestion(
                                        pack.request.chapterName,
                                        pack.request.subject,
                                        pack.request.classLevel,
                                        q.kind.label,
                                        "${q.marks} Mark",
                                        q.question,
                                        "${q.correctAnswerText}\n${q.explanation}"
                                    )
                                },
                                onCopy = {
                                    onCopyText(
                                        "Q${q.number}",
                                        "Q${q.number}. ${q.question}\nAnswer: ${q.correctAnswerText}\nExplanation: ${q.explanation}"
                                    )
                                }
                            )
                        }

                        if (isPracticeMode && !isTestSubmitted) {
                            item {
                                Button(
                                    onClick = onSubmitTest,
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(vertical = 14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_chapter_practice_test")
                                ) {
                                    Text(
                                        text = "Submit Chapter Practice Test (${selectedAnswers.size}/${pack.objectiveQuestions.size} Attempted)",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    ChapterSectionTab.ANSWER_KEY -> {
                        item {
                            if (isPracticeMode && !isTestSubmitted) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Answer Key Hidden in Practice Mode", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "Attempt the objective questions and submit your practice test, or switch to Study Mode to view the complete Answer Key immediately.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        OutlinedButton(onClick = { onSetPracticeMode(false) }) {
                                            Text("Switch to Study Mode")
                                        }
                                    }
                                }
                            } else {
                                NotebookPageCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HandwrittenRedHeading("3. Objective Answer Key (${pack.objectiveQuestions.size} Questions)")
                                        pack.objectiveQuestions.forEach { q ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Q${q.number} [${q.kind.shortTag}]",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = q.correctAnswerText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 10.dp)
                                                )
                                            }
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ChapterSectionTab.SHORT_ANS -> {
                        item {
                            MarksFilterBar(selected = marksFilter, onSelect = onSetMarksFilter)
                        }
                        val filtered = filterSubjectiveByMarks(pack.shortAnswers, marksFilter)
                        items(filtered, key = { "short_${it.number}_${it.question.hashCode()}" }) { sq ->
                            SubjectiveNotebookCard(
                                item = sq,
                                isFavorite = sq.question in favoriteQuestionSet,
                                onToggleFavorite = {
                                    onToggleFavoriteQuestion(
                                        pack.request.chapterName,
                                        pack.request.subject,
                                        pack.request.classLevel,
                                        sq.category.label,
                                        "${sq.marks} Marks",
                                        sq.question,
                                        "${sq.answerParagraph}\n${sq.bulletPoints.joinToString("\n• ", "• ")}"
                                    )
                                },
                                onCopy = {
                                    onCopyText(
                                        sq.answerHeading,
                                        "Q${sq.number}. ${sq.question}\n${sq.answerParagraph}\n${sq.bulletPoints.joinToString("\n• ", "• ")}"
                                    )
                                }
                            )
                        }
                    }

                    ChapterSectionTab.LONG_ANS -> {
                        items(pack.longAnswers, key = { "long_${it.number}_${it.question.hashCode()}" }) { lq ->
                            SubjectiveNotebookCard(
                                item = lq,
                                isFavorite = lq.question in favoriteQuestionSet,
                                onToggleFavorite = {
                                    onToggleFavoriteQuestion(
                                        pack.request.chapterName,
                                        pack.request.subject,
                                        pack.request.classLevel,
                                        lq.category.label,
                                        "${lq.marks} Marks",
                                        lq.question,
                                        "${lq.answerParagraph}\n${lq.bulletPoints.joinToString("\n• ", "• ")}"
                                    )
                                },
                                onCopy = {
                                    onCopyText(
                                        lq.answerHeading,
                                        "Q${lq.number}. ${lq.question}\n${lq.answerParagraph}\n${lq.bulletPoints.joinToString("\n• ", "• ")}"
                                    )
                                }
                            )
                        }
                    }

                    ChapterSectionTab.DIAGRAMS -> {
                        items(pack.diagramQuestions, key = { "diag_${it.number}" }) { dq ->
                            NotebookPageCard {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BrandPillTag("Diagram Q${dq.number} • ${dq.marks} Marks")
                                        IconButton(
                                            onClick = {
                                                onToggleFavoriteQuestion(
                                                    pack.request.chapterName,
                                                    pack.request.subject,
                                                    pack.request.classLevel,
                                                    "Diagram Question",
                                                    "${dq.marks} Marks",
                                                    dq.question,
                                                    dq.answerPoints.joinToString("\n• ", "• ")
                                                )
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (dq.question in favoriteQuestionSet) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                contentDescription = "Save favourite diagram question",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    HandwrittenBlackQuestion("Q${dq.number}. ${dq.question}")
                                    LabeledScientificDiagram(
                                        kind = dq.diagramKind,
                                        caption = dq.diagramCaption,
                                        labels = dq.labels
                                    )
                                    HandwrittenRedHeading("Key Explanation Points:")
                                    dq.answerPoints.forEach { pt ->
                                        HandwrittenBlueAnswer("• $pt", keywords = dq.labels)
                                    }
                                }
                            }
                        }
                    }

                    ChapterSectionTab.IMPORTANT -> {
                        items(pack.mostImportantExamQuestions, key = { "imp_${it.number}_${it.question.hashCode()}" }) { iq ->
                            SubjectiveNotebookCard(
                                item = iq,
                                isFavorite = iq.question in favoriteQuestionSet,
                                onToggleFavorite = {
                                    onToggleFavoriteQuestion(
                                        pack.request.chapterName,
                                        pack.request.subject,
                                        pack.request.classLevel,
                                        "Most Important Exam Q",
                                        "${iq.marks} Marks",
                                        iq.question,
                                        "${iq.answerParagraph}\n${iq.bulletPoints.joinToString("\n• ", "• ")}"
                                    )
                                },
                                onCopy = {
                                    onCopyText(iq.answerHeading, "Q${iq.number}. ${iq.question}\n${iq.answerParagraph}")
                                }
                            )
                        }
                    }

                    ChapterSectionTab.REVISION -> {
                        item {
                            NotebookPageCard {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    HandwrittenRedHeading("8. Quick Revision One-Liners")
                                    pack.quickRevisionQuestions.forEach { rq ->
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            HandwrittenBlackQuestion("Q${rq.number}. ${rq.question}")
                                            HandwrittenBlueAnswer("Ans: ${rq.answer}", keywords = listOf(rq.answer))
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                        }
                        if (performanceHistory.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Recent Performance Tracking", style = MaterialTheme.typography.titleMedium)
                                        performanceHistory.take(5).forEach { attempt ->
                                            val pct = if (attempt.totalQuestions > 0) (attempt.correctAnswers * 100) / attempt.totalQuestions else 0
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = attempt.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${attempt.correctAnswers}/${attempt.totalQuestions} ($pct%)",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ChapterSectionTab.FAVORITES -> {
                        if (favoriteQuestions.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Text(
                                        text = "No favourite questions saved yet. Tap the heart icon on any Objective or Subjective question to bookmark it here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        } else {
                            items(favoriteQuestions, key = { it.id }) { fav ->
                                NotebookPageCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                BrandPillTag(fav.questionType)
                                                BrandPillTag(fav.marksLabel)
                                            }
                                            IconButton(
                                                onClick = { onDeleteFavoriteQuestion(fav.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove favourite")
                                            }
                                        }
                                        HandwrittenBlackQuestion(fav.questionText)
                                        HandwrittenBlueAnswer(fav.answerText)
                                        Text(
                                            text = "${fav.chapterName} • ${fav.subject} • ${fav.classLevel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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

private fun filterSubjectiveByMarks(
    items: List<SubjectiveQuestionItem>,
    filter: MarksFilter
): List<SubjectiveQuestionItem> = when (filter) {
    MarksFilter.ALL -> items
    MarksFilter.ONE_MARK -> items.filter { it.marks == 1 }
    MarksFilter.TWO_THREE_MARKS -> items.filter { it.marks in 2..3 }
    MarksFilter.FOUR_SIX_MARKS -> items.filter { it.marks >= 4 }
    MarksFilter.DIFFERENCES -> items.filter { it.category == SubjectiveCategory.DIFFERENCE_BETWEEN || it.comparisonTable != null }
}.ifEmpty { items }

@Composable
private fun MarksFilterBar(
    selected: MarksFilter,
    onSelect: (MarksFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(MarksFilter.entries) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
private fun ObjectiveQuestionNotebookCard(
    item: ObjectiveQuestionItem,
    selectedOption: Int?,
    revealAnswer: Boolean,
    isFavorite: Boolean,
    onSelectOption: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit
) {
    val letters = listOf("A", "B", "C", "D")
    NotebookPageCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BrandPillTag("Q${item.number}")
                    BrandPillTag(
                        text = item.kind.label,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Save favourite question",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy question",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HandwrittenBlackQuestion(item.question)

            // Render Match the Following Table if present
            if (item.matchPairs.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Column A", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Column B", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider()
                        item.matchPairs.forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(pair.leftItem, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(pair.rightItem, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOption == idx
                    val isCorrect = idx == item.correctOptionIndex
                    val bg = when {
                        revealAnswer && isCorrect -> EmeraldSoft
                        revealAnswer && isSelected && !isCorrect -> CrimsonSoft
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val border = when {
                        revealAnswer && isCorrect -> EmeraldSuccess
                        revealAnswer && isSelected && !isCorrect -> CrimsonError
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !revealAnswer) { onSelectOption(idx) },
                        shape = RoundedCornerShape(10.dp),
                        color = bg,
                        border = BorderStroke(1.2.dp, border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) RoyalBlue else MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letters.getOrElse(idx) { "${idx + 1}" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) PureWhite else NavyDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (revealAnswer && (isCorrect || isSelected)) NavyDeep else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (revealAnswer && isCorrect) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Correct", tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            } else if (revealAnswer && isSelected && !isCorrect) {
                                Icon(Icons.Filled.Close, contentDescription = "Wrong", tint = CrimsonError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (revealAnswer) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Correct Answer: ${item.correctAnswerText}",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldSuccess
                        )
                        HandwrittenBlueAnswer(
                            text = "Explanation: ${item.explanation}",
                            keywords = listOf(item.correctAnswerText)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectiveNotebookCard(
    item: SubjectiveQuestionItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit
) {
    NotebookPageCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BrandPillTag("${item.category.label} • ${item.marks}M")
                    BrandPillTag(
                        text = item.category.wordLimit,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Save favourite answer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy answer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HandwrittenBlackQuestion("Q${item.number}. ${item.question}")
            HandwrittenRedHeading("Ans: ${item.answerHeading}")
            HandwrittenBlueAnswer(
                text = item.answerParagraph,
                keywords = item.keywordsToHighlight
            )

            if (item.bulletPoints.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.bulletPoints.forEach { bullet ->
                        HandwrittenBlueAnswer(
                            text = "• $bullet",
                            keywords = item.keywordsToHighlight
                        )
                    }
                }
            }

            item.definitionBox?.let { NotebookDefinitionBox(it) }
            item.comparisonTable?.let { NotebookComparisonTable(it) }
            if (item.flowchartSteps.isNotEmpty()) {
                HandwrittenRedHeading("Process Flowchart:")
                NotebookFlowchart(item.flowchartSteps)
            }
            item.mindMap?.let { NotebookMindMap(it) }

            if (item.examples.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Example(s):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        item.examples.forEach { ex ->
                            HandwrittenBlueAnswer("★ $ex", keywords = item.keywordsToHighlight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterPracticeReportCard(
    report: ChapterTestReport,
    onRetake: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
            .padding(18.dp)
            .testTag("chapter_score_report_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = SkyLight, modifier = Modifier.size(26.dp))
                Column {
                    Text("Chapter Practice Test Report", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                    Text(
                        text = "Score: ${report.correctCount}/${report.totalQuestions} (${report.accuracyPercent}% Accuracy)",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyLight
                    )
                }
            }
            report.breakdownByKind.forEach { (kind, pair) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(kind.label, style = MaterialTheme.typography.bodySmall, color = SkyLight)
                    Text("${pair.first}/${pair.second} correct", style = MaterialTheme.typography.labelMedium, color = PureWhite)
                }
            }
            OutlinedButton(
                onClick = onRetake,
                border = BorderStroke(1.dp, SkyLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retake Chapter Test", color = PureWhite)
            }
        }
    }
}
