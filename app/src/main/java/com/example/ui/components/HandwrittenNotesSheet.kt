package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeneratedNotes
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HandwritingFontFamily
import com.example.ui.theme.MarkerYellowBg
import com.example.ui.theme.MindMapAmber
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PenInkStyle(val label: String) {
    TOPPER_MULTI("Topper Multi-Color"),
    BLUE_BALLPEN("Blue Pen"),
    BLACK_GEL("Black Gel")
}

/**
 * Renders generated study notes as an authentic Indian student handwritten notebook register
 * with ruled pages, double red margin, Kalam handwriting font (English & Hindi Devanagari),
 * yellow highlighter keywords, sticky-note teacher explanations, and V.V. IMP exam callouts.
 */
@Composable
fun HandwrittenNotesSheet(
    notes: GeneratedNotes,
    inkStyle: PenInkStyle,
    modifier: Modifier = Modifier
) {
    val headingColor = when (inkStyle) {
        PenInkStyle.TOPPER_MULTI -> notebookRedHeadingColor()
        PenInkStyle.BLUE_BALLPEN -> notebookBlueInkColor()
        PenInkStyle.BLACK_GEL -> notebookBlackInkColor()
    }
    val bodyInkColor = when (inkStyle) {
        PenInkStyle.TOPPER_MULTI -> notebookBlueInkColor()
        PenInkStyle.BLUE_BALLPEN -> notebookBlueInkColor()
        PenInkStyle.BLACK_GEL -> notebookBlackInkColor()
    }
    val accentInkColor = when (inkStyle) {
        PenInkStyle.TOPPER_MULTI -> notebookBlackInkColor()
        PenInkStyle.BLUE_BALLPEN -> notebookBlueInkColor()
        PenInkStyle.BLACK_GEL -> notebookBlackInkColor()
    }

    val highlightKeywords = remember(notes.definitions, notes.topic) {
        buildList {
            notes.definitions.forEach { def ->
                val cleanTerm = def.term.trim()
                if (cleanTerm.length >= 3) add(cleanTerm)
            }
            notes.topic.split(" ", ",", "&", "-", "(", ")")
                .map { it.trim() }
                .filter { it.length >= 5 }
                .take(4)
                .forEach { add(it) }
        }.distinct()
    }

    val todayLabel = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    val parsedLines = remember(notes.detailedNotes) {
        notes.detailedNotes.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("handwritten_notes_sheet"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PAGE 01: Notebook Register Cover + Detailed Handwritten Notes
        NotebookPageCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top Date / Page Header Box (classic student register style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${notes.subject} • ${notes.classLevel}",
                        fontFamily = HandwritingFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = accentInkColor
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, headingColor.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "Date: $todayLabel  |  Pg: 01",
                            fontFamily = HandwritingFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = headingColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                HorizontalDivider(color = headingColor.copy(alpha = 0.45f), thickness = 1.2.dp)

                // Handwritten Chapter Title
                Text(
                    text = notes.title,
                    fontFamily = HandwritingFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    textDecoration = TextDecoration.Underline,
                    color = headingColor
                )

                Text(
                    text = "Exam Focus: ${notes.exam}  •  ${notes.language.label}",
                    fontFamily = HandwritingFontFamily,
                    fontSize = 13.sp,
                    color = accentInkColor.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(2.dp))

                // Detailed Handwritten Content Lines
                parsedLines.forEach { line ->
                    val cleanedHeading = line.removePrefix("###").removePrefix("##").removePrefix("#").trim()
                    val isSectionHeading = line.startsWith("#") ||
                        Regex("^\\d+\\.\\s+.{2,68}$").matches(line) && !line.endsWith(".") ||
                        (line.length <= 56 && line.endsWith(":"))

                    when {
                        isSectionHeading -> {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = cleanedHeading,
                                fontFamily = HandwritingFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 25.sp,
                                textDecoration = TextDecoration.Underline,
                                color = headingColor
                            )
                        }
                        line.startsWith("•") || line.startsWith("- ") || line.startsWith("* ") -> {
                            val bulletBody = line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "→",
                                    fontFamily = HandwritingFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = headingColor
                                )
                                Text(
                                    text = buildHighlightedAnswer(bulletBody, highlightKeywords, bodyInkColor),
                                    fontFamily = HandwritingFontFamily,
                                    fontSize = 16.sp,
                                    lineHeight = 26.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = buildHighlightedAnswer(line, highlightKeywords, bodyInkColor),
                                fontFamily = HandwritingFontFamily,
                                fontSize = 16.sp,
                                lineHeight = 26.sp
                            )
                        }
                    }
                }

                // Easy Explanation Sticky Note inside Page 01
                if (notes.easyExplanation.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MarkerYellowBg.copy(alpha = if (isNotebookDark()) 0.18f else 0.5f),
                        border = BorderStroke(1.3.dp, MindMapAmber)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = headingColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Easy Teacher's Note (Samajhne ka Tarika)",
                                    fontFamily = HandwritingFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = headingColor
                                )
                            }
                            Text(
                                text = notes.easyExplanation,
                                fontFamily = HandwritingFontFamily,
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                color = accentInkColor
                            )
                        }
                    }
                }
            }
        }

        // PAGE 02: Important Definitions, Short Notes & Key Facts
        if (notes.definitions.isNotEmpty() || notes.shortNotes.isNotEmpty() || notes.importantFacts.isNotEmpty()) {
            NotebookPageCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NotebookPageNumberStrip(label = "Key Definitions & Bullet Notes", pageNum = "02", color = headingColor)

                    if (notes.definitions.isNotEmpty()) {
                        HandwrittenSectionHeading("★ Important Definitions", headingColor)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            notes.definitions.forEach { def ->
                                NotebookDefinitionBox(definition = def)
                            }
                        }
                    }

                    if (notes.shortNotes.isNotEmpty()) {
                        HandwrittenSectionHeading("✎ Short Notes (Quick Points)", headingColor)
                        notes.shortNotes.forEachIndexed { idx, pt ->
                            HandwrittenNumberedLine(
                                indexLabel = "${idx + 1}.",
                                text = pt,
                                keywords = highlightKeywords,
                                markerColor = headingColor,
                                inkColor = bodyInkColor
                            )
                        }
                    }

                    if (notes.importantFacts.isNotEmpty()) {
                        HandwrittenSectionHeading("✦ Important Facts & Values", headingColor)
                        notes.importantFacts.forEach { fact ->
                            HandwrittenNumberedLine(
                                indexLabel = "★",
                                text = fact,
                                keywords = highlightKeywords,
                                markerColor = headingColor,
                                inkColor = accentInkColor
                            )
                        }
                    }
                }
            }
        }

        // PAGE 03: Comparison Table, Concept Flowchart & Worked Examples
        val hasTable = notes.comparisonTable.headers.isNotEmpty() && notes.comparisonTable.rows.isNotEmpty()
        if (hasTable || notes.flowchartSteps.isNotEmpty() || notes.examples.isNotEmpty()) {
            NotebookPageCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    NotebookPageNumberStrip(label = "Table, Flowchart & Examples", pageNum = "03", color = headingColor)

                    if (hasTable) {
                        HandwrittenSectionHeading("▣ ${notes.comparisonTable.title}", headingColor)
                        NotebookComparisonTable(table = notes.comparisonTable)
                    }

                    if (notes.flowchartSteps.isNotEmpty()) {
                        HandwrittenSectionHeading("⇣ Step-by-Step Concept Flowchart", headingColor)
                        NotebookFlowchart(steps = notes.flowchartSteps)
                    }

                    if (notes.examples.isNotEmpty()) {
                        HandwrittenSectionHeading("✓ Solved Examples & Illustrations", headingColor)
                        notes.examples.forEachIndexed { idx, ex ->
                            HandwrittenNumberedLine(
                                indexLabel = "Ex ${idx + 1}:",
                                text = ex,
                                keywords = highlightKeywords,
                                markerColor = if (inkStyle == PenInkStyle.TOPPER_MULTI) EmeraldSuccess else headingColor,
                                inkColor = bodyInkColor
                            )
                        }
                    }
                }
            }
        }

        // PAGE 04: V.V. IMP Exam-Oriented Scoring Box
        if (notes.examOrientedPoints.isNotEmpty()) {
            NotebookPageCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = headingColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "V.V. IMP • EXAM SCORING TIPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                        Text(
                            text = "Pg: 04",
                            fontFamily = HandwritingFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = headingColor
                        )
                    }

                    notes.examOrientedPoints.forEachIndexed { idx, point ->
                        HandwrittenNumberedLine(
                            indexLabel = "${idx + 1}.",
                            text = point,
                            keywords = highlightKeywords,
                            markerColor = headingColor,
                            inkColor = bodyInkColor
                        )
                    }
                }
            }
        }

        // Quick Revision Pocket Card (Handwritten Capsule)
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
                            text = "Last-Minute Handwritten Revision Sheet",
                            fontFamily = HandwritingFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
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
                                    .padding(top = 4.dp)
                                    .size(15.dp)
                            )
                            Text(
                                text = point,
                                fontFamily = HandwritingFontFamily,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotebookPageNumberStrip(
    label: String,
    pageNum: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = HandwritingFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = color.copy(alpha = 0.85f)
        )
        Text(
            text = "Pg: $pageNum",
            fontFamily = HandwritingFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color
        )
    }
    HorizontalDivider(color = color.copy(alpha = 0.35f), thickness = 1.dp)
}

@Composable
private fun HandwrittenSectionHeading(
    title: String,
    color: Color
) {
    Text(
        text = title,
        fontFamily = HandwritingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        textDecoration = TextDecoration.Underline,
        color = color
    )
}

@Composable
private fun HandwrittenNumberedLine(
    indexLabel: String,
    text: String,
    keywords: List<String>,
    markerColor: Color,
    inkColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = indexLabel,
            fontFamily = HandwritingFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = markerColor
        )
        Text(
            text = buildHighlightedAnswer(text, keywords, inkColor),
            fontFamily = HandwritingFontFamily,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
