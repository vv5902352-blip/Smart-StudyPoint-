package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GeneratedNotes
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NotebookBlackInkLight
import com.example.ui.theme.NotebookBlueInkLight
import com.example.ui.theme.NotebookHeadingRedLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratedNotesView(
    notes: GeneratedNotes,
    isSaved: Boolean,
    isFavorite: Boolean,
    isSpeaking: Boolean,
    onSaveClick: (() -> Unit)?,
    onFavoriteClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onPdfClick: () -> Unit,
    onTtsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var useHandwrittenNotebook by rememberSaveable { mutableStateOf(true) }
    var selectedInkStyle by rememberSaveable { mutableStateOf(PenInkStyle.TOPPER_MULTI) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (notes.isSamplePreview) {
            AiSampleNoticeBanner()
        }

        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = notes.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrandPillTag(notes.classLevel)
                    BrandPillTag(
                        text = notes.exam,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                    BrandPillTag(notes.subject)
                    BrandPillTag(notes.language.label)
                    BrandPillTag(notes.notesType.label)
                }
            }
        }

        // Actions Toolbar
        NoteActionsBar(
            isSaved = isSaved,
            isFavorite = isFavorite,
            isSpeaking = isSpeaking,
            onSaveClick = onSaveClick,
            onFavoriteClick = onFavoriteClick,
            onCopyClick = onCopyClick,
            onShareClick = onShareClick,
            onPdfClick = onPdfClick,
            onTtsClick = onTtsClick
        )

        // Handwritten Notebook / Clean Digital View Mode & Pen Ink Switcher
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = useHandwrittenNotebook,
                        onClick = { useHandwrittenNotebook = true },
                        label = { Text("Handwritten Notebook") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Draw,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("notes_mode_handwritten")
                    )
                    FilterChip(
                        selected = !useHandwrittenNotebook,
                        onClick = { useHandwrittenNotebook = false },
                        label = { Text("Clean Digital") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Article,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("notes_mode_digital")
                    )
                }

                if (useHandwrittenNotebook) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PenInkStyle.entries.forEach { ink ->
                            val dotColor = when (ink) {
                                PenInkStyle.TOPPER_MULTI -> NotebookHeadingRedLight
                                PenInkStyle.BLUE_BALLPEN -> NotebookBlueInkLight
                                PenInkStyle.BLACK_GEL -> NotebookBlackInkLight
                            }
                            FilterChip(
                                selected = selectedInkStyle == ink,
                                onClick = { selectedInkStyle = ink },
                                label = { Text(ink.label, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                },
                                modifier = Modifier.testTag("ink_style_${ink.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        if (useHandwrittenNotebook) {
            HandwrittenNotesSheet(
                notes = notes,
                inkStyle = selectedInkStyle
            )
        } else {
        // 1. Detailed Notes (Clean Digital View)
        StudySectionCard(
            title = "Detailed Notes",
            icon = Icons.Filled.Description
        ) {
            Text(
                text = notes.detailedNotes,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 2. Short Notes
        if (notes.shortNotes.isNotEmpty()) {
            StudySectionCard(
                title = "Short Notes",
                icon = Icons.Filled.FormatListBulleted
            ) {
                BulletPointsList(notes.shortNotes)
            }
        }

        // 3. Easy Explanation
        if (notes.easyExplanation.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Easy Explanation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = notes.easyExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 4. Important Definitions
        if (notes.definitions.isNotEmpty()) {
            StudySectionCard(
                title = "Important Definitions",
                icon = Icons.Filled.MenuBook
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    notes.definitions.forEach { def ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = def.term,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = def.definition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Important Facts
        if (notes.importantFacts.isNotEmpty()) {
            StudySectionCard(
                title = "Important Facts",
                icon = Icons.Filled.AutoAwesome
            ) {
                BulletPointsList(notes.importantFacts)
            }
        }

        // 6. Comparison Table
        if (notes.comparisonTable.headers.isNotEmpty() && notes.comparisonTable.rows.isNotEmpty()) {
            StudySectionCard(
                title = notes.comparisonTable.title,
                icon = Icons.Filled.TableChart
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavyDeep)
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            notes.comparisonTable.headers.forEach { header ->
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                        notes.comparisonTable.rows.forEachIndexed { idx, row ->
                            val rowBg = if (idx % 2 == 0) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                row.forEachIndexed { colIdx, cell ->
                                    Text(
                                        text = cell,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (colIdx == 0) FontWeight.SemiBold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            if (idx < notes.comparisonTable.rows.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }

        // 7. Flowchart Steps
        if (notes.flowchartSteps.isNotEmpty()) {
            StudySectionCard(
                title = "Concept Flowchart",
                icon = Icons.Filled.AccountTree
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    notes.flowchartSteps.forEachIndexed { index, step ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (index < notes.flowchartSteps.lastIndex) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 8. Examples
        if (notes.examples.isNotEmpty()) {
            StudySectionCard(
                title = "Examples & Illustrations",
                icon = Icons.Filled.School
            ) {
                BulletPointsList(notes.examples)
            }
        }

        // 9. Exam-Oriented Points
        if (notes.examOrientedPoints.isNotEmpty()) {
            StudySectionCard(
                title = "Exam-Oriented Scoring Points",
                icon = Icons.Filled.FactCheck
            ) {
                BulletPointsList(notes.examOrientedPoints)
            }
        }

        // 10. Quick Revision Card
        if (notes.quickRevision.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = SkyLight
                        )
                        Text(
                            text = "Quick Revision Capsule",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                    }
                    notes.quickRevision.forEach { point ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SkyLight,
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(16.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun StudySectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun BulletPointsList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
