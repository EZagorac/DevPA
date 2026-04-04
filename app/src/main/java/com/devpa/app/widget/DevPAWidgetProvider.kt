package com.devpa.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.devpa.app.R
import com.devpa.app.ui.MainActivity

/**
 * DevPAWidgetProvider — the core Android home screen widget.
 *
 * This uses RemoteViews (a special limited layout system for widgets).
 * Jetpack Compose does NOT work here — only XML layouts.
 *
 * The widget shows:
 *  - Best current habit streak + days tracked
 *  - Portfolio completion percentage
 *  - One-tap to open the app
 *
 * Data is refreshed by WidgetRefreshWorker (WorkManager) each morning.
 */
class DevPAWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update every widget instance on the home screen
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when first widget instance is added to home screen
        WidgetRefreshWorker.schedule(context)
    }

    override fun onDisabled(context: Context) {
        // Called when last widget instance is removed
        WidgetRefreshWorker.cancel(context)
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            streak: Int = 0,
            daysTracked: Int = 0,
            portfolioPct: Int = 0,
            habitsToday: Int = 0,
            totalHabits: Int = 0
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_dev_pa)

            // Populate widget views
            views.setTextViewText(R.id.widget_streak, "$streak")
            views.setTextViewText(R.id.widget_days_tracked, "${daysTracked}d tracked")
            views.setTextViewText(R.id.widget_portfolio_pct, "${portfolioPct}%")
            views.setTextViewText(R.id.widget_habits_today, "$habitsToday / $totalHabits done today")

            // Tap anywhere on widget to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
