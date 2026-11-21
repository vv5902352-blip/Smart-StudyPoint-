package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FlashcardEntity
import com.example.ui.theme.AmberHighlight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight
import com.example.viewmodel.FlashcardStudySession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardPracticeSessionView(
    session: FlashcardStudySession,
    onFlipCard: () -> Unit,
    onToggleHint: () -> Unit,
    onRateCard: (Boolean) -> Unit,
    onRestartMissedOnly: () -> Unit,
    onRestartAll: (Boolean) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = session.deckTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Active Recall Practice • ${session.subject}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier.testTag("flashcard_session_back")
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

        if (session.isCompleted) {
            FlashcardSessionSummary(
                session = session,
                onRestartMissedOnly = onRestartMissedOnly,
                onRestartAll = onRestartAll,
                onExit = onExit
            )
        } else {
            val currentCard = session.currentCard ?: return
            val progressFraction = ((session.currentIndex + 1).toFloat() / session.cards.size.coerceAtLeast(1))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress & Recall Counters
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Card ${session.currentIndex + 1} of ${session.cards.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BrandPillTag(
                                text = "Got it: ${session.rememberedIds.size}",
                                containerColor = EmeraldSoft,
                                contentColor = EmeraldSuccess
                            )
                            BrandPillTag(
                                text = "Review: ${session.needsReviewIds.size}",
                                containerColor = AmberSoft,
                                contentColor = AmberHighlight
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }

                // 3D Flip Active Recall Card
                FlippableFlashcard(
                    card = currentCard,
                    isFlipped = session.isFlipped,
                    showHint = session.showHint,
                    onFlip = onFlipCard,
                    onToggleHint = onToggleHint
                )

                // Active Recall Controls
                if (!session.isFlipped) {
                    Button(
                        onClick = onFlipCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("flashcard_reveal_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Flip, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reveal Definition", style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = "Try recalling the definition mentally before flipping the card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "How well did you recall this term?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRateCard(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("flashcard_rate_review"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, AmberHighlight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberHighlight)
                        ) {
                            Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Still Learning", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onRateCard(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("flashcard_rate_got_it"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Got It!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlippableFlashcard(
    card: FlashcardEntity,
    isFlipped: Boolean,
    showHint: Boolean,
    onFlip: () -> Unit,
    onToggleHint: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "flashcard_flip_rotation"
    )
    val density = LocalDensity.current.density
    val flipDesc = stringResource(R.string.cd_flip_flashcard)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 310.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onFlip)
            .testTag("flashcard_flip_surface")
            .semantics { contentDescription = flipDesc }
    ) {
        if (rotation <= 90f) {
            // FRONT: TERM SIDE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 310.dp)
                    .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 262.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandPillTag(
                            text = "TERM",
                            containerColor = PureWhite.copy(alpha = 0.18f),
                            contentColor = PureWhite
                        )
                        if (card.isMastered) {
                            BrandPillTag(
                                text = "Mastered ★",
                                containerColor = EmeraldSuccess.copy(alpha = 0.28f),
                                contentColor = PureWhite
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(vertical = 20.dp)
                    ) {
                        Text(
                            text = card.term,
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (card.hint.isNotBlank()) {
                            AnimatedVisibility(visible = showHint) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PureWhite.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Hint: ${card.hint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SkyLight,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            TextButton(
                                onClick = onToggleHint,
                                colors = ButtonDefaults.textButtonColors(contentColor = SkyLight),
                                modifier = Modifier.testTag("flashcard_hint_toggle")
                            ) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (showHint) "Hide Hint" else "Show Hint")
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flip,
                            contentDescription = null,
                            tint = SkyLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Tap card to flip for definition",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyLight
                        )
                    }
                }
            }
        } else {
            // BACK: DEFINITION SIDE (counter-rotated 180° so text reads normally)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 310.dp)
                    .graphicsLayer { rotationY = 180f },
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 310.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandPillTag(
                            text = "DEFINITION",
                            containerColor = EmeraldSoft,
                            contentColor = EmeraldSuccess
                        )
                        Text(
                            text = card.term,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = card.definition,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )

                    Text(
                        text = "Tap card to view term again",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardSessionSummary(
    session: FlashcardStudySession,
    onRestartMissedOnly: () -> Unit,
    onRestartAll: (Boolean) -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(EmeraldSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Session complete",
                tint = EmeraldSuccess,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = "Active Recall Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "You recalled ${session.rememberedIds.size} of ${session.cards.size} cards (${session.accuracyPercent}%).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatColumn(
                    value = "${session.accuracyPercent}%",
                    label = "Recall Score",
                    valueColor = MaterialTheme.colorScheme.primary
                )
                SummaryStatColumn(
                    value = "${session.rememberedIds.size}",
                    label = "Got It!",
                    valueColor = EmeraldSuccess
                )
                SummaryStatColumn(
                    value = "${session.needsReviewIds.size}",
                    label = "Needs Review",
                    valueColor = AmberHighlight
                )
            }
        }

        if (session.missedCards.isNotEmpty()) {
            Button(
                onClick = onRestartMissedOnly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("flashcard_retry_missed"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Review ${session.missedCards.size} Missed Cards")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onRestartAll(true) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("flashcard_shuffle_again"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Shuffle Deck")
            }

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("flashcard_finish_exit"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Deck")
            }
        }
    }
}

@Composable
private fun SummaryStatColumn(
    value: String,
    label: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
