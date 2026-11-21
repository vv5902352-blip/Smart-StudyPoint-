package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.analytics.DailyStreakWidgetInfo
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.noteCategory
import com.example.data.model.NoteCategory
import com.example.ui.components.BrandPillTag
import com.example.ui.components.DailyStudyStreakWidget
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.SmartStudyFooter
import com.example.ui.components.SmartStudyLogoBadge
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyLight

private data class QuickToolCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    searchQuery: String,
    searchHistory: List<SearchHistoryEntity>,
    allNotes: List<StudyNoteEntity>,
    recentNotes: List<StudyNoteEntity>,
    quizCount: Int,
    streakWidgetInfo: DailyStreakWidgetInfo,
    avgQuizAccuracy: Int,
    onToggleDarkMode: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onOpenProgress: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenChapterQnA: () -> Unit,
    onOpenNotesGenerator: () -> Unit,
    onOpenMcqGenerator: () -> Unit,
    onOpenQnAGenerator: () -> Unit,
    onOpenLibraryCategory: (NoteCategory?) -> Unit,
    onOpenDailyGk: () -> Unit,
    onOpenAiChatbot: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenNoteDetail: (StudyNoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults = remember(searchQuery, allNotes) {
        val q = searchQuery.trim()
        if (q.isBlank()) emptyList()
        else allNotes.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.topic.contains(q, ignoreCase = true) ||
                it.subject.contains(q, ignoreCase = true) ||
                it.exam.contains(q, ignoreCase = true) ||
                it.classLevel.contains(q, ignoreCase = true)
        }
    }

    val tools = remember(
        onOpenNotesGenerator,
        onOpenMcqGenerator,
        onOpenQnAGenerator,
        onOpenLibraryCategory
    ) {
        listOf(
            QuickToolCardData(
                title = "AI Notes Generator",
                subtitle = "Detailed handwritten notes",
                icon = Icons.Filled.AutoAwesome,
                testTag = "home_tool_ai_notes",
                onClick = onOpenNotesGenerator
            ),
            QuickToolCardData(
                title = "MCQ Generator",
                subtitle = "10 to 100 test Qs",
                icon = Icons.Filled.Quiz,
                testTag = "home_tool_mcq",
                onClick = onOpenMcqGenerator
            ),
            QuickToolCardData(
                title = "Q&A Generator",
                subtitle = "Short, Long & Exam Qs",
                icon = Icons.Filled.QuestionAnswer,
                testTag = "home_tool_qna",
                onClick = onOpenQnAGenerator
            ),
            QuickToolCardData(
                title = "Short Notes",
                subtitle = "Crisp exam pointers",
                icon = Icons.Filled.Summarize,
                testTag = "home_tool_short_notes",
                onClick = { onOpenLibraryCategory(NoteCategory.SHORT_NOTES) }
            ),
            QuickToolCardData(
                title = "Important Questions",
                subtitle = "High-weightage picks",
                icon = Icons.Filled.Star,
                testTag = "home_tool_important_qs",
                onClick = { onOpenLibraryCategory(NoteCategory.IMPORTANT_QUESTIONS) }
            ),
            QuickToolCardData(
                title = "Revision Notes",
                subtitle = "Last-day booster sheets",
                icon = Icons.Filled.Bolt,
                testTag = "home_tool_revision",
                onClick = { onOpenLibraryCategory(NoteCategory.REVISION_NOTES) }
            ),
            QuickToolCardData(
                title = "Previous Year Qs",
                subtitle = "CBSE, UPSC, SSC, BPSC",
                icon = Icons.Filled.HistoryEdu,
                testTag = "home_tool_pyq",
                onClick = { onOpenLibraryCategory(NoteCategory.PYQ) }
            ),
            QuickToolCardData(
                title = "Saved Notes",
                subtitle = "Offline study library",
                icon = Icons.Filled.Bookmark,
                testTag = "home_tool_saved",
                onClick = { onOpenLibraryCategory(null) }
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Brand Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    SmartStudyLogoBadge(size = 46.dp)
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onOpenProgress,
                        modifier = Modifier.testTag("home_progress_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = stringResource(R.string.cd_open_progress),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("toggle_dark_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = stringResource(R.string.cd_toggle_theme),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onOpenAdmin,
                        modifier = Modifier.testTag("home_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = stringResource(R.string.cd_open_admin),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Welcome Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1.25f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BrandPillTag(
                                text = "CBSE • UPSC • SSC • RRB • BPSC",
                                containerColor = PureWhite.copy(alpha = 0.16f),
                                contentColor = SkyLight
                            )
                            Text(
                                text = "Welcome, Future Topper!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PureWhite
                            )
                            Text(
                                text = "Generate detailed AI handwritten notes, practice MCQs, and revise faster in Hindi, English & Hinglish.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyLight
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(0.85f)
                                .height(104.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = PureWhite
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_study_hero_1795188294432),
                                contentDescription = stringResource(R.string.cd_hero_banner),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenProgress)
                            .testTag("home_hero_metrics_progress"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroMetricChip(
                            label = "Study Library",
                            value = "${allNotes.size} Notes",
                            modifier = Modifier.weight(1f)
                        )
                        HeroMetricChip(
                            label = "Quiz Avg",
                            value = if (quizCount > 0) "$avgQuizAccuracy%" else "$quizCount Tests",
                            modifier = Modifier.weight(1f)
                        )
                        HeroMetricChip(
                            label = "Streak",
                            value = "${streakWidgetInfo.currentStreakDays} Days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Daily Study Streak Circular Progress Widget
        item {
            DailyStudyStreakWidget(
                info = streakWidgetInfo,
                onClick = onOpenProgress
            )
        }

        // Search Bar + History
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_clear_search)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit(searchQuery) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar")
                )

                // Live Search Matches
                if (searchQuery.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Search Results (${searchResults.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (searchResults.isEmpty()) {
                                Text(
                                    text = "No saved notes matched \"$searchQuery\". Generate custom AI notes for this topic!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = {
                                    onSearchSubmit(searchQuery)
                                    onOpenNotesGenerator()
                                }) {
                                    Text("Generate Notes with AI Studio")
                                }
                            } else {
                                searchResults.take(4).forEach { note ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onSearchSubmit(searchQuery)
                                                onOpenNoteDetail(note)
                                            },
                                        color = MaterialTheme.colorScheme.surfaceContainerLow
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = note.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${note.exam} • ${note.subject} • ${note.classLevel}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = onClearSearchHistory,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchHistory, key = { it.id }) { item ->
                            AssistChip(
                                onClick = { onSearchQueryChange(item.query) },
                                label = { Text(item.query, maxLines = 1) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove search item",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onDeleteHistoryItem(item.id) }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Student Progress & Trends Spotlight Card
        item {
            Card(
                onClick = onOpenProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_tool_student_progress"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = "Student Progress",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Student Progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${streakWidgetInfo.currentStreakDays}d streak",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Quiz score trends ($avgQuizAccuracy% avg), ${allNotes.size} notes generated & 28-day streak calendar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Student Progress",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Featured Upgrade Banner: Chapter-wise Objective & Subjective Q&A (Notebook View)
        item {
            Card(
                onClick = onOpenChapterQnA,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_tool_chapter_qna"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "Chapter-wise Q&A Notebook",
                            tint = PureWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BrandPillTag(
                            text = "NEW • NOTEBOOK VIEW",
                            containerColor = RoyalBlue,
                            contentColor = PureWhite
                        )
                        Text(
                            text = "Chapter-wise Objective & Subjective Q&A",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "MCQs, T/F, Fill-in-Blanks, Match, 1–6 Mark Answers, Diagrams, Mind Maps & PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Chapter-wise Q&A Generator",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Flashcard Active Recall Spotlight Card
        item {
            Card(
                onClick = onOpenFlashcards,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_tool_flashcards"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Style,
                            contentDescription = "Flashcards Study Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Flashcards — Active Recall",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Create custom term & definition decks, flip cards & track mastery offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Flashcards Study Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Core Study Dashboard Grid (8 tools)
        item {
            SectionHeaderRow(
                title = "Study Tools",
                subtitle = "Everything you need for school & competitive exams"
            )
        }

        items(tools.chunked(2)) { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowTools.forEach { tool ->
                    QuickToolTile(
                        tool = tool,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTools.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        // Daily GK Quiz & AI Tutor Duo Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureHighlightCard(
                    title = "Daily GK Quiz",
                    subtitle = "10 Indian exam Qs",
                    icon = Icons.Filled.EmojiEvents,
                    gradientColors = listOf(RoyalBlue, SkyAccent),
                    testTag = "home_daily_gk_card",
                    onClick = onOpenDailyGk,
                    modifier = Modifier.weight(1f)
                )
                FeatureHighlightCard(
                    title = "AI Study Tutor",
                    subtitle = "Ask doubts 24×7",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    gradientColors = listOf(NavyDeep, RoyalBlue),
                    testTag = "home_ai_chatbot_card",
                    onClick = onOpenAiChatbot,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Notes Section
        if (recentNotes.isNotEmpty()) {
            item {
                SectionHeaderRow(
                    title = "Recent Notes",
                    subtitle = "Continue where you left off",
                    actionLabel = "View All",
                    onActionClick = { onOpenLibraryCategory(null) }
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(recentNotes.take(6), key = { it.id }) { note ->
                        RecentNoteCard(
                            note = note,
                            onClick = { onOpenNoteDetail(note) }
                        )
                    }
                }
            }
        }

        // Brand Footer
        item {
            SmartStudyFooter()
        }
    }
}

@Composable
private fun HeroMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PureWhite.copy(alpha = 0.14f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SkyLight)
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = PureWhite)
        }
    }
}

@Composable
private fun QuickToolTile(
    tool: QuickToolCardData,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = tool.onClick,
        modifier = modifier.testTag(tool.testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tool.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = PureWhite, modifier = Modifier.size(20.dp))
            }
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = PureWhite)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = SkyLight)
        }
    }
}

@Composable
private fun RecentNoteCard(
    note: StudyNoteEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(230.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandPillTag(note.noteCategory().label)
                Text(
                    text = note.exam,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${note.subject} • ${note.classLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
