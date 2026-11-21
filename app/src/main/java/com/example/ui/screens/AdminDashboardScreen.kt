package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CatalogItemEntity
import com.example.data.local.ManagedUserEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.catalogType
import com.example.data.local.noteCategory
import com.example.data.model.CatalogType
import com.example.data.model.NoteCategory
import com.example.data.model.StudyLanguage
import com.example.ui.components.BrandPillTag
import com.example.ui.components.DropdownPickerField
import com.example.ui.components.SmartStudyFooter
import com.example.viewmodel.AdminAnalytics

private data class AdminTabMeta(val label: String, val icon: ImageVector)

private val adminTabs = listOf(
    AdminTabMeta("Analytics", Icons.Filled.Analytics),
    AdminTabMeta("Content", Icons.Filled.EditNote),
    AdminTabMeta("Subjects & Classes", Icons.Filled.Category),
    AdminTabMeta("Users", Icons.Filled.Group)
)

@Composable
fun AdminDashboardScreen(
    analytics: AdminAnalytics,
    allNotes: List<StudyNoteEntity>,
    catalogItems: List<CatalogItemEntity>,
    managedUsers: List<ManagedUserEntity>,
    classOptions: List<String>,
    examOptions: List<String>,
    subjectOptions: List<String>,
    onPublishNote: (String, String, String, String, NoteCategory, StudyLanguage, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddCatalogItem: (CatalogType, String) -> Unit,
    onDeleteCatalogItem: (Long) -> Unit,
    onAddUser: (String, String, String, String) -> Unit,
    onToggleUserActive: (ManagedUserEntity) -> Unit,
    onCycleUserRole: (ManagedUserEntity) -> Unit,
    onDeleteUser: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Admin Dashboard", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Manage educational content, classes, subjects, users, and platform analytics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp
        ) {
            adminTabs.forEachIndexed { idx, meta ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(meta.label) },
                    icon = { Icon(meta.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("admin_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> AdminAnalyticsTab(analytics = analytics, modifier = Modifier.weight(1f))
            1 -> AdminContentTab(
                allNotes = allNotes,
                classOptions = classOptions,
                examOptions = examOptions,
                subjectOptions = subjectOptions,
                onPublishNote = onPublishNote,
                onDeleteNote = onDeleteNote,
                modifier = Modifier.weight(1f)
            )

            2 -> AdminCatalogTab(
                catalogItems = catalogItems,
                onAddItem = onAddCatalogItem,
                onDeleteItem = onDeleteCatalogItem,
                modifier = Modifier.weight(1f)
            )

            3 -> AdminUsersTab(
                users = managedUsers,
                examOptions = examOptions,
                onAddUser = onAddUser,
                onToggleActive = onToggleUserActive,
                onCycleRole = onCycleUserRole,
                onDeleteUser = onDeleteUser,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminAnalyticsTab(
    analytics: AdminAnalytics,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primaryContainer

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard("Total Notes", "${analytics.totalNotes}", "${analytics.publishedNotes} Official", Modifier.weight(1f))
                KpiCard("Active Users", "${analytics.activeUsers}/${analytics.totalUsers}", "Students & Staff", Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard("Quizzes Taken", "${analytics.totalQuizzesTaken}", "MCQ & Daily GK", Modifier.weight(1f))
                KpiCard("Avg Accuracy", "${analytics.averageQuizAccuracy}%", "${analytics.favoriteNotes} Favourites", Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Content Distribution by Category", style = MaterialTheme.typography.titleMedium)
                    val entries = analytics.notesByCategory.entries.toList()
                    val maxCount = (entries.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

                    entries.forEach { (category, count) ->
                        val ratio = count.toFloat() / maxCount.toFloat()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.label, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                            ) {
                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset.Zero,
                                    size = size,
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset.Zero,
                                    size = Size(size.width * ratio.coerceIn(0.03f, 1f), size.height),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}

@Composable
private fun KpiCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminContentTab(
    allNotes: List<StudyNoteEntity>,
    classOptions: List<String>,
    examOptions: List<String>,
    subjectOptions: List<String>,
    onPublishNote: (String, String, String, String, NoteCategory, StudyLanguage, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedClass by rememberSaveable { mutableStateOf(classOptions.firstOrNull() ?: "Class 10th") }
    var selectedExam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "UPSC CSE") }
    var selectedSubject by rememberSaveable { mutableStateOf(subjectOptions.firstOrNull() ?: "Indian Polity") }
    var selectedCategory by rememberSaveable { mutableStateOf(NoteCategory.IMPORTANT_QUESTIONS) }
    var selectedLanguage by rememberSaveable { mutableStateOf(StudyLanguage.ENGLISH) }
    var bodyContent by rememberSaveable { mutableStateOf("") }
    var keyPoints by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
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
                    Text("Publish Important Study Note", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g., Union Budget 2026 Key Highlights") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_publish_title")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownPickerField(
                            label = "Class",
                            options = classOptions,
                            selectedOption = if (selectedClass in classOptions) selectedClass else classOptions.firstOrNull().orEmpty(),
                            onOptionSelected = { selectedClass = it },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownPickerField(
                            label = "Exam",
                            options = examOptions,
                            selectedOption = if (selectedExam in examOptions) selectedExam else examOptions.firstOrNull().orEmpty(),
                            onOptionSelected = { selectedExam = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    DropdownPickerField(
                        label = "Subject",
                        options = subjectOptions,
                        selectedOption = if (selectedSubject in subjectOptions) selectedSubject else subjectOptions.firstOrNull().orEmpty(),
                        onOptionSelected = { selectedSubject = it }
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            NoteCategory.IMPORTANT_QUESTIONS,
                            NoteCategory.SHORT_NOTES,
                            NoteCategory.REVISION_NOTES,
                            NoteCategory.PYQ,
                            NoteCategory.ADMIN_PUBLISHED
                        ).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = bodyContent,
                        onValueChange = { bodyContent = it },
                        label = { Text("Detailed Educational Content") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_publish_body")
                    )
                    OutlinedTextField(
                        value = keyPoints,
                        onValueChange = { keyPoints = it },
                        label = { Text("Quick Revision Points (one per line)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onPublishNote(
                                title,
                                selectedClass,
                                selectedExam,
                                selectedSubject,
                                selectedCategory,
                                selectedLanguage,
                                bodyContent,
                                keyPoints
                            )
                            if (title.isNotBlank() && bodyContent.isNotBlank()) {
                                title = ""
                                bodyContent = ""
                                keyPoints = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_publish_button")
                    ) {
                        Icon(Icons.Filled.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Publish to App Library")
                    }
                }
            }
        }

        item {
            Text("Manage Library Content (${allNotes.size})", style = MaterialTheme.typography.titleMedium)
        }

        items(allNotes, key = { it.id }) { note ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(note.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${note.noteCategory().label} • ${note.exam} • ${note.subject}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteNote(note.id) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete note")
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminCatalogTab(
    catalogItems: List<CatalogItemEntity>,
    onAddItem: (CatalogType, String) -> Unit,
    onDeleteItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by rememberSaveable { mutableStateOf(CatalogType.SUBJECT) }
    var newName by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
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
                    Text("Add Class, Exam, or Subject", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CatalogType.entries.forEach { t ->
                            FilterChip(
                                selected = selectedType == t,
                                onClick = { selectedType = t },
                                label = { Text(t.label) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("New ${selectedType.label} Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_catalog_input")
                        )
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onAddItem(selectedType, newName)
                                    newName = ""
                                }
                            },
                            modifier = Modifier.testTag("admin_catalog_add_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        }

        CatalogType.entries.forEach { type ->
            val group = catalogItems.filter { it.catalogType() == type }
            item {
                Text("${type.label}s (${group.size})", style = MaterialTheme.typography.titleMedium)
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.name, style = MaterialTheme.typography.bodySmall)
                                IconButton(
                                    onClick = { onDeleteItem(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Remove ${item.name}",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<ManagedUserEntity>,
    examOptions: List<String>,
    onAddUser: (String, String, String, String) -> Unit,
    onToggleActive: (ManagedUserEntity) -> Unit,
    onCycleRole: (ManagedUserEntity) -> Unit,
    onDeleteUser: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("Student") }
    var targetExam by rememberSaveable { mutableStateOf(examOptions.firstOrNull() ?: "UPSC CSE") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                    Text("Add Student or Educator", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_user_name_input")
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_user_email_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownPickerField(
                            label = "Role",
                            options = listOf("Student", "Educator", "Admin"),
                            selectedOption = role,
                            onOptionSelected = { role = it },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownPickerField(
                            label = "Target Exam",
                            options = examOptions,
                            selectedOption = if (targetExam in examOptions) targetExam else examOptions.firstOrNull().orEmpty(),
                            onOptionSelected = { targetExam = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            onAddUser(fullName, email, role, targetExam)
                            if (fullName.isNotBlank() && email.contains("@")) {
                                fullName = ""
                                email = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_add_user_button")
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add User")
                    }
                }
            }
        }

        item {
            Text("Registered Users (${users.size})", style = MaterialTheme.typography.titleMedium)
        }

        items(users, key = { it.id }) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${user.email} • ${user.targetExam}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = user.isActive,
                            onCheckedChange = { onToggleActive(user) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            BrandPillTag(user.role)
                            BrandPillTag(
                                text = if (user.isActive) "Active" else "Paused",
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = { onCycleRole(user) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Change Role", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { onDeleteUser(user.id) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete user")
                            }
                        }
                    }
                }
            }
        }

        item { SmartStudyFooter() }
    }
}
