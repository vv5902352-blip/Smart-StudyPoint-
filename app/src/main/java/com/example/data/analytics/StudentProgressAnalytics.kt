package com.example.data.analytics

import com.example.data.local.FavoriteQuestionEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.noteCategory
import com.example.data.model.NoteCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class ProgressTimeRange(val label: String, val days: Int?) {
    DAYS_7("7 Days", 7),
    DAYS_14("14 Days", 14),
    DAYS_30("30 Days", 30),
    ALL("All Time", null)
}

data class QuizTrendPoint(
    val id: Long,
    val title: String,
    val subject: String,
    val exam: String,
    val dateLabel: String,
    val fullDateLabel: String,
    val scorePercent: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val isDailyGk: Boolean,
    val completedAt: Long
)

data class NotesTrendBucket(
    val dayStartMillis: Long,
    val label: String,
    val fullDateLabel: String,
    val count: Int,
    val cumulativeCount: Int
)

data class StreakDayCell(
    val dayStartMillis: Long,
    val dateLabel: String,
    val dayOfMonth: String,
    val dayOfWeekShort: String = "",
    val notesCount: Int,
    val quizzesCount: Int,
    val savedQuestionsCount: Int,
    val appOpened: Boolean = false,
    val isToday: Boolean
) {
    val totalActivities: Int get() = notesCount + quizzesCount + savedQuestionsCount + if (appOpened) 1 else 0
    val intensityLevel: Int
        get() = when {
            totalActivities <= 0 -> 0
            totalActivities == 1 -> 1
            totalActivities == 2 -> 2
            totalActivities <= 4 -> 3
            else -> 4
        }
}

data class DailyStreakWidgetInfo(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val nextGoalDays: Int,
    val progressFraction: Float,
    val studiedToday: Boolean,
    val motivationalMessage: String,
    val recentSevenDays: List<StreakDayCell>
)

data class CategorySlice(
    val category: NoteCategory,
    val count: Int,
    val percentage: Int
)

data class SubjectMasteryItem(
    val subject: String,
    val averageAccuracy: Int,
    val quizzesTaken: Int,
    val notesCount: Int
)

data class StudentProgressSummary(
    val timeRange: ProgressTimeRange,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val studiedToday: Boolean,
    val totalActiveDays: Int,
    val totalNotesGenerated: Int,
    val notesInRange: Int,
    val favoriteNotesCount: Int,
    val savedQuestionsCount: Int,
    val totalQuizzesCompleted: Int,
    val totalQuestionsAttempted: Int,
    val averageAccuracyPercent: Int,
    val bestScorePercent: Int,
    val accuracyDeltaPercent: Int,
    val quizTrendPoints: List<QuizTrendPoint>,
    val notesTrendBuckets: List<NotesTrendBucket>,
    val heatmapDays: List<StreakDayCell>,
    val categoryBreakdown: List<CategorySlice>,
    val subjectMastery: List<SubjectMasteryItem>,
    val streakWidgetInfo: DailyStreakWidgetInfo
)

object StudentProgressCalculator {

    private const val DAY_MS = 86_400_000L

    fun toDayKey(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))

    fun parseDayKeyToStartMillis(dayKey: String): Long? {
        return try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey) ?: return null
            startOfDayMillis(parsed.time)
        } catch (_: Exception) {
            null
        }
    }

    fun calculate(
        notes: List<StudyNoteEntity>,
        quizAttempts: List<QuizAttemptEntity>,
        favoriteQuestions: List<FavoriteQuestionEntity>,
        appUsageDayKeys: Set<String> = emptySet(),
        timeRange: ProgressTimeRange = ProgressTimeRange.DAYS_14,
        nowMillis: Long = System.currentTimeMillis()
    ): StudentProgressSummary {
        val todayStart = startOfDayMillis(nowMillis)
        val rangeStart = timeRange.days?.let { days -> todayStart - (days - 1).toLong() * DAY_MS }

        val shortDateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dayShortFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val fullDateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dayNumFmt = SimpleDateFormat("d", Locale.getDefault())

        // 1. Filter records by time range
        val filteredQuizzes = quizAttempts
            .filter { rangeStart == null || it.completedAt >= rangeStart }
            .sortedBy { it.completedAt }

        val filteredNotes = notes
            .filter { rangeStart == null || it.createdAt >= rangeStart }
            .sortedBy { it.createdAt }

        // 2. Historical Quiz Performance Trend
        val quizPoints = filteredQuizzes.map { attempt ->
            val pct = if (attempt.totalQuestions > 0) {
                ((attempt.correctAnswers * 100f) / attempt.totalQuestions).roundToInt().coerceIn(0, 100)
            } else 0
            QuizTrendPoint(
                id = attempt.id,
                title = attempt.title,
                subject = attempt.subject,
                exam = attempt.exam,
                dateLabel = shortDateFmt.format(Date(attempt.completedAt)),
                fullDateLabel = fullDateFmt.format(Date(attempt.completedAt)),
                scorePercent = pct,
                correctAnswers = attempt.correctAnswers,
                totalQuestions = attempt.totalQuestions,
                isDailyGk = attempt.isDailyGk,
                completedAt = attempt.completedAt
            )
        }

        val totalQuestionsAttempted = filteredQuizzes.sumOf { it.totalQuestions }
        val totalCorrect = filteredQuizzes.sumOf { it.correctAnswers }
        val averageAccuracy = if (totalQuestionsAttempted > 0) {
            ((totalCorrect * 100f) / totalQuestionsAttempted).roundToInt().coerceIn(0, 100)
        } else 0
        val bestScore = quizPoints.maxOfOrNull { it.scorePercent } ?: 0

        val accuracyDelta = if (quizPoints.size >= 2) {
            val mid = quizPoints.size / 2
            val firstHalfAvg = quizPoints.take(mid).map { it.scorePercent }.average()
            val secondHalfAvg = quizPoints.drop(mid).map { it.scorePercent }.average()
            (secondHalfAvg - firstHalfAvg).roundToInt()
        } else 0

        // 3. Notes Generated Trend (daily buckets over selected window)
        val bucketDaysCount = when (timeRange) {
            ProgressTimeRange.DAYS_7 -> 7
            ProgressTimeRange.DAYS_14 -> 14
            ProgressTimeRange.DAYS_30 -> 14 // 14 evenly spaced 2-day buckets or last 14 days for readability
            ProgressTimeRange.ALL -> 10
        }
        val notesBuckets = buildNotesBuckets(
            notes = notes,
            timeRange = timeRange,
            todayStart = todayStart,
            bucketCount = bucketDaysCount,
            shortDateFmt = shortDateFmt,
            dayShortFmt = dayShortFmt,
            fullDateFmt = fullDateFmt
        )

        // 4. Study Streak Calculation (all-time app usage + study activity days)
        val notesByDay = notes.groupingBy { startOfDayMillis(it.createdAt) }.eachCount()
        val quizzesByDay = quizAttempts.groupingBy { startOfDayMillis(it.completedAt) }.eachCount()
        val favQsByDay = favoriteQuestions.groupingBy { startOfDayMillis(it.savedAt) }.eachCount()
        val usageDayStarts = appUsageDayKeys.mapNotNull { parseDayKeyToStartMillis(it) }.toSet()

        val allActiveDayStarts = buildSet {
            addAll(usageDayStarts)
            addAll(notesByDay.keys)
            addAll(quizzesByDay.keys)
            addAll(favQsByDay.keys)
            notes.forEach { add(startOfDayMillis(it.lastOpenedAt)) }
        }

        val studiedToday = todayStart in allActiveDayStarts
        val currentStreak = computeCurrentStreak(allActiveDayStarts, todayStart)
        val longestStreak = computeLongestStreak(allActiveDayStarts)

        // 5. 28-Day Study Streak Heatmap (4 weeks ending today)
        val heatmapDays = (27 downTo 0).map { daysAgo ->
            val dayStart = todayStart - daysAgo * DAY_MS
            val date = Date(dayStart)
            StreakDayCell(
                dayStartMillis = dayStart,
                dateLabel = shortDateFmt.format(date),
                dayOfMonth = dayNumFmt.format(date),
                dayOfWeekShort = dayShortFmt.format(date).take(1).uppercase(Locale.getDefault()),
                notesCount = notesByDay[dayStart] ?: 0,
                quizzesCount = quizzesByDay[dayStart] ?: 0,
                savedQuestionsCount = favQsByDay[dayStart] ?: 0,
                appOpened = dayStart in usageDayStarts,
                isToday = daysAgo == 0
            )
        }

        val widgetInfo = buildStreakWidgetInfo(
            currentStreakDays = currentStreak,
            longestStreakDays = maxOf(longestStreak, currentStreak),
            studiedToday = studiedToday,
            recentSevenDays = heatmapDays.takeLast(7)
        )

        // 6. Notes Category Breakdown
        val totalNotes = notes.size
        val categoryCounts = notes.groupingBy { it.noteCategory() }.eachCount()
        val categorySlices = NoteCategory.entries.mapNotNull { cat ->
            val cnt = categoryCounts[cat] ?: 0
            if (cnt == 0) null
            else CategorySlice(
                category = cat,
                count = cnt,
                percentage = ((cnt * 100f) / totalNotes.coerceAtLeast(1)).roundToInt()
            )
        }.sortedByDescending { it.count }

        // 7. Subject Mastery Breakdown
        val allSubjects = (filteredQuizzes.map { it.subject } + notes.map { it.subject }).toSet()
        val subjectMastery = allSubjects.map { subj ->
            val subjQuizzes = filteredQuizzes.filter { it.subject.equals(subj, ignoreCase = true) }
            val qTotal = subjQuizzes.sumOf { it.totalQuestions }
            val qCorrect = subjQuizzes.sumOf { it.correctAnswers }
            val acc = if (qTotal > 0) ((qCorrect * 100f) / qTotal).roundToInt() else 0
            val nCount = notes.count { it.subject.equals(subj, ignoreCase = true) }
            SubjectMasteryItem(
                subject = subj,
                averageAccuracy = acc,
                quizzesTaken = subjQuizzes.size,
                notesCount = nCount
            )
        }.sortedWith(
            compareByDescending<SubjectMasteryItem> { it.quizzesTaken > 0 }
                .thenByDescending { it.averageAccuracy }
                .thenByDescending { it.notesCount }
        ).take(6)

        return StudentProgressSummary(
            timeRange = timeRange,
            currentStreakDays = currentStreak,
            longestStreakDays = maxOf(longestStreak, currentStreak),
            studiedToday = studiedToday,
            totalActiveDays = allActiveDayStarts.size,
            totalNotesGenerated = totalNotes,
            notesInRange = filteredNotes.size,
            favoriteNotesCount = notes.count { it.isFavorite },
            savedQuestionsCount = favoriteQuestions.size,
            totalQuizzesCompleted = filteredQuizzes.size,
            totalQuestionsAttempted = totalQuestionsAttempted,
            averageAccuracyPercent = averageAccuracy,
            bestScorePercent = bestScore,
            accuracyDeltaPercent = accuracyDelta,
            quizTrendPoints = quizPoints,
            notesTrendBuckets = notesBuckets,
            heatmapDays = heatmapDays,
            categoryBreakdown = categorySlices,
            subjectMastery = subjectMastery,
            streakWidgetInfo = widgetInfo
        )
    }

    fun buildStreakWidgetInfo(
        currentStreakDays: Int,
        longestStreakDays: Int,
        studiedToday: Boolean,
        recentSevenDays: List<StreakDayCell>
    ): DailyStreakWidgetInfo {
        val nextGoal = when {
            currentStreakDays < 7 -> 7
            currentStreakDays < 14 -> 14
            currentStreakDays < 30 -> 30
            currentStreakDays < 60 -> 60
            currentStreakDays < 100 -> 100
            else -> ((currentStreakDays / 50) + 1) * 50
        }
        val progress = (currentStreakDays.toFloat() / nextGoal.toFloat()).coerceIn(0f, 1f)
        val remaining = (nextGoal - currentStreakDays).coerceAtLeast(0)
        val message = when {
            currentStreakDays == 0 -> "Start your study streak today — open notes or attempt a quick quiz!"
            remaining == 1 -> "Just 1 more day to reach your $nextGoal-day Study Champion milestone!"
            currentStreakDays >= 14 -> "Unstoppable consistency! $remaining days to your $nextGoal-day milestone."
            currentStreakDays >= 7 -> "Fantastic weekly streak! $remaining days left to reach $nextGoal days."
            studiedToday -> "Great focus today! $remaining days left to hit your $nextGoal-day goal."
            else -> "Keep your flame alive today — $remaining days to reach $nextGoal days!"
        }
        return DailyStreakWidgetInfo(
            currentStreakDays = currentStreakDays,
            longestStreakDays = longestStreakDays,
            nextGoalDays = nextGoal,
            progressFraction = progress,
            studiedToday = studiedToday,
            motivationalMessage = message,
            recentSevenDays = recentSevenDays
        )
    }

    private fun buildNotesBuckets(
        notes: List<StudyNoteEntity>,
        timeRange: ProgressTimeRange,
        todayStart: Long,
        bucketCount: Int,
        shortDateFmt: SimpleDateFormat,
        dayShortFmt: SimpleDateFormat,
        fullDateFmt: SimpleDateFormat
    ): List<NotesTrendBucket> {
        val windowDays = when (timeRange) {
            ProgressTimeRange.DAYS_7 -> 7
            ProgressTimeRange.DAYS_14 -> 14
            ProgressTimeRange.DAYS_30 -> 30
            ProgressTimeRange.ALL -> {
                val earliest = notes.minOfOrNull { startOfDayMillis(it.createdAt) } ?: todayStart
                (((todayStart - earliest) / DAY_MS).toInt() + 1).coerceIn(7, 60)
            }
        }

        val stepDays = when {
            windowDays <= 7 -> 1
            windowDays <= 14 -> 2
            else -> (windowDays / bucketCount).coerceAtLeast(2)
        }

        val startDay = todayStart - (windowDays - 1).toLong() * DAY_MS
        val buckets = mutableListOf<NotesTrendBucket>()
        var runningTotal = notes.count { startOfDayMillis(it.createdAt) < startDay }

        var cursor = startDay
        while (cursor <= todayStart) {
            val bucketEnd = (cursor + (stepDays - 1).toLong() * DAY_MS).coerceAtMost(todayStart)
            val count = notes.count { note ->
                val d = startOfDayMillis(note.createdAt)
                d in cursor..bucketEnd
            }
            runningTotal += count
            val date = Date(bucketEnd)
            val label = if (windowDays <= 7) dayShortFmt.format(date) else shortDateFmt.format(date)
            val fullLabel = if (cursor == bucketEnd) {
                fullDateFmt.format(date)
            } else {
                "${shortDateFmt.format(Date(cursor))} – ${shortDateFmt.format(date)}"
            }
            buckets += NotesTrendBucket(
                dayStartMillis = bucketEnd,
                label = label,
                fullDateLabel = fullLabel,
                count = count,
                cumulativeCount = runningTotal
            )
            cursor += stepDays.toLong() * DAY_MS
        }
        return buckets
    }

    private fun computeCurrentStreak(activeDays: Set<Long>, todayStart: Long): Int {
        if (activeDays.isEmpty()) return 0
        val yesterdayStart = todayStart - DAY_MS
        var cursor = when {
            todayStart in activeDays -> todayStart
            yesterdayStart in activeDays -> yesterdayStart
            else -> return 0
        }
        var streak = 0
        while (cursor in activeDays) {
            streak++
            cursor -= DAY_MS
        }
        return streak
    }

    private fun computeLongestStreak(activeDays: Set<Long>): Int {
        if (activeDays.isEmpty()) return 0
        val sorted = activeDays.sorted()
        var best = 1
        var current = 1
        for (i in 1 until sorted.size) {
            val diffDays = ((sorted[i] - sorted[i - 1]) + DAY_MS / 2) / DAY_MS
            if (diffDays == 1L) {
                current++
                if (current > best) best = current
            } else if (diffDays > 1L) {
                current = 1
            }
        }
        return best
    }

    fun startOfDayMillis(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
