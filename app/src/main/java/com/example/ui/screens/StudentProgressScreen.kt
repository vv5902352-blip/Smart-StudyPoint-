package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.analytics.ProgressTimeRange
import com.example.data.analytics.StudentProgressCalculator
import com.example.data.analytics.SubjectMasteryItem
import com.example.data.local.FavoriteQuestionEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyNoteEntity
import com.example.ui.components.BrandPillTag
import com.example.ui.components.NotesCategoryDonutChart
import com.example.ui.components.NotesGeneratedBarChart
import com.example.ui.components.QuizPerformanceLineChart
import com.example.ui.components.SmartStudyFooter
import com.example.ui.components.StudyStreakHeatmap
import com.example.ui.theme.AmberHighlight
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProgressScreen(
    allNotes: List<StudyNoteEntity>,
    quizAttempts: List<QuizAttemptEntity>,
    favoriteQuestions: List<FavoriteQuestionEntity>,
    appUsageDayKeys: Set<String> = emptySet(),
    onBack: () -> Unit,
    onStartPracticeQuiz: () -> Unit,
    onGenerateNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedRange by rememberSaveable { mutableStateOf(ProgressTimeRange.DAYS_14) }
    var showCumulativeNotes by rememberSaveable { mutableStateOf(false) }

    val summary = remember(allNotes, quizAttempts, favoriteQuestions, appUsageDayKeys, selectedRange) {
        StudentProgressCalculator.calculate(
            notes = allNotes,
            quizAttempts = quizAttempts,
            favoriteQuestions = favoriteQuestions,
            appUsageDayKeys = appUsageDayKeys,
            timeRange = selectedRange
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.student_progress_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.student_progress_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("progress_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("student_progress_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Study Streak Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
                        .padding(20.dp)
                        .testTag("progress_streak_hero")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(AmberHighlight.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Study streak flame",
                                        tint = AmberHighlight,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${summary.currentStreakDays}-Day Study Streak",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (summary.studiedToday) {
                                            "Today's study session logged. Keep going!"
                                        } else {
                                            "Generate notes or complete a quiz today to extend your streak."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SkyLight
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StreakStatPill(
                                label = "Longest Streak",
                                value = "${summary.longestStreakDays} days",
                                modifier = Modifier.weight(1f)
                            )
                            StreakStatPill(
                                label = "Active Days",
                                value = "${summary.totalActiveDays} total",
                                modifier = Modifier.weight(1f)
                            )
                            StreakStatPill(
                                label = "Today",
                                value = if (summary.studiedToday) "Active" else "Pending",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Time Range Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ProgressTimeRange.entries) { range ->
                        FilterChip(
                            selected = selectedRange == range,
                            onClick = { selectedRange = range },
                            label = { Text(range.label) },
                            modifier = Modifier.testTag("progress_range_${range.name.lowercase()}")
                        )
                    }
                }
            }

            // 3. KPI Metric Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProgressKpiCard(
                            title = "Avg Quiz Score",
                            value = "${summary.averageAccuracyPercent}%",
                            subtitle = when {
                                summary.accuracyDeltaPercent > 0 -> "+${summary.accuracyDeltaPercent}% recent gain"
                                summary.accuracyDeltaPercent < 0 -> "${summary.accuracyDeltaPercent}% vs earlier"
                                else -> "${summary.totalQuestionsAttempted} Qs attempted"
                            },
                            icon = if (summary.accuracyDeltaPercent < 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = if (summary.accuracyDeltaPercent < 0) CrimsonError else EmeraldSuccess,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_avg_quiz_score")
                        )
                        ProgressKpiCard(
                            title = "Notes Generated",
                            value = "${summary.totalNotesGenerated}",
                            subtitle = "${summary.notesInRange} in ${selectedRange.label.lowercase()} • ${summary.favoriteNotesCount} ★",
                            icon = Icons.Filled.AutoAwesome,
                            accentColor = RoyalBlue,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_notes_generated")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProgressKpiCard(
                            title = "Quizzes Completed",
                            value = "${summary.totalQuizzesCompleted}",
                            subtitle = "Best Score: ${summary.bestScorePercent}%",
                            icon = Icons.Filled.EmojiEvents,
                            accentColor = AmberHighlight,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_quizzes_completed")
                        )
                        ProgressKpiCard(
                            title = "Saved Q&A Bookmarks",
                            value = "${summary.savedQuestionsCount}",
                            subtitle = "Revision highlights saved",
                            icon = Icons.Filled.Bookmark,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_saved_questions")
                        )
                    }
                }
            }

            // 4. Historical Quiz Performance Line Chart Card
            item {
                ProgressChartCard(
                    title = "Quiz Performance Trend",
                    subtitle = "Accuracy % over time (${summary.quizTrendPoints.size} attempts in ${selectedRange.label})",
                    badgeText = if (summary.accuracyDeltaPercent > 0) "+${summary.accuracyDeltaPercent}% Trend" else null,
                    badgeColor = EmeraldSuccess
                ) {
                    QuizPerformanceLineChart(
                        points = summary.quizTrendPoints,
                        averageAccuracy = summary.averageAccuracyPercent,
                        modifier = Modifier.testTag("chart_quiz_performance")
                    )
                }
            }

            // 5. Notes Generated Bar / Cumulative Growth Chart Card
            item {
                ProgressChartCard(
                    title = "Notes Generated Trend",
                    subtitle = if (showCumulativeNotes) "Cumulative study notes growth" else "New study notes created per period",
                    trailingHeader = {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !showCumulativeNotes,
                                onClick = { showCumulativeNotes = false },
                                label = { Text("Periodic", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("notes_chart_mode_periodic")
                            )
                            FilterChip(
                                selected = showCumulativeNotes,
                                onClick = { showCumulativeNotes = true },
                                label = { Text("Cumulative", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("notes_chart_mode_cumulative")
                            )
                        }
                    }
                ) {
                    NotesGeneratedBarChart(
                        buckets = summary.notesTrendBuckets,
                        showCumulative = showCumulativeNotes,
                        modifier = Modifier.testTag("chart_notes_generated")
                    )
                }
            }

            // 6. 28-Day Study Streak Activity Heatmap
            item {
                ProgressChartCard(
                    title = "28-Day Study Streak Calendar",
                    subtitle = "Daily study consistency across quizzes, notes & saved questions"
                ) {
                    StudyStreakHeatmap(
                        days = summary.heatmapDays,
                        modifier = Modifier.testTag("chart_study_streak_heatmap")
                    )
                }
            }

            // 7. Study Notes Category Distribution Donut
            item {
                ProgressChartCard(
                    title = "Study Library Distribution",
                    subtitle = "Breakdown of your saved notes by exam category"
                ) {
                    NotesCategoryDonutChart(
                        slices = summary.categoryBreakdown,
                        totalNotes = summary.totalNotesGenerated,
                        modifier = Modifier.testTag("chart_notes_category_donut")
                    )
                }
            }

            // 8. Subject Mastery Progress Bars
            if (summary.subjectMastery.isNotEmpty()) {
                item {
                    ProgressChartCard(
                        title = "Subject Mastery Overview",
                        subtitle = "Quiz accuracy & note coverage across subjects"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            summary.subjectMastery.forEach { item ->
                                SubjectMasteryRow(item = item)
                            }
                        }
                    }
                }
            }

            // 9. Recent Quiz Attempts Log
            if (summary.quizTrendPoints.isNotEmpty()) {
                item {
                    ProgressChartCard(
                        title = "Recent Quiz History",
                        subtitle = "Latest completed practice tests & Daily GK quizzes"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            summary.quizTrendPoints.asReversed().take(6).forEachIndexed { idx, pt ->
                                if (idx > 0) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (pt.isDailyGk) Icons.Filled.EmojiEvents else Icons.Filled.Quiz,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pt.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${pt.subject} • ${pt.exam} • ${pt.dateLabel}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${pt.scorePercent}%",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pt.scorePercent >= 75) EmeraldSuccess else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${pt.correctAnswers}/${pt.totalQuestions}",
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

            // 10. Quick Action CTA Row to keep building the streak
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartPracticeQuiz,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("progress_cta_take_quiz"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Take Quiz")
                    }
                    OutlinedButton(
                        onClick = onGenerateNotes,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("progress_cta_generate_notes"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("New Notes")
                    }
                }
            }

            item { SmartStudyFooter() }
        }
    }
}

@Composable
private fun StreakStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PureWhite.copy(alpha = 0.14f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SkyLight, maxLines = 1)
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = PureWhite, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun ProgressKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProgressChartCard(
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = EmeraldSuccess,
    trailingHeader: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (badgeText != null) {
                    Spacer(Modifier.width(8.dp))
                    BrandPillTag(
                        text = badgeText,
                        containerColor = badgeColor.copy(alpha = 0.15f),
                        contentColor = badgeColor
                    )
                }
            }
            if (trailingHeader != null) {
                trailingHeader()
            }
            content()
        }
    }
}

@Composable
private fun SubjectMasteryRow(item: SubjectMasteryItem) {
    val progress = if (item.quizzesTaken > 0) (item.averageAccuracy / 100f).coerceIn(0f, 1f) else 0.15f
    val barColor = when {
        item.quizzesTaken == 0 -> MaterialTheme.colorScheme.secondary
        item.averageAccuracy >= 80 -> EmeraldSuccess
        item.averageAccuracy >= 60 -> RoyalBlue
        else -> AmberHighlight
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (item.averageAccuracy >= 80) Icons.Filled.CheckCircle else Icons.Filled.School,
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (item.quizzesTaken > 0) {
                    "${item.averageAccuracy}% • ${item.quizzesTaken} quiz • ${item.notesCount} notes"
                } else {
                    "${item.notesCount} notes saved"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}
