package com.devpa.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object StreakCalculator {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    data class StreakResult(
        val currentStreak: Int,      // days in a row up to today
        val longestStreak: Int,      // personal best
        val daysSinceStart: Int,     // total days tracking this habit
        val completionRate: Float,   // percentage of tracked days completed
        val doneToday: Boolean
    )

    fun calculate(logDates: List<String>, startDate: String): StreakResult {
        val today = LocalDate.now()
        val start = LocalDate.parse(startDate, formatter)
        val daysSinceStart = ChronoUnit.DAYS.between(start, today).toInt() + 1

        val dates = logDates.map { LocalDate.parse(it, formatter) }.toSortedSet()
        val doneToday = dates.contains(today)

        // Current streak — count backwards from today
        var currentStreak = 0
        var checkDate = today
        while (dates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Longest streak — scan all dates
        var longestStreak = 0
        var runLength = 0
        var prevDate: LocalDate? = null
        for (date in dates) {
            runLength = if (prevDate != null && ChronoUnit.DAYS.between(prevDate, date) == 1L) {
                runLength + 1
            } else {
                1
            }
            longestStreak = maxOf(longestStreak, runLength)
            prevDate = date
        }

        val completionRate = if (daysSinceStart > 0) {
            (dates.size.toFloat() / daysSinceStart).coerceIn(0f, 1f)
        } else 0f

        return StreakResult(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            daysSinceStart = daysSinceStart,
            completionRate = completionRate,
            doneToday = doneToday
        )
    }

    fun todayString(): String = LocalDate.now().format(formatter)

    // Generate last N day strings for the dot row in the UI
    fun lastNDays(n: Int): List<Pair<String, Boolean>> {
        val today = LocalDate.now()
        return (n - 1 downTo 0).map { i ->
            val date = today.minusDays(i.toLong())
            date.format(formatter) to (i == 0)  // Pair<dateString, isToday>
        }
    }
}
