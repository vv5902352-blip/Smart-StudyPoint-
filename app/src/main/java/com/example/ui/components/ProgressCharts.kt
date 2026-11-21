package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.analytics.CategorySlice
import com.example.data.analytics.NotesTrendBucket
import com.example.data.analytics.QuizTrendPoint
import com.example.data.analytics.StreakDayCell
import com.example.ui.theme.AmberHighlight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MindMapIndigo
import com.example.ui.theme.MindMapRose
import com.example.ui.theme.MindMapTeal
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyAccent
import kotlin.math.abs

/**
 * Interactive Area + Smooth Curve Line Chart for historical quiz performance (0–100%).
 * Tap any point on the chart to inspect exact quiz title, subject, date, and score.
 */
@Composable
fun QuizPerformanceLineChart(
    points: List<QuizTrendPoint>,
    averageAccuracy: Int,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        EmptyChartState(
            message = "Complete a practice MCQ test, Chapter Q&A test, or Daily GK Quiz to plot your score trend.",
            modifier = modifier
        )
        return
    }

    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex) }
    var animateIn by remember(points) { mutableStateOf(false) }
    LaunchedEffect(points) { animateIn = true }
    val animProgress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "quiz_line_anim"
    )

    val lineColor = MaterialTheme.colorScheme.primary
    val accentColor = SkyAccent
    val avgLineColor = EmeraldSuccess
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceBg = MaterialTheme.colorScheme.surface
    val selectedPoint = points.getOrElse(selectedIndex) { points.last() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Selected point interactive tooltip banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedPoint.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${selectedPoint.subject} • ${selectedPoint.fullDateLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedPoint.scorePercent >= 75) EmeraldSuccess else MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${selectedPoint.scorePercent}% (${selectedPoint.correctAnswers}/${selectedPoint.totalQuestions})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Y-axis + Canvas Area Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis percentage labels
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .height(180.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf("100%", "75%", "50%", "25%", "0%").forEach { lbl ->
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(184.dp)
                    .semantics {
                        contentDescription =
                            "Quiz performance trend chart showing ${points.size} attempts with average accuracy $averageAccuracy percent"
                    }
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val padX = 18f
                            val usableW = (size.width - padX * 2f).coerceAtLeast(1f)
                            val count = points.size
                            var nearest = 0
                            var minDist = Float.MAX_VALUE
                            for (i in 0 until count) {
                                val x = if (count == 1) size.width / 2f
                                else padX + (i.toFloat() / (count - 1)) * usableW
                                val dist = abs(offset.x - x)
                                if (dist < minDist) {
                                    minDist = dist
                                    nearest = i
                                }
                            }
                            selectedIndex = nearest
                        }
                    }
            ) {
                val padX = 18.dp.toPx()
                val padTop = 12.dp.toPx()
                val padBottom = 12.dp.toPx()
                val chartW = (size.width - padX * 2f).coerceAtLeast(1f)
                val chartH = (size.height - padTop - padBottom).coerceAtLeast(1f)

                // Horizontal grid lines at 0, 25, 50, 75, 100
                val dashGrid = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                for (step in 0..4) {
                    val y = padTop + (step / 4f) * chartH
                    drawLine(
                        color = gridColor.copy(alpha = 0.65f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (step == 4) null else dashGrid
                    )
                }

                // Dashed Average Reference Line
                val avgY = padTop + chartH * (1f - (averageAccuracy / 100f))
                drawLine(
                    color = avgLineColor.copy(alpha = 0.7f),
                    start = Offset(0f, avgY),
                    end = Offset(size.width, avgY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                // Compute coordinates
                val coords = points.mapIndexed { i, pt ->
                    val x = if (points.size == 1) size.width / 2f
                    else padX + (i.toFloat() / (points.size - 1)) * chartW
                    val animatedScore = pt.scorePercent * animProgress
                    val y = padTop + chartH * (1f - (animatedScore / 100f))
                    Offset(x, y)
                }

                if (coords.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.lastIndex) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val ctrlX = (p0.x + p1.x) / 2f
                            cubicTo(ctrlX, p0.y, ctrlX, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, padTop + chartH)
                        lineTo(coords.first().x, padTop + chartH)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.28f),
                                accentColor.copy(alpha = 0.03f)
                            ),
                            startY = padTop,
                            endY = padTop + chartH
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Vertical guide for selected point
                val selCoord = coords.getOrElse(selectedIndex) { coords.last() }
                drawLine(
                    color = lineColor.copy(alpha = 0.35f),
                    start = Offset(selCoord.x, padTop),
                    end = Offset(selCoord.x, padTop + chartH),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Draw data nodes
                coords.forEachIndexed { idx, pt ->
                    val isSel = idx == selectedIndex
                    if (isSel) {
                        drawCircle(
                            color = lineColor.copy(alpha = 0.22f),
                            radius = 12.dp.toPx(),
                            center = pt
                        )
                    }
                    drawCircle(
                        color = surfaceBg,
                        radius = if (isSel) 6.5.dp.toPx() else 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = if (isSel) lineColor else accentColor,
                        radius = if (isSel) 4.5.dp.toPx() else 3.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // X-axis date labels (evenly sampled so labels never overlap)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelIndices = when {
                points.size <= 5 -> points.indices.toList()
                else -> listOf(0, points.size / 3, (points.size * 2) / 3, points.lastIndex).distinct()
            }
            labelIndices.forEach { idx ->
                val isSel = idx == selectedIndex
                Text(
                    text = points[idx].dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChartLegendDot(color = lineColor, label = "Quiz Score %")
                ChartLegendDot(color = avgLineColor, label = "Avg: $averageAccuracy%")
            }
            Text(
                text = "Tap chart to inspect",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Interactive Bar Chart showing Notes Generated per period (or Cumulative Notes Growth).
 */
@Composable
fun NotesGeneratedBarChart(
    buckets: List<NotesTrendBucket>,
    showCumulative: Boolean,
    modifier: Modifier = Modifier
) {
    if (buckets.isEmpty()) {
        EmptyChartState(
            message = "Generate and save AI Study Notes to see your note-creation momentum.",
            modifier = modifier
        )
        return
    }

    var selectedIndex by remember(buckets, showCumulative) {
        mutableIntStateOf(
            buckets.indexOfLast { it.count > 0 }.takeIf { it >= 0 } ?: buckets.lastIndex
        )
    }
    var animateIn by remember(buckets, showCumulative) { mutableStateOf(false) }
    LaunchedEffect(buckets, showCumulative) { animateIn = true }
    val animProgress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "notes_bar_anim"
    )

    val values = buckets.map { if (showCumulative) it.cumulativeCount else it.count }
    val maxVal = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    val selectedBucket = buckets.getOrElse(selectedIndex) { buckets.last() }

    val primaryBar = RoyalBlue
    val secondaryBar = SkyAccent.copy(alpha = 0.55f)
    val selectedBarColor = MindMapTeal
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tooltip bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedBucket.fullDateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (showCumulative) "Total study library notes" else "Notes generated in period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (showCumulative) "${selectedBucket.cumulativeCount} total" else "+${selectedBucket.count} notes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bar Canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(28.dp)
                    .height(160.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text("$maxVal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${(maxVal / 2f).toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.width(8.dp))

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(166.dp)
                    .semantics {
                        contentDescription = "Notes generated bar chart across ${buckets.size} periods"
                    }
                    .pointerInput(buckets) {
                        detectTapGestures { offset ->
                            val slotW = size.width / buckets.size.coerceAtLeast(1).toFloat()
                            val idx = (offset.x / slotW).toInt().coerceIn(0, buckets.lastIndex)
                            selectedIndex = idx
                        }
                    }
            ) {
                val padTop = 10.dp.toPx()
                val padBottom = 6.dp.toPx()
                val chartH = (size.height - padTop - padBottom).coerceAtLeast(1f)
                val dashGrid = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                listOf(0f, 0.5f, 1f).forEach { frac ->
                    val y = padTop + frac * chartH
                    drawLine(
                        color = gridColor.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = if (frac == 1f) null else dashGrid
                    )
                }

                val slotW = size.width / buckets.size.coerceAtLeast(1)
                val barW = (slotW * 0.56f).coerceIn(10.dp.toPx(), 34.dp.toPx())
                val corner = CornerRadius(8.dp.toPx(), 8.dp.toPx())

                values.forEachIndexed { idx, value ->
                    val normalized = (value.toFloat() / maxVal.toFloat()) * animProgress
                    val barH = (normalized * chartH).coerceAtLeast(if (value > 0) 6.dp.toPx() else 2.dp.toPx())
                    val left = idx * slotW + (slotW - barW) / 2f
                    val top = padTop + chartH - barH
                    val isSel = idx == selectedIndex

                    drawRoundRect(
                        color = when {
                            isSel -> selectedBarColor
                            value > 0 -> primaryBar
                            else -> secondaryBar.copy(alpha = 0.25f)
                        },
                        topLeft = Offset(left, top),
                        size = Size(barW, barH),
                        cornerRadius = corner
                    )
                }
            }
        }

        // X-axis bucket labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = if (buckets.size > 7) 2 else 1
            buckets.forEachIndexed { idx, bucket ->
                if (idx % step == 0 || idx == buckets.lastIndex) {
                    Text(
                        text = bucket.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (idx == selectedIndex) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 28-Day (4x7) Interactive Study Streak Heatmap Calendar.
 */
@Composable
fun StudyStreakHeatmap(
    days: List<StreakDayCell>,
    modifier: Modifier = Modifier
) {
    var selectedCell by remember(days) { mutableStateOf(days.lastOrNull()) }

    val level0 = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
    val level1 = SkyAccent.copy(alpha = 0.35f)
    val level2 = SkyAccent.copy(alpha = 0.7f)
    val level3 = RoyalBlue
    val level4 = EmeraldSuccess

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 4 rows of 7 days
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { cell ->
                    val bgColor = when (cell.intensityLevel) {
                        0 -> level0
                        1 -> level1
                        2 -> level2
                        3 -> level3
                        else -> level4
                    }
                    val isSelected = selectedCell?.dayStartMillis == cell.dayStartMillis
                    val textColor = when {
                        cell.intensityLevel >= 3 -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.15f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(bgColor)
                            .clickable { selectedCell = cell }
                            .semantics {
                                contentDescription =
                                    "${cell.dateLabel}, ${cell.totalActivities} study activities"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected || cell.isToday) {
                            Surface(
                                modifier = Modifier.matchParentSize(),
                                shape = RoundedCornerShape(9.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else AmberHighlight
                                )
                            ) {}
                        }
                        Text(
                            text = cell.dayOfMonth,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = if (cell.isToday || cell.totalActivities > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Selected day details + intensity legend
        selectedCell?.let { cell ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (cell.isToday) "${cell.dateLabel} (Today)" else cell.dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (cell.totalActivities == 0) {
                            "Rest day • 0 activities"
                        } else {
                            "${cell.quizzesCount} quiz • ${cell.notesCount} notes • ${cell.savedQuestionsCount} saved"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            listOf(level0, level1, level2, level3, level4).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Animated Donut Chart displaying Notes breakdown by Study Category.
 */
@Composable
fun NotesCategoryDonutChart(
    slices: List<CategorySlice>,
    totalNotes: Int,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) {
        EmptyChartState(
            message = "No study notes saved yet.",
            modifier = modifier
        )
        return
    }

    var animateIn by remember(slices) { mutableStateOf(false) }
    LaunchedEffect(slices) { animateIn = true }
    val sweepAnim by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "donut_anim"
    )

    val palette = listOf(RoyalBlue, MindMapTeal, MindMapIndigo, AmberHighlight, MindMapRose)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(124.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .semantics {
                        contentDescription = "Notes category distribution chart with $totalNotes total notes"
                    }
            ) {
                val strokeWidth = 18.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                var startAngle = -90f
                val total = slices.sumOf { it.count }.coerceAtLeast(1).toFloat()

                slices.forEachIndexed { idx, slice ->
                    val sweep = (slice.count / total) * 360f * sweepAnim
                    drawArc(
                        color = palette[idx % palette.size],
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2.5f).coerceAtLeast(2f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalNotes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEachIndexed { idx, slice ->
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(palette[idx % palette.size])
                        )
                        Text(
                            text = slice.category.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${slice.count} (${slice.percentage}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyChartState(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )
    }
}
