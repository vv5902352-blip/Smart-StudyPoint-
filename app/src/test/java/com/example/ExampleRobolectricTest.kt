package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.model.ChapterQnARequest
import com.example.data.model.McqRequest
import com.example.data.model.NotesRequest
import com.example.data.model.NotesType
import com.example.data.model.ObjectiveKind
import com.example.data.model.QnARequest
import com.example.data.model.QnAType
import com.example.data.model.StudyLanguage
import com.example.data.model.SubjectiveCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Smart StudyPoint`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart StudyPoint", appName)
    }

    @Test
    fun `placeholder ai generates complete notes and requested mcq counts`() = runTest {
        val repo = PlaceholderAiRepository()

        val notesResult = repo.generateNotes(
            NotesRequest(
                classLevel = "Class 10th",
                exam = "CBSE Board",
                subject = "Biology",
                topic = "Photosynthesis",
                language = StudyLanguage.ENGLISH,
                notesType = NotesType.COMPLETE_PACK
            )
        )
        assertTrue(notesResult.isSuccess)
        val notes = notesResult.getOrThrow()
        assertTrue(notes.shortNotes.isNotEmpty())
        assertTrue(notes.definitions.isNotEmpty())
        assertTrue(notes.flowchartSteps.isNotEmpty())
        assertTrue(notes.comparisonTable.rows.isNotEmpty())

        val mcqResult = repo.generateMcqs(
            McqRequest(
                classLevel = "Graduate / Competitive",
                exam = "UPSC CSE",
                subject = "Indian Polity",
                topic = "Fundamental Rights",
                language = StudyLanguage.HINDI,
                count = 30
            )
        )
        assertTrue(mcqResult.isSuccess)
        assertEquals(30, mcqResult.getOrThrow().size)

        val qnaResult = repo.generateQnA(
            QnARequest(
                classLevel = "Class 12th",
                exam = "CBSE Board",
                subject = "Physics",
                topic = "Electromagnetism",
                language = StudyLanguage.HINGLISH,
                selectedTypes = QnAType.entries.toSet()
            )
        )
        assertTrue(qnaResult.isSuccess)
        assertEquals(QnAType.entries.size * 2, qnaResult.getOrThrow().size)
    }

    @Test
    fun `class 10 science life processes chapter pack covers all 8 sections and visual answers`() = runTest {
        val repo = PlaceholderAiRepository()
        val request = ChapterQnARequest(
            classLevel = "Class 10th",
            subject = "General Science",
            chapterName = "Life Processes",
            boardExam = "CBSE Board",
            language = StudyLanguage.ENGLISH,
            objectiveCount = 20
        )

        val pack = repo.generateChapterStudyPack(request).getOrThrow()

        // 1. Chapter Introduction
        assertTrue(pack.chapterIntro.overview.isNotBlank())
        assertTrue(pack.chapterIntro.keyTopics.size >= 4)
        assertTrue(pack.chapterIntro.definitionBox.term.isNotBlank())
        assertTrue(pack.chapterIntro.definitionBox.definition.isNotBlank())

        // 2 & 3. Objective Questions (exact count, all 5 kinds, numbered 1..20, zero duplicates)
        assertEquals(20, pack.objectiveQuestions.size)
        val kinds = pack.objectiveQuestions.map { it.kind }.toSet()
        assertTrue(kinds.containsAll(ObjectiveKind.entries))
        assertEquals((1..20).toList(), pack.objectiveQuestions.map { it.number })
        assertEquals(20, pack.objectiveQuestions.map { it.question }.toSet().size)
        assertTrue(pack.objectiveQuestions.all { it.options.size >= 2 && it.correctAnswerText.isNotBlank() && it.explanation.isNotBlank() })

        // 4 & 5. Subjective Questions (1M, 2-3M, 4-6M, Difference table, Flowchart, Mind Map, Highlights)
        assertTrue(pack.shortAnswers.any { it.category == SubjectiveCategory.VERY_SHORT })
        assertTrue(pack.shortAnswers.any { it.category == SubjectiveCategory.SHORT })
        assertTrue(pack.shortAnswers.any { it.category == SubjectiveCategory.DIFFERENCE_BETWEEN && it.comparisonTable != null })
        assertTrue(pack.longAnswers.any { it.category == SubjectiveCategory.LONG || it.category == SubjectiveCategory.EXPLAIN_DETAIL })
        assertTrue(pack.longAnswers.any { it.flowchartSteps.isNotEmpty() })
        assertTrue(pack.longAnswers.any { it.mindMap != null })
        val allSubjective = pack.shortAnswers + pack.longAnswers + pack.mostImportantExamQuestions
        assertTrue(allSubjective.all { it.bulletPoints.isNotEmpty() && it.keywordsToHighlight.isNotEmpty() })

        // 6, 7, 8. Diagrams, Important Exam Qs, Quick Revision
        assertTrue(pack.diagramQuestions.size >= 2)
        assertTrue(pack.diagramQuestions.all { it.labels.isNotEmpty() && it.answerPoints.isNotEmpty() })
        assertTrue(pack.mostImportantExamQuestions.isNotEmpty())
        assertTrue(pack.quickRevisionQuestions.size >= 5)

        // Generate More (+10 unique questions numbered 21..30)
        val more = repo.generateMoreChapterQuestions(pack, 10).getOrThrow()
        assertEquals(10, more.size)
        assertEquals((21..30).toList(), more.map { it.number })
        val combinedTexts = (pack.objectiveQuestions + more).map { it.question }.toSet()
        assertEquals(30, combinedTexts.size)

        // PDF Plain Text Export contains all 8 numbered section headers
        val pdfText = pack.toPlainText()
        listOf(
            "1. CHAPTER INTRODUCTION",
            "2. IMPORTANT OBJECTIVE QUESTIONS",
            "3. OBJECTIVE ANSWER KEY",
            "4. SHORT ANSWER QUESTIONS",
            "5. LONG ANSWER QUESTIONS",
            "6. DIAGRAM-BASED QUESTIONS",
            "7. MOST IMPORTANT EXAM QUESTIONS",
            "8. QUICK REVISION QUESTIONS"
        ).forEach { header ->
            assertTrue("Missing section header: $header", pdfText.contains(header))
        }
    }

    @Test
    fun `class 10 science chapter supports 10 20 30 50 100 counts without duplicates and Hindi language`() = runTest {
        val repo = PlaceholderAiRepository()

        listOf(10, 20, 30, 50, 100).forEach { qty ->
            val pack = repo.generateChapterStudyPack(
                ChapterQnARequest(
                    classLevel = "Class 10th",
                    subject = "General Science",
                    chapterName = "Life Processes",
                    boardExam = "CBSE Board",
                    language = StudyLanguage.ENGLISH,
                    objectiveCount = qty
                )
            ).getOrThrow()
            assertEquals("Expected $qty objective questions", qty, pack.objectiveQuestions.size)
            assertEquals(
                "Duplicate objective question found for qty=$qty",
                qty,
                pack.objectiveQuestions.map { it.question }.toSet().size
            )
        }

        val hindiPack = repo.generateChapterStudyPack(
            ChapterQnARequest(
                classLevel = "Class 10th",
                subject = "General Science",
                chapterName = "Life Processes",
                boardExam = "CBSE Board",
                language = StudyLanguage.HINDI,
                objectiveCount = 10
            )
        ).getOrThrow()
        assertEquals(10, hindiPack.objectiveQuestions.size)
        assertTrue(hindiPack.chapterIntro.overview.contains("जैव प्रक्रम"))
        assertNotNull(hindiPack.longAnswers.firstOrNull()?.mindMap)
    }

    @Test
    fun `student progress calculator computes quiz trends notes buckets and study streaks accurately`() {
        val now = System.currentTimeMillis()
        val notes = com.example.data.local.SeedDataProvider.defaultStudyNotes()
        val quizzes = com.example.data.local.SeedDataProvider.defaultQuizAttempts()

        val summary = com.example.data.analytics.StudentProgressCalculator.calculate(
            notes = notes,
            quizAttempts = quizzes,
            favoriteQuestions = emptyList(),
            timeRange = com.example.data.analytics.ProgressTimeRange.DAYS_14,
            nowMillis = now
        )

        // 6 consecutive days of seeded study activity (day -5 through today)
        assertTrue("Expected at least 6-day streak, got ${summary.currentStreakDays}", summary.currentStreakDays >= 6)
        assertTrue(summary.longestStreakDays >= summary.currentStreakDays)
        assertTrue(summary.studiedToday)
        assertEquals(28, summary.heatmapDays.size)
        assertTrue(summary.heatmapDays.last().isToday)

        // Quiz performance trend is chronologically ordered with positive upward delta
        assertEquals(6, summary.quizTrendPoints.size)
        assertTrue(summary.averageAccuracyPercent in 75..85)
        assertEquals(90, summary.bestScorePercent)
        assertTrue("Expected positive trend delta", summary.accuracyDeltaPercent > 0)

        // Notes trend buckets & category breakdown
        assertEquals(notes.size, summary.totalNotesGenerated)
        assertTrue(summary.notesTrendBuckets.isNotEmpty())
        assertEquals(notes.size, summary.notesTrendBuckets.last().cumulativeCount)
        assertTrue(summary.categoryBreakdown.isNotEmpty())
        assertTrue(summary.subjectMastery.isNotEmpty())

        // Empty dataset handles cleanly without crashing
        val emptySummary = com.example.data.analytics.StudentProgressCalculator.calculate(
            notes = emptyList(),
            quizAttempts = emptyList(),
            favoriteQuestions = emptyList(),
            timeRange = com.example.data.analytics.ProgressTimeRange.DAYS_7,
            nowMillis = now
        )
        assertEquals(0, emptySummary.currentStreakDays)
        assertEquals(0, emptySummary.averageAccuracyPercent)
        assertEquals(0, emptySummary.totalNotesGenerated)
        assertEquals(28, emptySummary.heatmapDays.size)
        assertEquals(7, emptySummary.streakWidgetInfo.nextGoalDays)
        assertEquals(0f, emptySummary.streakWidgetInfo.progressFraction, 0.001f)
    }

    @Test
    fun `daily study streak tracks consecutive app usage days and milestone ring progress`() {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val consecutive4Days = (0..3).map { d ->
            com.example.data.analytics.StudentProgressCalculator.toDayKey(now - d * dayMs)
        }.toSet()

        val summary = com.example.data.analytics.StudentProgressCalculator.calculate(
            notes = emptyList(),
            quizAttempts = emptyList(),
            favoriteQuestions = emptyList(),
            appUsageDayKeys = consecutive4Days,
            nowMillis = now
        )

        val widget = summary.streakWidgetInfo
        assertEquals(4, widget.currentStreakDays)
        assertEquals(4, widget.longestStreakDays)
        assertEquals(7, widget.nextGoalDays)
        assertEquals(4f / 7f, widget.progressFraction, 0.01f)
        assertTrue(widget.studiedToday)
        assertEquals(7, widget.recentSevenDays.size)
        assertTrue(widget.recentSevenDays.last().appOpened)
        assertTrue(widget.motivationalMessage.contains("7-day"))
    }

    @Test
    fun `room flashcard decks support creating decks cards active recall mastery and cascade delete`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.SmartStudyDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val dao = db.flashcardDao()

            // 1. Create a deck
            val deckId = dao.insertDeck(
                com.example.data.local.FlashcardDeckEntity(
                    title = "Physics — Electricity Formulas",
                    subject = "Physics",
                    examTag = "CBSE Board",
                    description = "Key formulas and SI units"
                )
            )
            assertTrue(deckId > 0L)

            // 2. Add term & definition flashcards
            val card1Id = dao.insertCard(
                com.example.data.local.FlashcardEntity(
                    deckId = deckId,
                    term = "Ohm's Law",
                    definition = "At constant temperature, current through a conductor is directly proportional to potential difference (V = I × R).",
                    hint = "V = IR"
                )
            )
            val card2Id = dao.insertCard(
                com.example.data.local.FlashcardEntity(
                    deckId = deckId,
                    term = "Electric Power",
                    definition = "Rate of doing electrical work: P = V × I = I²R = V²/R, measured in Watts (W).",
                    hint = "SI unit is Watt"
                )
            )

            // 3. Observe decks with relational cards
            val initialDecks = dao.observeDecksWithCards().first()
            assertEquals(1, initialDecks.size)
            assertEquals(2, initialDecks.first().totalCards)
            assertEquals(0, initialDecks.first().masteredCount)

            // 4. Active recall: Got It on card1, Still Learning on card2
            dao.recordCardRecall(cardId = card1Id, isMastered = true, correctDelta = 1)
            dao.recordCardRecall(cardId = card2Id, isMastered = false, correctDelta = 0)
            val afterRecall = dao.observeDecksWithCards().first().first()
            assertEquals(1, afterRecall.masteredCount)
            assertEquals(50, afterRecall.masteryPercent)
            val updatedCard1 = afterRecall.cards.first { it.id == card1Id }
            assertEquals(1, updatedCard1.reviewCount)
            assertEquals(1, updatedCard1.correctCount)

            // 5. Update card2 definition & reset deck mastery
            val card2 = afterRecall.cards.first { it.id == card2Id }
            dao.updateCard(card2.copy(definition = "Updated definition: P = VI (Watt)"))
            dao.resetDeckMastery(deckId)
            val afterReset = dao.observeDecksWithCards().first().first()
            assertEquals(0, afterReset.masteredCount)
            assertTrue(afterReset.cards.first { it.id == card2Id }.definition.contains("Updated definition"))

            // 6. Delete deck cascades to all child flashcards
            dao.deleteDeckById(deckId)
            val afterDelete = dao.observeDecksWithCards().first()
            val remainingCards = dao.observeCardsForDeck(deckId).first()
            assertEquals(0, afterDelete.size)
            assertEquals(0, remainingCards.size)
        } finally {
            db.close()
        }
    }

    @Test
    fun `firebase ai repository enables live gemini and parses detailed handwritten notes json`() {
        val repo = com.example.data.ai.FirebaseAiRepository()
        assertTrue(repo.isLiveAiEnabled)
        assertEquals(NotesType.HANDWRITTEN_DETAILED, NotesType.entries.first())

        val request = NotesRequest(
            classLevel = "Class 10th",
            exam = "CBSE Board",
            subject = "Science",
            topic = "Photosynthesis",
            language = StudyLanguage.HINGLISH,
            notesType = NotesType.HANDWRITTEN_DETAILED
        )

        val sampleJson = """
            {
              "title": "Photosynthesis — Detailed Handwritten Notes",
              "detailedNotes": "1. Introduction\nPhotosynthesis woh process hai jisme green plants sunlight use karke apna food banate hain.\n• Chlorophyll light energy absorb karta hai.",
              "shortNotes": ["Equation: 6CO2 + 6H2O -> C6H12O6 + 6O2", "Occurs inside Chloroplast"],
              "easyExplanation": "Jaise ghar ki kitchen me gas se khana banta hai, waise hi leaf plant ki kitchen hai.",
              "definitions": [
                {"term": "Photosynthesis", "definition": "Sunlight aur chlorophyll ki help se CO2 aur water ko glucose me convert karne ka process."}
              ],
              "importantFacts": ["Stomata gas exchange control karta hai."],
              "comparisonTable": {
                "title": "Light vs Dark Reaction",
                "headers": ["Basis", "Light Reaction", "Dark Reaction"],
                "rows": [
                  ["Site", "Grana / Thylakoid", "Stroma"],
                  ["Requirement", "Needs sunlight directly", "Uses ATP & NADPH"]
                ]
              },
              "flowchartSteps": ["Sunlight absorbed by chlorophyll", "Photolysis of water", "Formation of glucose"],
              "examples": ["Green leaves of Mango and Peepal trees"],
              "examOrientedPoints": ["Balanced equation zaroor likhein — 1 full mark milta hai!"],
              "quickRevision": ["Site: Chloroplast", "By-product: Oxygen (O2)"]
            }
        """.trimIndent()

        val parsed = com.example.data.ai.FirebaseAiSchemas.parseGeneratedNotes(sampleJson, request)
        assertEquals(false, parsed.isSamplePreview)
        assertEquals("Photosynthesis — Detailed Handwritten Notes", parsed.title)
        assertEquals(NotesType.HANDWRITTEN_DETAILED, parsed.notesType)
        assertEquals(1, parsed.definitions.size)
        assertEquals("Photosynthesis", parsed.definitions.first().term)
        assertEquals(2, parsed.comparisonTable.rows.size)
        assertEquals(3, parsed.flowchartSteps.size)
        assertTrue(parsed.examOrientedPoints.first().contains("Balanced equation"))
    }
}
