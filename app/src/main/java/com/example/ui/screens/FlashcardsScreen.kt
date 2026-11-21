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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FlashcardDeckWithCards
import com.example.data.local.FlashcardEntity
import com.example.ui.components.BrandPillTag
import com.example.ui.components.DropdownPickerField
import com.example.ui.components.FlashcardPracticeSessionView
import com.example.ui.components.SmartStudyFooter
import com.example.ui.theme.AmberHighlight
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyLight
import com.example.viewmodel.FlashcardStudySession

@Composable
fun FlashcardsScreen(
    decks: List<FlashcardDeckWithCards>,
    selectedDeckId: Long?,
    studySession: FlashcardStudySession?,
    subjectOptions: List<String>,
    examOptions: List<String>,
    onOpenDeck: (Long) -> Unit,
    onCloseDeck: () -> Unit,
    onCreateDeck: (String, String, String, String) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    onAddCard: (Long, String, String, String) -> Unit,
    onUpdateCard: (FlashcardEntity, String, String, String) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onResetDeckMastery: (Long) -> Unit,
    onStartStudy: (FlashcardDeckWithCards, Boolean, Boolean) -> Unit,
    onFlipCard: () -> Unit,
    onToggleHint: () -> Unit,
    onRateCard: (Boolean) -> Unit,
    onRestartMissedOnly: () -> Unit,
    onRestartAll: (Boolean) -> Unit,
    onExitStudy: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (studySession != null) {
        BackHandler(onBack = onExitStudy)
        FlashcardPracticeSessionView(
            session = studySession,
            onFlipCard = onFlipCard,
            onToggleHint = onToggleHint,
            onRateCard = onRateCard,
            onRestartMissedOnly = onRestartMissedOnly,
            onRestartAll = onRestartAll,
            onExit = onExitStudy,
            modifier = modifier
        )
        return
    }

    val activeDeck = remember(selectedDeckId, decks) {
        selectedDeckId?.let { id -> decks.firstOrNull { it.deck.id == id } }
    }

    if (activeDeck != null) {
        BackHandler(onBack = onCloseDeck)
        FlashcardDeckDetailView(
            deckWithCards = activeDeck,
            onBack = onCloseDeck,
            onAddCard = { term, def, hint -> onAddCard(activeDeck.deck.id, term, def, hint) },
            onUpdateCard = onUpdateCard,
            onDeleteCard = onDeleteCard,
            onResetMastery = { onResetDeckMastery(activeDeck.deck.id) },
            onStartStudy = { onlyUnmastered, shuffle ->
                onStartStudy(activeDeck, onlyUnmastered, shuffle)
            },
            modifier = modifier
        )
        return
    }

    // DECK LIBRARY VIEW
    var showCreateDeckDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteDeck by remember { mutableStateOf<FlashcardDeckWithCards?>(null) }

    val totalCards = decks.sumOf { it.totalCards }
    val totalMastered = decks.sumOf { it.masteredCount }
    val overallMastery = if (totalCards == 0) 0 else (totalMastered * 100) / totalCards

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("flashcards_deck_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(NavyDeep, RoyalBlue)))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BrandPillTag(
                                text = "ACTIVE RECALL • ROOM DB",
                                containerColor = PureWhite.copy(alpha = 0.16f),
                                contentColor = SkyLight
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Flashcard Study Mode",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Build custom term & definition decks and test yourself with 3D flip recall.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyLight
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PureWhite.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Style,
                                contentDescription = "Flashcard decks",
                                tint = PureWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeckHeroMetric("Decks", "${decks.size}", Modifier.weight(1f))
                        DeckHeroMetric("Total Cards", "$totalCards", Modifier.weight(1f))
                        DeckHeroMetric("Mastery", "$overallMastery%", Modifier.weight(1f))
                    }
                }
            }
        }

        // Create New Deck Primary Action
        item {
            Button(
                onClick = { showCreateDeckDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("flashcard_create_deck_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create New Flashcard Deck", style = MaterialTheme.typography.titleSmall)
            }
        }

        if (decks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Style,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No Flashcard Decks Yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Create your first deck with terms and definitions to start active recall practice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(decks, key = { it.deck.id }) { deckWithCards ->
                FlashcardDeckCard(
                    deckWithCards = deckWithCards,
                    onManage = { onOpenDeck(deckWithCards.deck.id) },
                    onStudy = { onStartStudy(deckWithCards, false, false) },
                    onDelete = { pendingDeleteDeck = deckWithCards }
                )
            }
        }

        item { SmartStudyFooter() }
    }

    if (showCreateDeckDialog) {
        CreateDeckDialog(
            subjectOptions = subjectOptions,
            examOptions = examOptions,
            onDismiss = { showCreateDeckDialog = false },
            onConfirm = { title, subject, exam, desc ->
                onCreateDeck(title, subject, exam, desc)
                showCreateDeckDialog = false
            }
        )
    }

    pendingDeleteDeck?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteDeck = null },
            title = { Text("Delete Flashcard Deck?") },
            text = {
                Text("Delete \"${target.deck.title}\" and all ${target.totalCards} flashcards inside it? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDeck(target.deck.id)
                        pendingDeleteDeck = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteDeck = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FlashcardDeckCard(
    deckWithCards: FlashcardDeckWithCards,
    onManage: () -> Unit,
    onStudy: () -> Unit,
    onDelete: () -> Unit
) {
    val deck = deckWithCards.deck
    Card(
        onClick = onManage,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flashcard_deck_card_${deck.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BrandPillTag(deck.subject)
                        BrandPillTag(
                            text = deck.examTag,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = deck.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (deck.description.isNotBlank()) {
                        Text(
                            text = deck.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_deck_${deck.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete deck ${deck.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mastery Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${deckWithCards.totalCards} cards • ${deckWithCards.masteredCount} mastered",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${deckWithCards.masteryPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (deckWithCards.masteryPercent >= 80) EmeraldSuccess else MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { (deckWithCards.masteryPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (deckWithCards.masteryPercent >= 80) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onManage,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("manage_deck_${deck.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cards (${deckWithCards.totalCards})")
                }
                Button(
                    onClick = onStudy,
                    enabled = deckWithCards.totalCards > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("study_deck_${deck.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Study Now")
                }
            }
        }
    }
}

@Composable
private fun FlashcardDeckDetailView(
    deckWithCards: FlashcardDeckWithCards,
    onBack: () -> Unit,
    onAddCard: (String, String, String) -> Unit,
    onUpdateCard: (FlashcardEntity, String, String, String) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onResetMastery: () -> Unit,
    onStartStudy: (Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddCardDialog by rememberSaveable { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<FlashcardEntity?>(null) }
    var onlyUnmastered by rememberSaveable { mutableStateOf(false) }
    var shuffleCards by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("flashcard_deck_detail_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("deck_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deckWithCards.deck.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${deckWithCards.deck.subject} • ${deckWithCards.deck.examTag}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Study Launch Controls Card
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
                        Text(
                            text = "Mastery: ${deckWithCards.masteredCount}/${deckWithCards.totalCards} (${deckWithCards.masteryPercent}%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (deckWithCards.masteredCount > 0) {
                            TextButton(
                                onClick = onResetMastery,
                                modifier = Modifier.testTag("reset_deck_mastery_button")
                            ) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (deckWithCards.masteryPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldSuccess,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = onlyUnmastered,
                            onClick = { onlyUnmastered = !onlyUnmastered },
                            label = { Text("Unmastered (${deckWithCards.unmasteredCount})") },
                            modifier = Modifier.testTag("filter_unmastered_chip")
                        )
                        FilterChip(
                            selected = shuffleCards,
                            onClick = { shuffleCards = !shuffleCards },
                            label = { Text("Shuffle") },
                            leadingIcon = {
                                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("filter_shuffle_chip")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartStudy(onlyUnmastered, shuffleCards) },
                            enabled = deckWithCards.totalCards > 0,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("start_active_recall_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Start Active Recall")
                        }
                        OutlinedButton(
                            onClick = { showAddCardDialog = true },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                                .testTag("add_flashcard_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Card")
                        }
                    }
                }
            }
        }

        if (deckWithCards.cards.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "This deck has no cards yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap 'Add Card' above to add terms and definitions for active recall.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(deckWithCards.cards, key = { it.id }) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashcard_item_${card.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                BrandPillTag(
                                    text = if (card.isMastered) "Mastered" else "Learning",
                                    containerColor = if (card.isMastered) EmeraldSoft else AmberSoft,
                                    contentColor = if (card.isMastered) EmeraldSuccess else AmberHighlight
                                )
                                if (card.reviewCount > 0) {
                                    Text(
                                        text = "${card.correctCount}/${card.reviewCount} recalled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { editingCard = card },
                                    modifier = Modifier.testTag("edit_card_${card.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit flashcard ${card.term}",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteCard(card.id) },
                                    modifier = Modifier.testTag("delete_card_${card.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete flashcard ${card.term}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (card.isMastered) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = card.term,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = card.definition,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (card.hint.isNotBlank()) {
                            Text(
                                text = "Hint: ${card.hint}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCardDialog) {
        FlashcardEditorDialog(
            title = "Add Flashcard",
            initialTerm = "",
            initialDefinition = "",
            initialHint = "",
            confirmLabel = "Save Card",
            onDismiss = { showAddCardDialog = false },
            onConfirm = { term, def, hint ->
                onAddCard(term, def, hint)
                showAddCardDialog = false
            }
        )
    }

    editingCard?.let { card ->
        FlashcardEditorDialog(
            title = "Edit Flashcard",
            initialTerm = card.term,
            initialDefinition = card.definition,
            initialHint = card.hint,
            confirmLabel = "Update",
            onDismiss = { editingCard = null },
            onConfirm = { term, def, hint ->
                onUpdateCard(card, term, def, hint)
                editingCard = null
            }
        )
    }
}

@Composable
private fun DeckHeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PureWhite.copy(alpha = 0.14f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SkyLight)
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = PureWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CreateDeckDialog(
    subjectOptions: List<String>,
    examOptions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf(subjectOptions.firstOrNull() ?: "General Science") }
    var exam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "CBSE Board") }
    var description by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Flashcard Deck") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = null
                    },
                    label = { Text("Deck Title *") },
                    placeholder = { Text("e.g., Physics Formulas — Optics") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { msg -> { Text(msg) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deck_title_input")
                )
                DropdownPickerField(
                    label = "Subject",
                    selectedOption = subject,
                    options = subjectOptions.ifEmpty { listOf("General Science", "Biology", "Physics", "Indian Polity") },
                    onOptionSelected = { subject = it }
                )
                DropdownPickerField(
                    label = "Exam / Board",
                    selectedOption = exam,
                    options = examOptions.ifEmpty { listOf("CBSE Board", "UPSC CSE", "SSC CGL / CHSL") },
                    onOptionSelected = { exam = it }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Short note on what this deck covers") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deck_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Enter a deck title"
                    } else {
                        onConfirm(title, subject, exam, description)
                    }
                },
                modifier = Modifier.testTag("confirm_create_deck_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FlashcardEditorDialog(
    title: String,
    initialTerm: String,
    initialDefinition: String,
    initialHint: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var term by rememberSaveable { mutableStateOf(initialTerm) }
    var definition by rememberSaveable { mutableStateOf(initialDefinition) }
    var hint by rememberSaveable { mutableStateOf(initialHint) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = term,
                    onValueChange = {
                        term = it
                        error = null
                    },
                    label = { Text("Term / Concept *") },
                    placeholder = { Text("e.g., Ohm's Law") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashcard_term_input")
                )
                OutlinedTextField(
                    value = definition,
                    onValueChange = {
                        definition = it
                        error = null
                    },
                    label = { Text("Definition / Answer *") },
                    placeholder = { Text("Write a clear, exam-ready explanation") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashcard_definition_input")
                )
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Memory Hint (Optional)") },
                    placeholder = { Text("Short clue shown before flipping") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashcard_hint_input")
                )
                error?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (term.isBlank() || definition.isBlank()) {
                        error = "Please enter both a term and its definition."
                    } else {
                        onConfirm(term, definition, hint)
                    }
                },
                modifier = Modifier.testTag("confirm_save_card_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
