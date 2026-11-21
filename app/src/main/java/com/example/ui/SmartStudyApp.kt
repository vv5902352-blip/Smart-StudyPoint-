package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.analytics.ProgressTimeRange
import com.example.data.analytics.StudentProgressCalculator
import com.example.data.model.NoteCategory
import com.example.data.model.StudyLanguage
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AssistantAndQuizScreen
import com.example.ui.screens.ChapterQnAScreen
import com.example.ui.screens.FlashcardsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.McqGeneratorScreen
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.NotesGeneratorScreen
import com.example.ui.screens.QnAGeneratorScreen
import com.example.ui.screens.QuizChatTab
import com.example.ui.screens.StudentProgressScreen
import com.example.util.NoteExportUtils
import com.example.util.StudyTtsManager
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.ChapterQnAViewModel
import com.example.viewmodel.FlashcardViewModel
import com.example.viewmodel.StudyViewModel
import kotlinx.coroutines.launch

enum class MainDestination(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    STUDIO(R.string.nav_studio, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    QUIZ_CHAT(R.string.nav_quiz_chat, Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    LIBRARY(R.string.nav_library, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    ADMIN(R.string.nav_admin, Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

enum class StudioMode(val label: String, val icon: ImageVector) {
    CHAPTER_QNA("Chapter Q&A", Icons.Filled.MenuBook),
    FLASHCARDS("Flashcards", Icons.Filled.Style),
    NOTES("AI Notes", Icons.Filled.AutoAwesome),
    MCQ("MCQ Test", Icons.Filled.Quiz),
    QNA("Q&A Bank", Icons.Filled.QuestionAnswer)
}

@Composable
fun SmartStudyApp(
    studyViewModel: StudyViewModel,
    chapterQnAViewModel: ChapterQnAViewModel,
    flashcardViewModel: FlashcardViewModel,
    adminViewModel: AdminViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val ttsManager = remember { StudyTtsManager(context) }
    val isSpeaking by ttsManager.isSpeaking.collectAsStateWithLifecycle()
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    val isDarkMode by studyViewModel.isDarkMode.collectAsStateWithLifecycle()
    val appUsageDayKeys by studyViewModel.appUsageDayKeys.collectAsStateWithLifecycle()
    val allNotes by studyViewModel.allNotes.collectAsStateWithLifecycle()
    val recentNotes by studyViewModel.recentNotes.collectAsStateWithLifecycle()
    val searchQuery by studyViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchHistory by studyViewModel.searchHistory.collectAsStateWithLifecycle()
    val quizAttempts by studyViewModel.quizAttempts.collectAsStateWithLifecycle()
    val classOptions by studyViewModel.classOptions.collectAsStateWithLifecycle()
    val examOptions by studyViewModel.examOptions.collectAsStateWithLifecycle()
    val subjectOptions by studyViewModel.subjectOptions.collectAsStateWithLifecycle()

    val notesState by studyViewModel.notesState.collectAsStateWithLifecycle()
    val savedNoteIdForCurrent by studyViewModel.savedNoteIdForCurrent.collectAsStateWithLifecycle()
    val mcqState by studyViewModel.mcqUiState.collectAsStateWithLifecycle()
    val qnaState by studyViewModel.qnaUiState.collectAsStateWithLifecycle()
    val dailyGkState by studyViewModel.dailyGkState.collectAsStateWithLifecycle()
    val dailyGkLanguage by studyViewModel.dailyGkLanguage.collectAsStateWithLifecycle()
    val chatMessages by studyViewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatSending by studyViewModel.isChatSending.collectAsStateWithLifecycle()
    val chatError by studyViewModel.chatError.collectAsStateWithLifecycle()

    val adminAnalytics by adminViewModel.analytics.collectAsStateWithLifecycle()
    val catalogItems by adminViewModel.catalogItems.collectAsStateWithLifecycle()
    val managedUsers by adminViewModel.managedUsers.collectAsStateWithLifecycle()

    val chapterPackState by chapterQnAViewModel.packState.collectAsStateWithLifecycle()
    val chapterIsPracticeMode by chapterQnAViewModel.isPracticeMode.collectAsStateWithLifecycle()
    val chapterSelectedAnswers by chapterQnAViewModel.selectedAnswers.collectAsStateWithLifecycle()
    val chapterIsTestSubmitted by chapterQnAViewModel.isTestSubmitted.collectAsStateWithLifecycle()
    val chapterIsGeneratingMore by chapterQnAViewModel.isGeneratingMore.collectAsStateWithLifecycle()
    val chapterMarksFilter by chapterQnAViewModel.marksFilter.collectAsStateWithLifecycle()
    val chapterFavoriteQuestions by chapterQnAViewModel.favoriteQuestions.collectAsStateWithLifecycle()
    val chapterPerformanceHistory by chapterQnAViewModel.performanceHistory.collectAsStateWithLifecycle()
    val chapterCurrentReport = remember(chapterPackState, chapterSelectedAnswers, chapterIsTestSubmitted) {
        if (chapterIsTestSubmitted) chapterQnAViewModel.computeCurrentReport() else null
    }

    val flashcardDecks by flashcardViewModel.decks.collectAsStateWithLifecycle()
    val selectedDeckId by flashcardViewModel.selectedDeckId.collectAsStateWithLifecycle()
    val flashcardStudySession by flashcardViewModel.studySession.collectAsStateWithLifecycle()

    val homeProgressSummary = remember(allNotes, quizAttempts, chapterFavoriteQuestions, appUsageDayKeys) {
        StudentProgressCalculator.calculate(
            notes = allNotes,
            quizAttempts = quizAttempts,
            favoriteQuestions = chapterFavoriteQuestions,
            appUsageDayKeys = appUsageDayKeys,
            timeRange = ProgressTimeRange.ALL
        )
    }

    var currentDestination by rememberSaveable { mutableStateOf(MainDestination.HOME) }
    var studioMode by rememberSaveable { mutableStateOf(StudioMode.CHAPTER_QNA) }
    var quizChatTab by rememberSaveable { mutableStateOf(QuizChatTab.DAILY_GK) }
    var libraryCategory by rememberSaveable { mutableStateOf<NoteCategory?>(null) }
    var activeNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showProgressScreen by rememberSaveable { mutableStateOf(false) }

    val activeNote = remember(activeNoteId, allNotes) {
        activeNoteId?.let { id -> allNotes.firstOrNull { it.id == id } }
    }
    val isOverlayOpen = activeNote != null || showProgressScreen

    if (currentDestination != MainDestination.HOME && !isOverlayOpen) {
        BackHandler {
            currentDestination = MainDestination.HOME
        }
    }

    fun showMessage(msg: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun toggleSpeech(text: String, language: StudyLanguage) {
        if (isSpeaking) {
            ttsManager.stop()
            showMessage("Audio playback stopped.")
        } else {
            val started = ttsManager.speak(text, language)
            showMessage(if (started) "Reading study notes aloud..." else "Text-to-Speech is initializing, please tap again.")
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useNavRail && !isOverlayOpen) {
                    NavigationBar {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag("nav_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail && !isOverlayOpen) {
                    NavigationRail {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag("rail_${dest.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)

                    if (activeNote != null) {
                        NoteDetailScreen(
                            note = activeNote,
                            isSpeaking = isSpeaking,
                            onBack = {
                                ttsManager.stop()
                                activeNoteId = null
                            },
                            onToggleFavorite = {
                                studyViewModel.toggleNoteFavorite(activeNote)
                                showMessage(if (activeNote.isFavorite) "Removed from Favourites" else "Saved to Favourites")
                            },
                            onCopy = { gen ->
                                NoteExportUtils.copyToClipboard(context, gen.title, gen.toPlainText())
                                showMessage("Study notes copied to clipboard.")
                            },
                            onShare = { gen ->
                                NoteExportUtils.sharePlainText(context, gen.title, gen.toPlainText())
                            },
                            onExportPdf = { gen ->
                                NoteExportUtils.exportNotesAsPdf(context, gen).fold(
                                    onSuccess = { showMessage(it) },
                                    onFailure = { showMessage(it.message ?: "PDF export failed.") }
                                )
                            },
                            onToggleTts = { gen -> toggleSpeech(gen.toPlainText(), gen.language) },
                            modifier = contentModifier
                        )
                    } else if (showProgressScreen) {
                        StudentProgressScreen(
                            allNotes = allNotes,
                            quizAttempts = quizAttempts,
                            favoriteQuestions = chapterFavoriteQuestions,
                            appUsageDayKeys = appUsageDayKeys,
                            onBack = { showProgressScreen = false },
                            onStartPracticeQuiz = {
                                showProgressScreen = false
                                studioMode = StudioMode.MCQ
                                currentDestination = MainDestination.STUDIO
                            },
                            onGenerateNotes = {
                                showProgressScreen = false
                                studioMode = StudioMode.NOTES
                                currentDestination = MainDestination.STUDIO
                            },
                            modifier = contentModifier
                        )
                    } else {
                        when (currentDestination) {
                            MainDestination.HOME -> HomeScreen(
                                isDarkMode = isDarkMode,
                                searchQuery = searchQuery,
                                searchHistory = searchHistory,
                                allNotes = allNotes,
                                recentNotes = recentNotes,
                                quizCount = quizAttempts.size,
                                streakWidgetInfo = homeProgressSummary.streakWidgetInfo,
                                avgQuizAccuracy = homeProgressSummary.averageAccuracyPercent,
                                onToggleDarkMode = studyViewModel::toggleDarkMode,
                                onSearchQueryChange = studyViewModel::updateSearchQuery,
                                onSearchSubmit = studyViewModel::submitSearch,
                                onClearSearchHistory = studyViewModel::clearSearchHistory,
                                onDeleteHistoryItem = studyViewModel::deleteSearchHistoryItem,
                                onOpenProgress = { showProgressScreen = true },
                                onOpenFlashcards = {
                                    studioMode = StudioMode.FLASHCARDS
                                    currentDestination = MainDestination.STUDIO
                                },
                                onOpenChapterQnA = {
                                    studioMode = StudioMode.CHAPTER_QNA
                                    currentDestination = MainDestination.STUDIO
                                },
                                onOpenNotesGenerator = {
                                    studioMode = StudioMode.NOTES
                                    currentDestination = MainDestination.STUDIO
                                },
                                onOpenMcqGenerator = {
                                    studioMode = StudioMode.MCQ
                                    currentDestination = MainDestination.STUDIO
                                },
                                onOpenQnAGenerator = {
                                    studioMode = StudioMode.QNA
                                    currentDestination = MainDestination.STUDIO
                                },
                                onOpenLibraryCategory = { cat ->
                                    libraryCategory = cat
                                    currentDestination = MainDestination.LIBRARY
                                },
                                onOpenDailyGk = {
                                    quizChatTab = QuizChatTab.DAILY_GK
                                    currentDestination = MainDestination.QUIZ_CHAT
                                },
                                onOpenAiChatbot = {
                                    quizChatTab = QuizChatTab.AI_ASSISTANT
                                    currentDestination = MainDestination.QUIZ_CHAT
                                },
                                onOpenAdmin = {
                                    currentDestination = MainDestination.ADMIN
                                },
                                onOpenNoteDetail = { note ->
                                    studyViewModel.markNoteOpened(note.id)
                                    activeNoteId = note.id
                                },
                                modifier = contentModifier
                            )

                            MainDestination.STUDIO -> Column(modifier = contentModifier) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(StudioMode.entries) { mode ->
                                        val selected = studioMode == mode
                                        FilterChip(
                                            selected = selected,
                                            onClick = { studioMode = mode },
                                            label = {
                                                Text(
                                                    text = mode.label,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = mode.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            modifier = Modifier.testTag("studio_tab_${mode.name.lowercase()}")
                                        )
                                    }
                                }

                                when (studioMode) {
                                    StudioMode.CHAPTER_QNA -> ChapterQnAScreen(
                                        isLiveAiEnabled = chapterQnAViewModel.isLiveAiEnabled,
                                        classOptions = classOptions,
                                        examOptions = examOptions,
                                        subjectOptions = subjectOptions,
                                        packState = chapterPackState,
                                        isPracticeMode = chapterIsPracticeMode,
                                        selectedAnswers = chapterSelectedAnswers,
                                        isTestSubmitted = chapterIsTestSubmitted,
                                        isGeneratingMore = chapterIsGeneratingMore,
                                        marksFilter = chapterMarksFilter,
                                        favoriteQuestions = chapterFavoriteQuestions,
                                        performanceHistory = chapterPerformanceHistory,
                                        currentReport = chapterCurrentReport,
                                        onGeneratePack = chapterQnAViewModel::generateChapterPack,
                                        onRetry = chapterQnAViewModel::retryLastGeneration,
                                        onGenerateMore = {
                                            chapterQnAViewModel.generateMoreQuestions(10) { added ->
                                                showMessage("+$added fresh chapter questions added!")
                                            }
                                        },
                                        onSetPracticeMode = chapterQnAViewModel::setPracticeMode,
                                        onSelectAnswer = chapterQnAViewModel::selectObjectiveAnswer,
                                        onSubmitTest = {
                                            val report = chapterQnAViewModel.submitChapterPracticeTest()
                                            if (report != null) {
                                                showMessage("Practice Test Score: ${report.correctCount}/${report.totalQuestions} (${report.accuracyPercent}%)")
                                            }
                                        },
                                        onResetTest = chapterQnAViewModel::resetPracticeTest,
                                        onSetMarksFilter = chapterQnAViewModel::setMarksFilter,
                                        onToggleFavoriteQuestion = { ch, sub, cls, type, marks, q, a ->
                                            val wasFav = chapterFavoriteQuestions.any { it.questionText == q }
                                            chapterQnAViewModel.toggleFavoriteQuestion(ch, sub, cls, type, marks, q, a)
                                            showMessage(if (wasFav) "Removed from Saved Questions." else "Saved to Favourite Questions!")
                                        },
                                        onDeleteFavoriteQuestion = { id ->
                                            chapterQnAViewModel.deleteFavoriteQuestion(id)
                                            showMessage("Saved question deleted.")
                                        },
                                        onCopyText = { label, text ->
                                            NoteExportUtils.copyToClipboard(context, label, text)
                                            showMessage("$label copied to clipboard.")
                                        },
                                        onShareText = { title, text ->
                                            NoteExportUtils.sharePlainText(context, title, text)
                                        },
                                        onExportPdf = { pack ->
                                            NoteExportUtils.exportTextAsPdf(
                                                context = context,
                                                title = "${pack.request.chapterName} — Chapter Q&A",
                                                subtitle = "${pack.request.classLevel} • ${pack.request.subject} • ${pack.request.boardExam} • ${pack.request.language.label}",
                                                bodyText = pack.toPlainText()
                                            ).fold(
                                                onSuccess = { showMessage(it) },
                                                onFailure = { showMessage(it.message ?: "PDF export failed.") }
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    StudioMode.FLASHCARDS -> FlashcardsScreen(
                                        decks = flashcardDecks,
                                        selectedDeckId = selectedDeckId,
                                        studySession = flashcardStudySession,
                                        subjectOptions = subjectOptions,
                                        examOptions = examOptions,
                                        onOpenDeck = flashcardViewModel::openDeck,
                                        onCloseDeck = flashcardViewModel::closeDeck,
                                        onCreateDeck = { title, subject, exam, desc ->
                                            flashcardViewModel.createDeck(title, subject, exam, desc) { res ->
                                                res.fold(
                                                    onSuccess = { showMessage("Created deck \"$title\"!") },
                                                    onFailure = { showMessage(it.message ?: "Could not create deck.") }
                                                )
                                            }
                                        },
                                        onDeleteDeck = { id ->
                                            flashcardViewModel.deleteDeck(id) {
                                                showMessage("Flashcard deck deleted.")
                                            }
                                        },
                                        onAddCard = { deckId, term, def, hint ->
                                            flashcardViewModel.addCard(deckId, term, def, hint) { res ->
                                                res.fold(
                                                    onSuccess = { showMessage("Flashcard added to deck!") },
                                                    onFailure = { showMessage(it.message ?: "Could not save card.") }
                                                )
                                            }
                                        },
                                        onUpdateCard = { card, term, def, hint ->
                                            flashcardViewModel.updateCard(card, term, def, hint) { res ->
                                                res.fold(
                                                    onSuccess = { showMessage("Flashcard updated.") },
                                                    onFailure = { showMessage(it.message ?: "Could not update card.") }
                                                )
                                            }
                                        },
                                        onDeleteCard = { cardId ->
                                            flashcardViewModel.deleteCard(cardId) {
                                                showMessage("Flashcard deleted.")
                                            }
                                        },
                                        onResetDeckMastery = { deckId ->
                                            flashcardViewModel.resetDeckMastery(deckId) {
                                                showMessage("Deck mastery progress reset.")
                                            }
                                        },
                                        onStartStudy = { deckWithCards, onlyUnmastered, shuffle ->
                                            val started = flashcardViewModel.startStudySession(
                                                deckWithCards = deckWithCards,
                                                onlyUnmastered = onlyUnmastered,
                                                shuffle = shuffle
                                            )
                                            if (!started) {
                                                showMessage("Add at least one card to start Active Recall practice.")
                                            }
                                        },
                                        onFlipCard = flashcardViewModel::flipCard,
                                        onToggleHint = flashcardViewModel::toggleHint,
                                        onRateCard = flashcardViewModel::rateCurrentCard,
                                        onRestartMissedOnly = flashcardViewModel::restartMissedCardsOnly,
                                        onRestartAll = flashcardViewModel::restartCurrentSession,
                                        onExitStudy = flashcardViewModel::exitStudySession,
                                        modifier = Modifier.weight(1f)
                                    )

                                    StudioMode.NOTES -> NotesGeneratorScreen(
                                        isLiveAiEnabled = studyViewModel.isLiveAiEnabled,
                                        classOptions = classOptions,
                                        examOptions = examOptions,
                                        subjectOptions = subjectOptions,
                                        notesState = notesState,
                                        isSaved = savedNoteIdForCurrent != null,
                                        isSpeaking = isSpeaking,
                                        onGenerate = studyViewModel::generateNotes,
                                        onRetry = studyViewModel::retryLastNotes,
                                        onSaveNotes = { fav ->
                                            studyViewModel.saveCurrentGeneratedNotes(isFavorite = fav) {
                                                showMessage(if (fav) "Saved to Favourites!" else "Notes saved to Library!")
                                            }
                                        },
                                        onCopyNotes = { gen ->
                                            NoteExportUtils.copyToClipboard(context, gen.title, gen.toPlainText())
                                            showMessage("Study notes copied to clipboard.")
                                        },
                                        onShareNotes = { gen ->
                                            NoteExportUtils.sharePlainText(context, gen.title, gen.toPlainText())
                                        },
                                        onExportPdf = { gen ->
                                            NoteExportUtils.exportNotesAsPdf(context, gen).fold(
                                                onSuccess = { showMessage(it) },
                                                onFailure = { showMessage(it.message ?: "PDF export failed.") }
                                            )
                                        },
                                        onToggleTts = { gen -> toggleSpeech(gen.toPlainText(), gen.language) },
                                        modifier = Modifier.weight(1f)
                                    )

                                    StudioMode.MCQ -> McqGeneratorScreen(
                                        isLiveAiEnabled = studyViewModel.isLiveAiEnabled,
                                        classOptions = classOptions,
                                        examOptions = examOptions,
                                        subjectOptions = subjectOptions,
                                        mcqState = mcqState,
                                        onGenerate = studyViewModel::generateMcqs,
                                        onRetry = studyViewModel::retryLastMcqs,
                                        onSelectOption = studyViewModel::selectMcqOption,
                                        onSetIndex = studyViewModel::setMcqIndex,
                                        onSubmitTest = {
                                            studyViewModel.submitMcqTest()
                                            showMessage("Practice test submitted! Check your score & explanations.")
                                        },
                                        onResetTest = studyViewModel::resetMcqTest,
                                        modifier = Modifier.weight(1f)
                                    )

                                    StudioMode.QNA -> QnAGeneratorScreen(
                                        isLiveAiEnabled = studyViewModel.isLiveAiEnabled,
                                        classOptions = classOptions,
                                        examOptions = examOptions,
                                        subjectOptions = subjectOptions,
                                        qnaState = qnaState,
                                        isSpeaking = isSpeaking,
                                        onGenerate = studyViewModel::generateQnA,
                                        onRetry = studyViewModel::retryLastQnA,
                                        onSaveToLibrary = { req, list ->
                                            studyViewModel.saveQnAToLibrary(req, list) {
                                                showMessage("Q&A Bank saved to Important Questions!")
                                            }
                                        },
                                        onCopyAll = { text ->
                                            NoteExportUtils.copyToClipboard(context, "Smart StudyPoint Q&A", text)
                                            showMessage("All Questions & Answers copied.")
                                        },
                                        onShareAll = { title, text ->
                                            NoteExportUtils.sharePlainText(context, title, text)
                                        },
                                        onExportPdf = { title, sub, body ->
                                            NoteExportUtils.exportTextAsPdf(context, title, sub, body).fold(
                                                onSuccess = { showMessage(it) },
                                                onFailure = { showMessage(it.message ?: "PDF export failed.") }
                                            )
                                        },
                                        onToggleTts = { text, lang -> toggleSpeech(text, lang) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            MainDestination.QUIZ_CHAT -> AssistantAndQuizScreen(
                                selectedTab = quizChatTab,
                                onSelectTab = { quizChatTab = it },
                                isLiveAiEnabled = studyViewModel.isLiveAiEnabled,
                                dailyGkState = dailyGkState,
                                dailyGkLanguage = dailyGkLanguage,
                                onLoadDailyGk = studyViewModel::loadDailyGkQuiz,
                                onSelectDailyGkOption = studyViewModel::selectDailyGkOption,
                                onSubmitDailyGk = {
                                    studyViewModel.submitDailyGkQuiz()
                                    showMessage("Daily GK Quiz submitted! Review explanations below.")
                                },
                                chatMessages = chatMessages,
                                isChatSending = isChatSending,
                                chatError = chatError,
                                examOptions = examOptions,
                                isSpeaking = isSpeaking,
                                onSendChat = studyViewModel::sendChatMessage,
                                onClearChat = {
                                    studyViewModel.clearChat()
                                    showMessage("Chat history cleared.")
                                },
                                onCopyMessage = { text ->
                                    NoteExportUtils.copyToClipboard(context, "AI Tutor Reply", text)
                                    showMessage("Copied tutor reply.")
                                },
                                onToggleTts = { text, lang -> toggleSpeech(text, lang) },
                                modifier = contentModifier
                            )

                            MainDestination.LIBRARY -> LibraryScreen(
                                allNotes = allNotes,
                                selectedCategory = libraryCategory,
                                onSelectCategory = { libraryCategory = it },
                                onOpenNote = { note ->
                                    studyViewModel.markNoteOpened(note.id)
                                    activeNoteId = note.id
                                },
                                onToggleFavorite = { note ->
                                    studyViewModel.toggleNoteFavorite(note)
                                    showMessage(if (note.isFavorite) "Removed from Favourites" else "Added to Favourites")
                                },
                                onDeleteNote = { id ->
                                    studyViewModel.deleteNote(id)
                                    showMessage("Note deleted from Library.")
                                },
                                modifier = contentModifier
                            )

                            MainDestination.ADMIN -> AdminDashboardScreen(
                                analytics = adminAnalytics,
                                allNotes = allNotes,
                                catalogItems = catalogItems,
                                managedUsers = managedUsers,
                                classOptions = classOptions,
                                examOptions = examOptions,
                                subjectOptions = subjectOptions,
                                onPublishNote = { title, cls, exam, subj, cat, lang, body, bullets ->
                                    adminViewModel.publishImportantNote(
                                        title, cls, exam, subj, cat, lang, body, bullets
                                    ) { result ->
                                        result.fold(
                                            onSuccess = { showMessage("Published \"$title\" to Study Library!") },
                                            onFailure = { showMessage(it.message ?: "Fill title and content.") }
                                        )
                                    }
                                },
                                onDeleteNote = { id ->
                                    adminViewModel.deleteNote(id)
                                    showMessage("Content deleted.")
                                },
                                onAddCatalogItem = { type, name ->
                                    adminViewModel.addCatalogItem(type, name) { res ->
                                        res.fold(
                                            onSuccess = { showMessage("Added ${type.label}: $name") },
                                            onFailure = { showMessage(it.message ?: "Invalid name.") }
                                        )
                                    }
                                },
                                onDeleteCatalogItem = { id ->
                                    adminViewModel.deleteCatalogItem(id)
                                    showMessage("Catalog entry removed.")
                                },
                                onAddUser = { name, email, role, exam ->
                                    adminViewModel.addUser(name, email, role, exam) { res ->
                                        res.fold(
                                            onSuccess = { showMessage("Added user $name") },
                                            onFailure = { showMessage(it.message ?: "Enter valid name & email.") }
                                        )
                                    }
                                },
                                onToggleUserActive = { user ->
                                    adminViewModel.toggleUserActive(user)
                                    showMessage("Updated status for ${user.fullName}")
                                },
                                onCycleUserRole = { user ->
                                    adminViewModel.cycleUserRole(user)
                                    showMessage("Updated role for ${user.fullName}")
                                },
                                onDeleteUser = { id ->
                                    adminViewModel.deleteUser(id)
                                    showMessage("User removed.")
                                },
                                modifier = contentModifier
                            )
                        }
                    }
                }
            }
        }
    }
}
