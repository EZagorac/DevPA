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

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val manager = AppWidgetManager.getInstance(applicationContext)

                // ── Habit widgets ──────────────────────────────────
                val habitWidgetIds = manager.getAppWidgetIds(
                    ComponentName(applicationContext, DevPAWidgetProvider::class.java)
                )
                val habitPrefs = applicationContext.getSharedPreferences(
                    DevPAWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE
                )
                for (widgetId in habitWidgetIds) {
                    val habitId = habitPrefs.getLong(
                        "${DevPAWidgetProvider.PREF_HABIT_ID_PREFIX}$widgetId", -1L
                    )
                    if (habitId == -1L) continue
                    DevPAWidgetProvider.refreshWidget(applicationContext, manager, widgetId, habitId)
                }

                // ── Journey widgets ────────────────────────────────
                val journeyWidgetIds = manager.getAppWidgetIds(
                    ComponentName(applicationContext, JourneyWidgetProvider::class.java)
                )
                val journeyPrefs = applicationContext.getSharedPreferences(
                    JourneyWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE
                )
                for (widgetId in journeyWidgetIds) {
                    val journeyId = journeyPrefs.getLong(
                        "${JourneyWidgetProvider.PREF_JOURNEY_ID_PREFIX}$widgetId", -1L
                    )
                    if (journeyId == -1L) continue
                    JourneyWidgetProvider.refreshWidget(applicationContext, manager, widgetId, journeyId)
                }

                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "devpa_widget_refresh"

        fun schedule(context: Context) {
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
