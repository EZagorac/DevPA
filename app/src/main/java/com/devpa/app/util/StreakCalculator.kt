package com.devpa.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

object StreakCalculator {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoWeek = WeekFields.ISO

    data class StreakResult(
        val currentStreak: Int,
        val longestStreak: Int,
        val daysSinceStart: Int,
        val completionRate: Float,
        val doneToday: Boolean,
        val isOnBreak: Boolean = false
    )

    fun calculate(
        logDates: List<String>,
        startDate: String,
        scheduleType: String = "daily",
        scheduleDays: String = "",
        timesPerWeek: Int = 0,
        breakUntil: String? = null,
        breakStartStreak: Int = 0
    ): StreakResult {
        val today = LocalDate.now()
        val start = LocalDate.parse(startDate, formatter)
        val daysSinceStart = ChronoUnit.DAYS.between(start, today).toInt().coerceAtLeast(0) + 1

        val breakUntilDate = breakUntil?.let { LocalDate.parse(it, formatter) }

        // On break — return frozen streak immediately
        if (breakUntilDate != null && !today.isAfter(breakUntilDate)) {
            return StreakResult(
                currentStreak = breakStartStreak,
                longestStreak = breakStartStreak,
                daysSinceStart = daysSinceStart,
                completionRate = 0f,
                doneToday = false,
                isOnBreak = true
            )
        }

        val dates = logDates.map { LocalDate.parse(it, formatter) }.toSortedSet()
        val doneToday = dates.contains(today)
        val completionRate = if (daysSinceStart > 0) {
            (dates.size.toFloat() / daysSinceStart).coerceIn(0f, 1f)
        } else 0f

        return when (scheduleType) {
            "days_of_week" -> calculateDaysOfWeek(
                dates, today, daysSinceStart, completionRate, doneToday,
                parseDaySet(scheduleDays), breakUntilDate, breakStartStreak
            )
            "times_per_week" -> calculateTimesPerWeek(
                dates, today, daysSinceStart, completionRate, doneToday,
                timesPerWeek.coerceAtLeast(1), breakUntilDate, breakStartStreak
            )
            else -> calculateDaily(
                dates, today, daysSinceStart, completionRate, doneToday,
                breakUntilDate, breakStartStreak
            )
        }
    }

    private fun calculateDaily(
        dates: Set<LocalDate>,
        today: LocalDate,
        daysSinceStart: Int,
        completionRate: Float,
        doneToday: Boolean,
        breakUntilDate: LocalDate?,
        breakStartStreak: Int
    ): StreakResult {
        var currentStreak = 0
        var checkDate = today
        loop@ while (true) {
            when {
                dates.contains(checkDate) -> {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                }
                breakUntilDate != null && !checkDate.isAfter(breakUntilDate) -> {
                    // Hit the break period — bridge over it
                    currentStreak += breakStartStreak
                    break@loop
                }
                else -> break@loop
            }
        }

        // Longest streak scan over all log dates
        var longestStreak = currentStreak
        var runLength = 0
        var prevDate: LocalDate? = null
        for (date in dates.sorted()) {
            runLength = if (prevDate != null && ChronoUnit.DAYS.between(prevDate, date) == 1L) {
                runLength + 1
            } else 1
            longestStreak = maxOf(longestStreak, runLength)
            prevDate = date
        }

        return StreakResult(currentStreak, longestStreak, daysSinceStart, completionRate, doneToday)
    }

    private fun calculateDaysOfWeek(
        dates: Set<LocalDate>,
        today: LocalDate,
        daysSinceStart: Int,
        completionRate: Float,
        doneToday: Boolean,
        scheduledDays: Set<DayOfWeek>,
        breakUntilDate: LocalDate?,
        breakStartStreak: Int
    ): StreakResult {
        // Walk backwards: skip non-scheduled days, break on missed scheduled day
        var currentStreak = 0
        var checkDate = today
        loop@ while (true) {
            when {
                breakUntilDate != null && !checkDate.isAfter(breakUntilDate) -> {
                    currentStreak += breakStartStreak
                    break@loop
                }
                !scheduledDays.contains(checkDate.dayOfWeek) -> {
                    checkDate = checkDate.minusDays(1)
                }
                dates.contains(checkDate) -> {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                }
                else -> break@loop  // missed a scheduled day
            }
        }

        // Longest streak over scheduled log dates
        var longestStreak = currentStreak
        var runLength = 0
        var prevScheduledDate: LocalDate? = null
        for (date in dates.sorted()) {
            if (!scheduledDays.contains(date.dayOfWeek)) continue
            runLength = if (prevScheduledDate != null &&
                countMissedScheduledDays(prevScheduledDate, date, scheduledDays) == 0
            ) {
                runLength + 1
            } else 1
            longestStreak = maxOf(longestStreak, runLength)
            prevScheduledDate = date
        }

        return StreakResult(currentStreak, longestStreak, daysSinceStart, completionRate, doneToday)
    }

    private fun countMissedScheduledDays(
        from: LocalDate, to: LocalDate, scheduledDays: Set<DayOfWeek>
    ): Int {
        var count = 0
        var d = from.plusDays(1)
        while (d.isBefore(to)) {
            if (scheduledDays.contains(d.dayOfWeek)) count++
            d = d.plusDays(1)
        }
        return count
    }

    private fun calculateTimesPerWeek(
        dates: Set<LocalDate>,
        today: LocalDate,
        daysSinceStart: Int,
        completionRate: Float,
        doneToday: Boolean,
        timesPerWeek: Int,
        breakUntilDate: LocalDate?,
        breakStartStreak: Int
    ): StreakResult {
        val weekCounts: Map<Pair<Int, Int>, Int> = dates.groupBy { date ->
            date.get(isoWeek.weekBasedYear()) to date.get(isoWeek.weekOfWeekBasedYear())
        }.mapValues { it.value.size }

        val todayKey = today.get(isoWeek.weekBasedYear()) to today.get(isoWeek.weekOfWeekBasedYear())

        // Walk backwards week-by-week
        var currentStreak = 0
        var weekMonday = today.with(DayOfWeek.MONDAY)
        loop@ while (true) {
            val key = weekMonday.get(isoWeek.weekBasedYear()) to weekMonday.get(isoWeek.weekOfWeekBasedYear())

            if (breakUntilDate != null && !weekMonday.isAfter(breakUntilDate)) {
                currentStreak += breakStartStreak
                break@loop
            }

            val count = weekCounts[key] ?: 0
            val isCurrentWeek = key == todayKey
            if (isCurrentWeek || count >= timesPerWeek) {
                currentStreak++
                weekMonday = weekMonday.minusWeeks(1)
            } else {
                break@loop
            }
        }

        // Longest streak in consecutive qualifying weeks
        val sortedWeekKeys = weekCounts.keys.sortedWith(compareBy({ it.first }, { it.second }))
        var longestStreak = currentStreak
        var runLen = 0
        var prevMonday: LocalDate? = null
        for (key in sortedWeekKeys) {
            if ((weekCounts[key] ?: 0) < timesPerWeek) {
                runLen = 0; prevMonday = null; continue
            }
            val monday = weekKeyToMonday(key)
            runLen = if (prevMonday != null && ChronoUnit.WEEKS.between(prevMonday, monday) == 1L) {
                runLen + 1
            } else 1
            longestStreak = maxOf(longestStreak, runLen)
            prevMonday = monday
        }

        return StreakResult(currentStreak, longestStreak, daysSinceStart, completionRate, doneToday)
    }

    private fun weekKeyToMonday(key: Pair<Int, Int>): LocalDate =
        LocalDate.now()
            .with(isoWeek.weekBasedYear(), key.first.toLong())
            .with(isoWeek.weekOfWeekBasedYear(), key.second.toLong())
            .with(DayOfWeek.MONDAY)

    private fun parseDaySet(scheduleDays: String): Set<DayOfWeek> {
        if (scheduleDays.isBlank()) return emptySet()
        return scheduleDays.split(",").mapNotNull { it.trim().toIntOrNull()?.let { n -> DayOfWeek.of(n) } }.toSet()
    }

    /**
     * Returns true if the habit should be completed today based on its schedule.
     * Used to decide whether to show the "streak at risk" warning.
     */
    fun isScheduledToday(scheduleType: String, scheduleDays: String): Boolean = when (scheduleType) {
        "days_of_week" -> parseDaySet(scheduleDays).contains(LocalDate.now().dayOfWeek)
        else -> true
    }

    fun todayString(): String = LocalDate.now().format(formatter)

    /**
     * Returns a list of the last [n] days as triples:
     *   (dateString, isToday, isScheduledDay)
     * Non-scheduled days have isScheduledDay = false and are skipped in the dot row.
     */
    fun lastNDays(
        n: Int,
        scheduleType: String = "daily",
        scheduleDays: String = ""
    ): List<Triple<String, Boolean, Boolean>> {
        val today = LocalDate.now()
        val scheduledDaySet = if (scheduleType == "days_of_week") parseDaySet(scheduleDays) else emptySet()
        return (n - 1 downTo 0).map { i ->
            val date = today.minusDays(i.toLong())
            val isScheduled = when (scheduleType) {
                "days_of_week" -> scheduledDaySet.contains(date.dayOfWeek)
                else -> true
            }
            Triple(date.format(formatter), i == 0, isScheduled)
        }
    }
}
