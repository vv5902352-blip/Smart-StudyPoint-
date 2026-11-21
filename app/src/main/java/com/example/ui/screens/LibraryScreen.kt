package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.StudyNoteEntity
import com.example.data.local.noteCategory
import com.example.data.local.toGeneratedNotes
import com.example.data.model.GeneratedNotes
import com.example.data.model.NoteCategory
import com.example.ui.components.BrandPillTag
import com.example.ui.components.GeneratedNotesView
import com.example.ui.components.SmartStudyFooter

@Composable
fun LibraryScreen(
    allNotes: List<StudyNoteEntity>,
    selectedCategory: NoteCategory?,
    onSelectCategory: (NoteCategory?) -> Unit,
    onOpenNote: (StudyNoteEntity) -> Unit,
    onToggleFavorite: (StudyNoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var filterQuery by rememberSaveable { mutableStateOf("") }

    val filteredNotes = remember(allNotes, selectedCategory, favoritesOnly, filterQuery) {
        allNotes.filter { note ->
            val matchCat = selectedCategory == null || note.noteCategory() == selectedCategory
            val matchFav = !favoritesOnly || note.isFavorite
            val q = filterQuery.trim()
            val matchQuery = q.isEmpty() ||
                note.title.contains(q, ignoreCase = true) ||
                note.subject.contains(q, ignoreCase = true) ||
                note.exam.contains(q, ignoreCase = true) ||
                note.topic.contains(q, ignoreCase = true)
            matchCat && matchFav && matchQuery
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Study Library & Saved Notes", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Access saved notes, favourites, short notes, revision sheets, and PYQs anytime offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Filter saved notes by topic, subject, or exam...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filterQuery.isNotEmpty()) {
                        IconButton(onClick = { filterQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_search))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = !favoritesOnly },
                        label = { Text("Favourites") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (favoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("library_favorites_chip")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All (${allNotes.size})") }
                    )
                }
                items(NoteCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat.label) }
                    )
                }
            }
        }

        if (filteredNotes.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("No matching notes yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Generate notes in AI Studio or clear active filters to view your complete library.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredNotes, key = { it.id }) { note ->
                Card(
                    onClick = { onOpenNote(note) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_note_card_${note.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BrandPillTag(note.noteCategory().label)
                                BrandPillTag(
                                    text = note.exam,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onToggleFavorite(note) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Toggle favourite",
                                        tint = if (note.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteNote(note.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete note",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${note.subject} • ${note.classLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = note.detailedNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: StudyNoteEntity,
    isSpeaking: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: (GeneratedNotes) -> Unit,
    onShare: (GeneratedNotes) -> Unit,
    onExportPdf: (GeneratedNotes) -> Unit,
    onToggleTts: (GeneratedNotes) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val generated = remember(note) { note.toGeneratedNotes() }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = note.topic.ifBlank { note.title },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("note_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GeneratedNotesView(
                    notes = generated,
                    isSaved = true,
                    isFavorite = note.isFavorite,
                    isSpeaking = isSpeaking,
                    onSaveClick = null,
                    onFavoriteClick = onToggleFavorite,
                    onCopyClick = { onCopy(generated) },
                    onShareClick = { onShare(generated) },
                    onPdfClick = { onExportPdf(generated) },
                    onTtsClick = { onToggleTts(generated) }
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                SmartStudyFooter()
            }
        }
    }
}
