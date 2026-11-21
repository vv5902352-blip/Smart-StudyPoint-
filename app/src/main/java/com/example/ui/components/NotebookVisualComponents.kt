package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DefinitionItem
import com.example.data.model.DiagramKind
import com.example.data.model.MindMapData
import com.example.data.model.TableData
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HandwritingFontFamily
import com.example.ui.theme.MarkerYellowBg
import com.example.ui.theme.MarkerYellowText
import com.example.ui.theme.MindMapAmber
import com.example.ui.theme.MindMapIndigo
import com.example.ui.theme.MindMapRose
import com.example.ui.theme.MindMapTeal
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NotebookBlackInkDark
import com.example.ui.theme.NotebookBlackInkLight
import com.example.ui.theme.NotebookBlueInkDark
import com.example.ui.theme.NotebookBlueInkLight
import com.example.ui.theme.NotebookHeadingRedDark
import com.example.ui.theme.NotebookHeadingRedLight
import com.example.ui.theme.NotebookMarginRed
import com.example.ui.theme.NotebookPaperDark
import com.example.ui.theme.NotebookPaperLight
import com.example.ui.theme.NotebookRuleDark
import com.example.ui.theme.NotebookRuleLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyAccent

@Composable
fun isNotebookDark(): Boolean {
    return MaterialTheme.colorScheme.background.red < 0.2f || isSystemInDarkTheme() && MaterialTheme.colorScheme.background.red < 0.5f
}

@Composable
fun notebookRedHeadingColor(): Color =
    if (isNotebookDark()) NotebookHeadingRedDark else NotebookHeadingRedLight

@Composable
fun notebookBlueInkColor(): Color =
    if (isNotebookDark()) NotebookBlueInkDark else NotebookBlueInkLight

@Composable
fun notebookBlackInkColor(): Color =
    if (isNotebookDark()) NotebookBlackInkDark else NotebookBlackInkLight

/**
 * Colorful ruled notebook-style page card with pale-blue ruled lines and vertical red margin line.
 */
@Composable
fun NotebookPageCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dark = isNotebookDark()
    val paperBg = if (dark) NotebookPaperDark else NotebookPaperLight
    val ruleColor = if (dark) NotebookRuleDark else NotebookRuleLight
    val marginColor = if (dark) NotebookHeadingRedDark.copy(alpha = 0.45f) else NotebookMarginRed

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = paperBg,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val lineSpacingPx = 28.dp.toPx()
                    var y = lineSpacingPx
                    while (y < size.height) {
                        drawLine(
                            color = ruleColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineSpacingPx
                    }
                    val marginX = 24.dp.toPx()
                    drawLine(
                        color = marginColor,
                        start = Offset(marginX, 0f),
                        end = Offset(marginX, size.height),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = marginColor.copy(alpha = 0.5f),
                        start = Offset(marginX + 3.dp.toPx(), 0f),
                        end = Offset(marginX + 3.dp.toPx(), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(start = 34.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            content()
        }
    }
}

/**
 * Renders handwritten-style text with real yellow highlighter spans on important exam keywords.
 */
fun buildHighlightedAnswer(
    text: String,
    keywords: List<String>,
    baseColor: Color
): AnnotatedString = buildAnnotatedString {
    append(text)
    addStyle(SpanStyle(color = baseColor), 0, text.length)
    val lower = text.lowercase()
    keywords.filter { it.isNotBlank() }.forEach { kw ->
        val kwLower = kw.lowercase()
        var startIdx = lower.indexOf(kwLower, 0)
        while (startIdx >= 0) {
            val endIdx = (startIdx + kw.length).coerceAtMost(text.length)
            addStyle(
                style = SpanStyle(
                    background = MarkerYellowBg,
                    color = MarkerYellowText,
                    fontWeight = FontWeight.Bold
                ),
                start = startIdx,
                end = endIdx
            )
            startIdx = lower.indexOf(kwLower, endIdx)
        }
    }
}

@Composable
fun HandwrittenRedHeading(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontFamily = HandwritingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        color = notebookRedHeadingColor(),
        modifier = modifier
    )
}

@Composable
fun HandwrittenBlueAnswer(
    text: String,
    keywords: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val ink = notebookBlueInkColor()
    Text(
        text = buildHighlightedAnswer(text, keywords, ink),
        fontFamily = HandwritingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        modifier = modifier
    )
}

@Composable
fun HandwrittenBlackQuestion(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontFamily = HandwritingFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = notebookBlackInkColor(),
        modifier = modifier
    )
}

@Composable
fun NotebookDefinitionBox(
    definition: DefinitionItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MarkerYellowBg.copy(alpha = if (isNotebookDark()) 0.16f else 0.45f),
        border = BorderStroke(1.5.dp, MindMapAmber)
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
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = notebookRedHeadingColor(),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Important Definition — ${definition.term}",
                    fontFamily = HandwritingFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = notebookRedHeadingColor()
                )
            }
            Text(
                text = definition.definition,
                fontFamily = HandwritingFontFamily,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = notebookBlackInkColor()
            )
        }
    }
}

@Composable
fun NotebookComparisonTable(
    table: TableData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, RoyalBlue)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDeep)
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                table.headers.forEach { header ->
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
            table.rows.forEachIndexed { idx, row ->
                val bg = if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    row.forEachIndexed { cIdx, cell ->
                        Text(
                            text = cell,
                            fontFamily = HandwritingFontFamily,
                            fontSize = 14.sp,
                            fontWeight = if (cIdx == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (cIdx == 0) notebookRedHeadingColor() else notebookBlueInkColor(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                if (idx < table.rows.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
fun NotebookFlowchart(
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        steps.forEachIndexed { idx, step ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = step,
                        fontFamily = HandwritingFontFamily,
                        fontSize = 15.sp,
                        color = notebookBlackInkColor(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (idx < steps.lastIndex) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = notebookRedHeadingColor(),
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NotebookMindMap(
    mindMap: MindMapData,
    modifier: Modifier = Modifier
) {
    val branchColors = listOf(RoyalBlue, MindMapTeal, MindMapAmber, MindMapRose, MindMapIndigo)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Hub, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "Visual Mind Map",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Central Node
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = NavyDeep,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = mindMap.centralTopic,
                    fontFamily = HandwritingFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PureWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Connector Lines Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                val midX = size.width / 2f
                val leftX = size.width * 0.25f
                val rightX = size.width * 0.75f
                drawLine(RoyalBlue, Offset(midX, 0f), Offset(midX, size.height * 0.45f), strokeWidth = 2.dp.toPx())
                drawLine(RoyalBlue, Offset(leftX, size.height * 0.45f), Offset(rightX, size.height * 0.45f), strokeWidth = 2.dp.toPx())
                drawLine(MindMapTeal, Offset(leftX, size.height * 0.45f), Offset(leftX, size.height), strokeWidth = 2.dp.toPx())
                drawLine(MindMapAmber, Offset(rightX, size.height * 0.45f), Offset(rightX, size.height), strokeWidth = 2.dp.toPx())
            }

            // Branch Grid (2 columns)
            mindMap.branches.chunked(2).forEachIndexed { rowIdx, pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEachIndexed { colIdx, branch ->
                        val color = branchColors[(rowIdx * 2 + colIdx) % branchColors.size]
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = color.copy(alpha = 0.1f),
                            border = BorderStroke(1.2.dp, color)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = branch.title,
                                    fontFamily = HandwritingFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = color
                                )
                                branch.points.forEach { pt ->
                                    Text(
                                        text = "• $pt",
                                        fontFamily = HandwritingFontFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = notebookBlackInkColor()
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Renders actual labeled scientific vector Canvas diagrams for Diagram-Based Questions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabeledScientificDiagram(
    kind: DiagramKind,
    caption: String,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.2.dp, RoyalBlue.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FBFF))
            ) {
                when (kind) {
                    DiagramKind.STOMATA -> drawStomataDiagram()
                    DiagramKind.PHOTOSYNTHESIS_LEAF -> drawLeafPhotosynthesisDiagram()
                    DiagramKind.HUMAN_HEART_FLOW -> drawDoubleCirculationDiagram()
                    DiagramKind.ELECTRIC_CIRCUIT -> drawOhmsLawCircuitDiagram()
                    DiagramKind.CONCEPT_PROCESS -> drawConceptCycleDiagram()
                }
            }

            // Numbered Labeled Callout Legend
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                labels.forEachIndexed { idx, label ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${idx + 1}. $label",
                            style = MaterialTheme.typography.labelSmall,
                            color = NavyDeep,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "Fig: $caption",
                style = MaterialTheme.typography.labelMedium,
                color = NavyDeep,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStomataDiagram() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.height * 0.34f

    // Outer epidermal cell boundary
    drawOval(
        color = Color(0xFFDCFCE7),
        topLeft = Offset(cx - r * 1.9f, cy - r * 1.15f),
        size = Size(r * 3.8f, r * 2.3f)
    )
    drawOval(
        color = Color(0xFF15803D),
        topLeft = Offset(cx - r * 1.9f, cy - r * 1.15f),
        size = Size(r * 3.8f, r * 2.3f),
        style = Stroke(width = 2.dp.toPx())
    )

    // Left bean-shaped guard cell
    drawOval(
        color = Color(0xFF86EFAC),
        topLeft = Offset(cx - r * 1.15f, cy - r * 0.85f),
        size = Size(r * 0.95f, r * 1.7f)
    )
    drawOval(
        color = Color(0xFF166534),
        topLeft = Offset(cx - r * 1.15f, cy - r * 0.85f),
        size = Size(r * 0.95f, r * 1.7f),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Right bean-shaped guard cell
    drawOval(
        color = Color(0xFF86EFAC),
        topLeft = Offset(cx + r * 0.2f, cy - r * 0.85f),
        size = Size(r * 0.95f, r * 1.7f)
    )
    drawOval(
        color = Color(0xFF166534),
        topLeft = Offset(cx + r * 0.2f, cy - r * 0.85f),
        size = Size(r * 0.95f, r * 1.7f),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Central Open Stomatal Pore
    drawOval(
        color = PureWhite,
        topLeft = Offset(cx - r * 0.28f, cy - r * 0.55f),
        size = Size(r * 0.56f, r * 1.1f)
    )
    drawOval(
        color = NavyDeep,
        topLeft = Offset(cx - r * 0.28f, cy - r * 0.55f),
        size = Size(r * 0.56f, r * 1.1f),
        style = Stroke(width = 2.dp.toPx())
    )

    // Chloroplast dots inside guard cells
    val dots = listOf(
        Offset(cx - r * 0.75f, cy - r * 0.4f),
        Offset(cx - r * 0.82f, cy),
        Offset(cx - r * 0.72f, cy + r * 0.42f),
        Offset(cx + r * 0.75f, cy - r * 0.4f),
        Offset(cx + r * 0.82f, cy),
        Offset(cx + r * 0.72f, cy + r * 0.42f)
    )
    dots.forEach { d ->
        drawCircle(color = Color(0xFF047857), radius = 4.dp.toPx(), center = d)
    }

    // Callout lines (1, 2, 3, 4)
    drawLine(Color(0xFFB91C1C), Offset(cx - r * 0.9f, cy - r * 0.65f), Offset(cx - r * 1.85f, cy - r * 0.95f), 1.8.dp.toPx())
    drawLine(RoyalBlue, Offset(cx, cy), Offset(cx + r * 1.8f, cy - r * 0.9f), 1.8.dp.toPx())
    drawLine(Color(0xFF047857), Offset(cx + r * 0.82f, cy), Offset(cx + r * 1.85f, cy + r * 0.1f), 1.8.dp.toPx())
    drawLine(NavyDeep, Offset(cx - r * 1.4f, cy + r * 0.95f), Offset(cx - r * 1.95f, cy + r * 1.05f), 1.8.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeafPhotosynthesisDiagram() {
    val w = size.width
    val h = size.height

    // Sun rays top-left
    drawCircle(color = Color(0xFFFACC15), radius = 18.dp.toPx(), center = Offset(w * 0.12f, h * 0.2f))
    drawLine(Color(0xFFD97706), Offset(w * 0.19f, h * 0.26f), Offset(w * 0.34f, h * 0.38f), 2.5.dp.toPx())

    // Upper epidermis cuticle
    drawRoundRect(
        color = Color(0xFFBBF7D0),
        topLeft = Offset(w * 0.22f, h * 0.34f),
        size = Size(w * 0.56f, h * 0.1f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Palisade mesophyll layer with chloroplasts
    drawRect(
        color = Color(0xFF4ADE80),
        topLeft = Offset(w * 0.22f, h * 0.44f),
        size = Size(w * 0.56f, h * 0.22f)
    )
    for (i in 0..6) {
        val x = w * (0.26f + i * 0.075f)
        drawCircle(Color(0xFF14532D), 3.5.dp.toPx(), Offset(x, h * 0.51f))
        drawCircle(Color(0xFF14532D), 3.5.dp.toPx(), Offset(x, h * 0.59f))
    }

    // Vascular bundle (Xylem / Phloem)
    drawCircle(color = RoyalBlue, radius = 14.dp.toPx(), center = Offset(w * 0.5f, h * 0.55f))
    drawCircle(color = SkyAccent, radius = 7.dp.toPx(), center = Offset(w * 0.5f, h * 0.55f))

    // Lower epidermis + stomatal opening
    drawRect(color = Color(0xFFBBF7D0), topLeft = Offset(w * 0.22f, h * 0.66f), size = Size(w * 0.23f, h * 0.09f))
    drawRect(color = Color(0xFFBBF7D0), topLeft = Offset(w * 0.55f, h * 0.66f), size = Size(w * 0.23f, h * 0.09f))

    // CO2 In & O2 Out arrows
    drawLine(RoyalBlue, Offset(w * 0.46f, h * 0.88f), Offset(w * 0.46f, h * 0.68f), 2.5.dp.toPx())
    drawLine(EmeraldSuccess, Offset(w * 0.54f, h * 0.68f), Offset(w * 0.54f, h * 0.88f), 2.5.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDoubleCirculationDiagram() {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    // Lungs box (top)
    drawRoundRect(
        color = Color(0xFFDBEAFE),
        topLeft = Offset(cx - w * 0.22f, h * 0.08f),
        size = Size(w * 0.44f, h * 0.18f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = RoyalBlue,
        topLeft = Offset(cx - w * 0.22f, h * 0.08f),
        size = Size(w * 0.44f, h * 0.18f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(2.dp.toPx())
    )

    // 4-chambered Heart (center): Blue right side + Red left side
    val heartW = w * 0.34f
    val heartH = h * 0.32f
    drawRect(Color(0xFF93C5FD), Offset(cx - heartW / 2f, h * 0.34f), Size(heartW / 2f, heartH))
    drawRect(Color(0xFFFCA5A5), Offset(cx, h * 0.34f), Size(heartW / 2f, heartH))
    drawRect(NavyDeep, Offset(cx - heartW / 2f, h * 0.34f), Size(heartW, heartH), style = Stroke(2.dp.toPx()))
    drawLine(NavyDeep, Offset(cx, h * 0.34f), Offset(cx, h * 0.66f), 2.dp.toPx())
    drawLine(NavyDeep, Offset(cx - heartW / 2f, h * 0.5f), Offset(cx + heartW / 2f, h * 0.5f), 1.5.dp.toPx())

    // Body Tissues box (bottom)
    drawRoundRect(
        color = Color(0xFFFEF3C7),
        topLeft = Offset(cx - w * 0.24f, h * 0.76f),
        size = Size(w * 0.48f, h * 0.17f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = MindMapAmber,
        topLeft = Offset(cx - w * 0.24f, h * 0.76f),
        size = Size(w * 0.48f, h * 0.17f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(2.dp.toPx())
    )

    // Blue Deoxygenated loop (left) & Red Oxygenated loop (right)
    drawLine(RoyalBlue, Offset(cx - heartW / 2f, h * 0.58f), Offset(w * 0.16f, h * 0.58f), 3.dp.toPx())
    drawLine(RoyalBlue, Offset(w * 0.16f, h * 0.17f), Offset(w * 0.16f, h * 0.84f), 3.dp.toPx())
    drawLine(RoyalBlue, Offset(w * 0.16f, h * 0.17f), Offset(cx - w * 0.22f, h * 0.17f), 3.dp.toPx())
    drawLine(RoyalBlue, Offset(w * 0.16f, h * 0.84f), Offset(cx - w * 0.24f, h * 0.84f), 3.dp.toPx())

    drawLine(Color(0xFFDC2626), Offset(cx + heartW / 2f, h * 0.42f), Offset(w * 0.84f, h * 0.42f), 3.dp.toPx())
    drawLine(Color(0xFFDC2626), Offset(w * 0.84f, h * 0.17f), Offset(w * 0.84f, h * 0.84f), 3.dp.toPx())
    drawLine(Color(0xFFDC2626), Offset(cx + w * 0.22f, h * 0.17f), Offset(w * 0.84f, h * 0.17f), 3.dp.toPx())
    drawLine(Color(0xFFDC2626), Offset(cx + w * 0.24f, h * 0.84f), Offset(w * 0.84f, h * 0.84f), 3.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOhmsLawCircuitDiagram() {
    val w = size.width
    val h = size.height
    val left = w * 0.14f
    val right = w * 0.86f
    val top = h * 0.38f
    val bottom = h * 0.80f

    // Outer loop wires
    drawRect(
        color = NavyDeep,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Zig-zag Resistor R along top center
    val resPath = Path().apply {
        val sx = w * 0.36f
        val step = (w * 0.28f) / 6f
        moveTo(sx, top)
        for (i in 1..5) {
            val yOff = if (i % 2 == 1) -10.dp.toPx() else 10.dp.toPx()
            lineTo(sx + step * i, top + yOff)
        }
        lineTo(sx + step * 6, top)
    }
    drawRect(Color(0xFFF8FBFF), Offset(w * 0.36f, top - 12.dp.toPx()), Size(w * 0.28f, 24.dp.toPx()))
    drawPath(resPath, color = Color(0xFFB91C1C), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

    // Parallel Voltmeter (V) above Resistor
    val vTop = h * 0.14f
    drawLine(RoyalBlue, Offset(w * 0.34f, top), Offset(w * 0.34f, vTop), 2.dp.toPx())
    drawLine(RoyalBlue, Offset(w * 0.66f, top), Offset(w * 0.66f, vTop), 2.dp.toPx())
    drawLine(RoyalBlue, Offset(w * 0.34f, vTop), Offset(w * 0.66f, vTop), 2.dp.toPx())
    drawCircle(Color(0xFFDBEAFE), radius = 14.dp.toPx(), center = Offset(w * 0.5f, vTop))
    drawCircle(RoyalBlue, radius = 14.dp.toPx(), center = Offset(w * 0.5f, vTop), style = Stroke(2.dp.toPx()))

    // Series Ammeter (A) on right wire
    drawCircle(Color(0xFFD1FAE5), radius = 14.dp.toPx(), center = Offset(right, (top + bottom) / 2f))
    drawCircle(EmeraldSuccess, radius = 14.dp.toPx(), center = Offset(right, (top + bottom) / 2f), style = Stroke(2.dp.toPx()))

    // Battery plates on bottom wire
    val bx = w * 0.46f
    drawRect(Color(0xFFF8FBFF), Offset(bx - 10.dp.toPx(), bottom - 12.dp.toPx()), Size(26.dp.toPx(), 24.dp.toPx()))
    drawLine(NavyDeep, Offset(bx, bottom - 14.dp.toPx()), Offset(bx, bottom + 14.dp.toPx()), 3.dp.toPx())
    drawLine(Color(0xFFB91C1C), Offset(bx + 10.dp.toPx(), bottom - 8.dp.toPx()), Offset(bx + 10.dp.toPx(), bottom + 8.dp.toPx()), 4.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConceptCycleDiagram() {
    val w = size.width
    val h = size.height
    val colors = listOf(RoyalBlue, MindMapTeal, MindMapAmber, MindMapRose)
    val centers = listOf(
        Offset(w * 0.25f, h * 0.30f),
        Offset(w * 0.75f, h * 0.30f),
        Offset(w * 0.75f, h * 0.72f),
        Offset(w * 0.25f, h * 0.72f)
    )
    // Connecting lines
    drawLine(NavyDeep, centers[0], centers[1], 2.5.dp.toPx())
    drawLine(NavyDeep, centers[1], centers[2], 2.5.dp.toPx())
    drawLine(NavyDeep, centers[2], centers[3], 2.5.dp.toPx())
    drawLine(NavyDeep, centers[3], centers[0], 2.5.dp.toPx())

    centers.forEachIndexed { idx, pt ->
        drawCircle(color = colors[idx].copy(alpha = 0.2f), radius = 26.dp.toPx(), center = pt)
        drawCircle(color = colors[idx], radius = 26.dp.toPx(), center = pt, style = Stroke(2.5.dp.toPx()))
        drawCircle(color = colors[idx], radius = 7.dp.toPx(), center = pt)
    }
}
