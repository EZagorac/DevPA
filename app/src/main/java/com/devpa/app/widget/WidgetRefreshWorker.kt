package com.devpa.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.*
import com.devpa.app.data.db.DatabaseProvider
import com.devpa.app.util.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WidgetRefreshWorker — runs every morning via WorkManager to update
 * the home screen widget with fresh streak and portfolio data.
 *
 * WorkManager survives app restarts and device reboots (once rescheduled
 * by BootReceiver).
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = DatabaseProvider.getInstance(applicationContext)
                val today = StreakCalculator.todayString()

                // Get all habits and calculate best streak
                val habitsWithStatus = db.habitDao().getHabitsWithTodayStatus(today)
                var bestStreak = 0
                var daysTracked = 0
                var habitsToday = 0

                for (h in habitsWithStatus) {
                    val logs = db.habitDao().getLogDatesForHabit(h.id)
                    val streak = StreakCalculator.calculate(logs, h.startDate)
                    if (streak.currentStreak > bestStreak) {
                        bestStreak = streak.currentStreak
                        daysTracked = streak.daysSinceStart
                    }
                    if (h.logCount > 0) habitsToday++
                }

                // Portfolio completion
                val done = db.portfolioDao().getCompletedCount()
                val total = db.portfolioDao().getTotalCount()
                val pct = if (total > 0) (done * 100 / total) else 0

                // Push to all widget instances
                val manager = AppWidgetManager.getInstance(applicationContext)
                val ids = manager.getAppWidgetIds(
                    ComponentName(applicationContext, DevPAWidgetProvider::class.java)
                )
                for (id in ids) {
                    DevPAWidgetProvider.updateWidget(
                        applicationContext, manager, id,
                        streak = bestStreak,
                        daysTracked = daysTracked,
                        portfolioPct = pct,
                        habitsToday = habitsToday,
                        totalHabits = habitsWithStatus.size
                    )
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "widget_refresh"

        fun schedule(context: Context) {
            // Run once daily at ~8am
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateDelayUntil8am(), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun calculateDelayUntil8am(): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 8)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}
