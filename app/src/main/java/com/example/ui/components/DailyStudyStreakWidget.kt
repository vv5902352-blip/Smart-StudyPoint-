package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.analytics.DailyStreakWidgetInfo
import com.example.data.analytics.StreakDayCell
import com.example.ui.theme.AmberHighlight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Motivating Home Screen widget tracking consecutive days of app usage & study activity,
 * featuring an animated circular progress indicator with a centered flame icon and a 7-day strip.
 */
@Composable
fun DailyStudyStreakWidget(
    info: DailyStreakWidgetInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animateIn by remember(info.currentStreakDays, info.nextGoalDays) { mutableStateOf(false) }
    LaunchedEffect(info.currentStreakDays, info.nextGoalDays) { animateIn = true }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animateIn) info.progressFraction else 0f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "daily_streak_ring_progress"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val ringAccent = AmberHighlight
    val ringStart = RoyalBlue
    val ringMid = SkyAccent

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_daily_study_streak_widget")
            .semantics {
                contentDescription =
                    "Daily Study Streak: ${info.currentStreakDays} consecutive days, progress ${info.currentStreakDays} of ${info.nextGoalDays} days goal. Tap to view full Student Progress."
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Progress Indicator with Flame Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .testTag("daily_streak_circular_indicator"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val strokePx = 9.dp.toPx()
                        val inset = strokePx / 2f + 2.dp.toPx()
                        val diameter = (size.minDimension - inset * 2f).coerceAtLeast(1f)
                        val arcTopLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val arcSize = Size(diameter, diameter)

                        // Soft subtle inner warm halo when streak is active
                        if (info.currentStreakDays > 0) {
                            drawCircle(
                                color = ringAccent.copy(alpha = 0.09f),
                                radius = diameter / 2f - strokePx / 2f,
                                center = Offset(size.width / 2f, size.height / 2f)
                            )
                        }

                        // Background ring track
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        // Active gradient progress arc
                        val sweep = (animatedProgress * 360f).coerceAtLeast(
                            if (info.currentStreakDays > 0) 14f else 0f
                        )
                        if (sweep > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(ringAccent, ringStart, ringMid, ringAccent)
                                ),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )

                            // Glowing endpoint cap dot
                            val endRad = (-90f + sweep) * (PI / 180.0)
                            val radius = diameter / 2f
                            val cx = size.width / 2f + (radius * cos(endRad)).toFloat()
                            val cy = size.height / 2f + (radius * sin(endRad)).toFloat()
                            drawCircle(
                                color = PureWhite,
                                radius = 3.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Daily study streak flame",
                            tint = if (info.currentStreakDays > 0) AmberHighlight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "${info.currentStreakDays}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (info.currentStreakDays == 1) "DAY" else "DAYS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right Motivational Content & Goal Progress
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Study Streak",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        BrandPillTag(
                            text = if (info.studiedToday) "Active Today" else "Check In",
                            containerColor = if (info.studiedToday) EmeraldSoft else AmberSoft,
                            contentColor = if (info.studiedToday) EmeraldSuccess else AmberHighlight
                        )
                    }

                    Text(
                        text = "${info.currentStreakDays}/${info.nextGoalDays} day milestone • Best: ${info.longestStreakDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = info.motivationalMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 7-Day Consecutive Usage Strip + Progress Link
            if (info.recentSevenDays.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            info.recentSevenDays.forEach { dayCell ->
                                StreakDayPill(
                                    cell = dayCell,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open full progress trends",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakDayPill(
    cell: StreakDayCell,
    modifier: Modifier = Modifier
) {
    val active = cell.totalActivities > 0
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = cell.dayOfWeekShort.ifBlank { cell.dayOfMonth },
            style = MaterialTheme.typography.labelSmall,
            color = if (cell.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when {
                        active && cell.isToday -> AmberHighlight
                        active -> RoyalBlue
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Icon(
                    imageVector = if (cell.isToday) Icons.Filled.LocalFireDepartment else Icons.Filled.Check,
                    contentDescription = "${cell.dateLabel} streak active",
                    tint = PureWhite,
                    modifier = Modifier.size(15.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(26.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = if (cell.isToday) 1.5.dp else 1.dp,
                        color = if (cell.isToday) AmberHighlight else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {}
            }
        }
    }
}
