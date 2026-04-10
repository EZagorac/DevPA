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
import com.devpa.app.data.db.JourneyEntity
import com.devpa.app.data.db.JourneyStepEntity
import com.devpa.app.util.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class JourneyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for (widgetId in appWidgetIds) {
            val journeyId = prefs.getLong("$PREF_JOURNEY_ID_PREFIX$widgetId", -1L)
            if (journeyId == -1L) continue
            GlobalScope.launch(Dispatchers.IO) {
                refreshWidget(context, appWidgetManager, widgetId, journeyId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_COMPLETE_STEP) {
            val widgetId = intent.getIntExtra(EXTRA_JOURNEY_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val stepId = intent.getLongExtra(EXTRA_STEP_ID, -1L)
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID || stepId == -1L) return

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val journeyId = prefs.getLong("$PREF_JOURNEY_ID_PREFIX$widgetId", -1L)
            if (journeyId == -1L) return

            GlobalScope.launch(Dispatchers.IO) {
                val db = DatabaseProvider.getInstance(context)
                val stepDao = db.journeyStepDao()
                val journeyDao = db.journeyDao()

                val step = stepDao.getNextThreeSteps(journeyId).firstOrNull { it.id == stepId }
                    ?: stepDao.getStepsForJourneyOnce(journeyId).firstOrNull { it.id == stepId }
                    ?: return@launch

                stepDao.updateProgress(step.id, 100, true, System.currentTimeMillis())

                // Check if journey is now complete
                val total = stepDao.getTotalStepCount(journeyId)
                val completed = stepDao.getCompletedStepCount(journeyId)
                if (total > 0 && total == completed) {
                    journeyDao.updateStatus(journeyId, "COMPLETED", System.currentTimeMillis())
                }

                val manager = AppWidgetManager.getInstance(context)
                refreshWidget(context, manager, widgetId, journeyId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) editor.remove("$PREF_JOURNEY_ID_PREFIX$id")
        editor.apply()
    }

    override fun onEnabled(context: Context) {
        WidgetRefreshWorker.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val journeyId = prefs.getLong("$PREF_JOURNEY_ID_PREFIX$appWidgetId", -1L)
        if (journeyId == -1L) return
        GlobalScope.launch(Dispatchers.IO) {
            refreshWidget(context, appWidgetManager, appWidgetId, journeyId)
        }
    }

    companion object {
        const val ACTION_COMPLETE_STEP = "com.devpa.app.ACTION_COMPLETE_STEP"
        const val EXTRA_JOURNEY_WIDGET_ID = "extra_journey_widget_id"
        const val EXTRA_STEP_ID = "extra_step_id"
        const val PREFS_NAME = "devpa_journey_widget_prefs"
        const val PREF_JOURNEY_ID_PREFIX = "journey_id_"

        suspend fun refreshWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            journeyId: Long
        ) {
            val db = DatabaseProvider.getInstance(context)
            val journey = db.journeyDao().getJourneyById(journeyId) ?: return
            val steps = db.journeyStepDao().getNextThreeSteps(journeyId)
            val total = db.journeyStepDao().getTotalStepCount(journeyId)
            val completed = db.journeyStepDao().getCompletedStepCount(journeyId)
            val sumPct = db.journeyStepDao().getSumProgressPct(journeyId) ?: 0
            val progressPct = if (total == 0) 0 else sumPct / total

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val heightDp = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                appWidgetManager.getAppWidgetInfo(appWidgetId)?.minHeight ?: 50
            )

            val views = when {
                heightDp < 80 -> buildSmallViews(context, journey, progressPct, completed, total)
                heightDp < 160 -> buildMediumViews(context, journey, progressPct, completed, total, steps, appWidgetId)
                else -> buildLargeViews(context, journey, progressPct, completed, total, steps, appWidgetId)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun buildSmallViews(
            context: Context,
            journey: JourneyEntity,
            progressPct: Int,
            completed: Int,
            total: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_journey_small)
            views.setTextViewText(R.id.widget_journey_name, "${journey.iconEmoji} ${journey.name}")
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPct, false)
            views.setTextViewText(R.id.widget_progress_label, "$completed / $total · $progressPct%")
            return views
        }

        private fun buildMediumViews(
            context: Context,
            journey: JourneyEntity,
            progressPct: Int,
            completed: Int,
            total: Int,
            steps: List<JourneyStepEntity>,
            widgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_journey_medium)
            views.setTextViewText(R.id.widget_journey_name, "${journey.iconEmoji} ${journey.name}")
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPct, false)
            views.setTextViewText(R.id.widget_progress_label, "$completed / $total · $progressPct%")
            bindStepRow(context, views, steps.getOrNull(0), R.id.widget_step_row_0, widgetId)
            return views
        }

        private fun buildLargeViews(
            context: Context,
            journey: JourneyEntity,
            progressPct: Int,
            completed: Int,
            total: Int,
            steps: List<JourneyStepEntity>,
            widgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_journey_large)
            views.setTextViewText(R.id.widget_journey_name, "${journey.iconEmoji} ${journey.name}")
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPct, false)
            views.setTextViewText(R.id.widget_progress_label, "$completed / $total · $progressPct%")
            bindStepRow(context, views, steps.getOrNull(0), R.id.widget_step_row_0, widgetId)
            bindStepRow(context, views, steps.getOrNull(1), R.id.widget_step_row_1, widgetId)
            bindStepRow(context, views, steps.getOrNull(2), R.id.widget_step_row_2, widgetId)
            return views
        }

        private fun bindStepRow(
            context: Context,
            views: RemoteViews,
            step: JourneyStepEntity?,
            rowId: Int,
            widgetId: Int
        ) {
            if (step == null) {
                views.setViewVisibility(rowId, View.INVISIBLE)
                return
            }
            views.setViewVisibility(rowId, View.VISIBLE)

            // Each row has sub-views: btn_complete_step, tv_step_label, tv_step_pct
            val completeBtnId = when (rowId) {
                R.id.widget_step_row_0 -> R.id.widget_step_check_0
                R.id.widget_step_row_1 -> R.id.widget_step_check_1
                else -> R.id.widget_step_check_2
            }
            val labelId = when (rowId) {
                R.id.widget_step_row_0 -> R.id.widget_step_label_0
                R.id.widget_step_row_1 -> R.id.widget_step_label_1
                else -> R.id.widget_step_label_2
            }
            val pctId = when (rowId) {
                R.id.widget_step_row_0 -> R.id.widget_step_pct_0
                R.id.widget_step_row_1 -> R.id.widget_step_pct_1
                else -> R.id.widget_step_pct_2
            }

            views.setTextViewText(labelId, step.label)
            views.setTextViewText(pctId, "${step.progressPct}%")

            if (step.isDone) {
                views.setInt(completeBtnId, "setBackgroundColor", Color.parseColor("#4FD1C5"))
                views.setBoolean(completeBtnId, "setClickable", false)
            } else {
                views.setInt(completeBtnId, "setBackgroundColor", Color.parseColor("#2A3A2A"))
                views.setOnClickPendingIntent(completeBtnId, buildCompleteStepPi(context, widgetId, step.id))
            }

            // Long-press on row opens app to step detail
            views.setOnClickPendingIntent(rowId, buildOpenAppPi(context, step.id))
        }

        private fun buildCompleteStepPi(context: Context, widgetId: Int, stepId: Long): PendingIntent {
            val intent = Intent(context, JourneyWidgetProvider::class.java).apply {
                action = ACTION_COMPLETE_STEP
                putExtra(EXTRA_JOURNEY_WIDGET_ID, widgetId)
                putExtra(EXTRA_STEP_ID, stepId)
            }
            return PendingIntent.getBroadcast(
                context, (widgetId * 1000 + stepId).toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun buildOpenAppPi(context: Context, stepId: Long): PendingIntent {
            val intent = Intent(context, com.devpa.app.ui.MainActivity::class.java).apply {
                putExtra("open_step_id", stepId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context, stepId.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
