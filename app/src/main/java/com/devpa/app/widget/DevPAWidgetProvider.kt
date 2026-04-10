package com.devpa.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.devpa.app.R
import com.devpa.app.data.db.DatabaseProvider
import com.devpa.app.data.db.HabitEntity
import com.devpa.app.data.db.HabitLogEntity
import com.devpa.app.util.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DevPAWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for (appWidgetId in appWidgetIds) {
            val habitId = prefs.getLong("$PREF_HABIT_ID_PREFIX$appWidgetId", -1L)
            if (habitId == -1L) continue  // not yet configured
            GlobalScope.launch(Dispatchers.IO) {
                refreshWidget(context, appWidgetManager, appWidgetId, habitId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_HABIT) {
            val appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val habitId = prefs.getLong("$PREF_HABIT_ID_PREFIX$appWidgetId", -1L)
            if (habitId == -1L) return

            GlobalScope.launch(Dispatchers.IO) {
                val dao = DatabaseProvider.getInstance(context).habitDao()
                val today = StreakCalculator.todayString()
                val isDone = dao.isCompletedOn(habitId, today) > 0
                if (isDone) {
                    dao.deleteLog(habitId, today)
                } else {
                    dao.insertLog(HabitLogEntity(habitId = habitId, date = today))
                }
                val manager = AppWidgetManager.getInstance(context)
                refreshWidget(context, manager, appWidgetId, habitId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        for (appWidgetId in appWidgetIds) {
            editor.remove("$PREF_HABIT_ID_PREFIX$appWidgetId")
        }
        editor.apply()
    }

    override fun onEnabled(context: Context) {
        WidgetRefreshWorker.schedule(context)
    }

    override fun onDisabled(context: Context) {
        WidgetRefreshWorker.cancel(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        // Re-render when user resizes the widget so small/medium layout switches correctly
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val habitId = prefs.getLong("$PREF_HABIT_ID_PREFIX$appWidgetId", -1L)
        if (habitId == -1L) return
        GlobalScope.launch(Dispatchers.IO) {
            refreshWidget(context, appWidgetManager, appWidgetId, habitId)
        }
    }

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.devpa.app.ACTION_TOGGLE_HABIT"
        const val EXTRA_WIDGET_ID = "extra_widget_id"
        const val PREFS_NAME = "devpa_widget_prefs"
        const val PREF_HABIT_ID_PREFIX = "habit_id_"

        suspend fun refreshWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            habitId: Long
        ) {
            val dao = DatabaseProvider.getInstance(context).habitDao()
            val habit = dao.getHabitById(habitId) ?: return
            val logDates = dao.getLogDatesForHabit(habitId)
            val streak = StreakCalculator.calculate(
                logDates = logDates,
                startDate = habit.startDate,
                scheduleType = habit.scheduleType,
                scheduleDays = habit.scheduleDays,
                timesPerWeek = habit.timesPerWeek,
                breakUntil = habit.breakUntil,
                breakStartStreak = habit.breakStartStreak
            )

            // Use current displayed height from options to select layout.
            // getAppWidgetOptions gives actual size after user resizes; falls back to info minHeight.
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val currentHeightDp = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                appWidgetManager.getAppWidgetInfo(appWidgetId)?.minHeight ?: 50
            )
            val useSmall = currentHeightDp < 110

            val views = if (useSmall) {
                buildSmallViews(context, habit, streak, logDates.toSet())
            } else {
                buildMediumViews(context, habit, streak, logDates.toSet(), appWidgetId)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun buildSmallViews(
            context: Context,
            habit: HabitEntity,
            streak: StreakCalculator.StreakResult,
            logDateSet: Set<String>
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_devpa_small)
            views.setTextViewText(R.id.widget_habit_name, habit.name)
            views.setTextViewText(R.id.widget_streak, "${streak.currentStreak}")

            val streakColor = if (streak.isOnBreak) Color.parseColor("#FFC04A")
                             else Color.parseColor("#7FFF6E")
            views.setTextColor(R.id.widget_streak, streakColor)
            views.setTextViewText(
                R.id.widget_streak_label,
                if (streak.isOnBreak) "on break" else "day streak"
            )

            val today = StreakCalculator.todayString()
            val doneToday = logDateSet.contains(today)
            val dotColor = when {
                streak.isOnBreak -> Color.parseColor("#4A5568")
                doneToday        -> Color.parseColor("#7FFF6E")
                else             -> Color.parseColor("#FFC04A")
            }
            views.setInt(R.id.widget_today_dot, "setBackgroundColor", dotColor)

            return views
        }

        private fun buildMediumViews(
            context: Context,
            habit: HabitEntity,
            streak: StreakCalculator.StreakResult,
            logDateSet: Set<String>,
            appWidgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_devpa_medium)
            views.setTextViewText(R.id.widget_habit_name, habit.name)

            val streakColor = if (streak.isOnBreak) Color.parseColor("#FFC04A")
                             else Color.parseColor("#7FFF6E")
            views.setTextColor(R.id.widget_streak_val, streakColor)
            views.setTextViewText(R.id.widget_streak_val, "${streak.currentStreak}")
            views.setTextViewText(R.id.widget_days_val, "${streak.daysSinceStart}")
            views.setTextViewText(R.id.widget_best_val, "${streak.longestStreak}")

            // Dot row — 7 dots for the last 7 days, colors set programmatically
            val dotIds = listOf(
                R.id.widget_dot_0, R.id.widget_dot_1, R.id.widget_dot_2,
                R.id.widget_dot_3, R.id.widget_dot_4, R.id.widget_dot_5, R.id.widget_dot_6
            )
            val last7 = StreakCalculator.lastNDays(7, habit.scheduleType, habit.scheduleDays)
            val breakUntilDate = habit.breakUntil?.let { LocalDate.parse(it) }

            for ((i, triple) in last7.withIndex()) {
                val (date, isToday, isScheduled) = triple
                if (!isScheduled) {
                    views.setViewVisibility(dotIds[i], View.GONE)
                    continue
                }
                views.setViewVisibility(dotIds[i], View.VISIBLE)
                val dateLocal = LocalDate.parse(date)
                val isDone = logDateSet.contains(date)
                val isBreakDay = breakUntilDate != null && !dateLocal.isAfter(breakUntilDate)
                val color = when {
                    isBreakDay       -> Color.parseColor("#4A5568")
                    isToday && isDone -> Color.parseColor("#FFC04A")
                    isToday          -> Color.parseColor("#3FFFFFFF")
                    isDone           -> Color.parseColor("#7FFF6E")
                    else             -> Color.parseColor("#1E2026")
                }
                views.setInt(dotIds[i], "setBackgroundColor", color)
            }

            // Toggle button
            val today = StreakCalculator.todayString()
            val doneToday = logDateSet.contains(today)
            when {
                streak.isOnBreak -> {
                    val daysLeft = if (habit.breakUntil != null) {
                        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(habit.breakUntil)).toInt() + 1
                    } else 0
                    views.setTextViewText(R.id.widget_toggle_btn, "On break — $daysLeft days left")
                    views.setTextColor(R.id.widget_toggle_btn, Color.parseColor("#888A8F"))
                    views.setInt(R.id.widget_toggle_btn, "setBackgroundColor", Color.TRANSPARENT)
                    views.setBoolean(R.id.widget_toggle_btn, "setClickable", false)
                }
                doneToday -> {
                    views.setTextViewText(R.id.widget_toggle_btn, "✓ Done today")
                    views.setTextColor(R.id.widget_toggle_btn, Color.parseColor("#4FD1C5"))
                    views.setInt(R.id.widget_toggle_btn, "setBackgroundColor", Color.parseColor("#1A7FFF6E"))
                    views.setBoolean(R.id.widget_toggle_btn, "setClickable", true)
                    views.setOnClickPendingIntent(R.id.widget_toggle_btn, buildTogglePi(context, appWidgetId))
                }
                else -> {
                    views.setTextViewText(R.id.widget_toggle_btn, "Mark done today")
                    views.setTextColor(R.id.widget_toggle_btn, Color.parseColor("#7FFF6E"))
                    views.setInt(R.id.widget_toggle_btn, "setBackgroundColor", Color.parseColor("#1A7FFF6E"))
                    views.setBoolean(R.id.widget_toggle_btn, "setClickable", true)
                    views.setOnClickPendingIntent(R.id.widget_toggle_btn, buildTogglePi(context, appWidgetId))
                }
            }

            return views
        }

        private fun buildTogglePi(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, DevPAWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_HABIT
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            return PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
