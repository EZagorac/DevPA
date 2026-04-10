package com.devpa.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.R
import com.devpa.app.data.db.DatabaseProvider
import com.devpa.app.data.db.HabitEntity
import com.devpa.app.databinding.ActivityWidgetConfigureBinding
import com.devpa.app.databinding.ItemWidgetHabitSelectBinding
import com.devpa.app.util.StreakCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HabitWidgetConfigureActivity : AppCompatActivity() {

    private var _binding: ActivityWidgetConfigureBinding? = null
    private val binding get() = _binding!!

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Required by Android: set RESULT_CANCELED in case user backs out without configuring
        setResult(RESULT_CANCELED)

        _binding = ActivityWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerHabits.layoutManager = LinearLayoutManager(this)
        binding.btnCancel.setOnClickListener { finish() }

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = DatabaseProvider.getInstance(applicationContext).habitDao()
            val habits = dao.getAllHabitsSync()
            val items = habits.map { habit ->
                val logDates = dao.getLogDatesForHabit(habit.id)
                val streak = StreakCalculator.calculate(
                    logDates = logDates,
                    startDate = habit.startDate,
                    scheduleType = habit.scheduleType,
                    scheduleDays = habit.scheduleDays,
                    timesPerWeek = habit.timesPerWeek,
                    breakUntil = habit.breakUntil,
                    breakStartStreak = habit.breakStartStreak
                )
                Pair(habit, streak)
            }

            withContext(Dispatchers.Main) {
                binding.recyclerHabits.adapter = HabitSelectAdapter(items) { (habit, streak) ->
                    onHabitSelected(habit.id, streak.currentStreak)
                }
            }
        }
    }

    private fun onHabitSelected(habitId: Long, currentStreak: Int) {
        getSharedPreferences(DevPAWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong("${DevPAWidgetProvider.PREF_HABIT_ID_PREFIX}$appWidgetId", habitId)
            .apply()

        val widgetManager = AppWidgetManager.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            DevPAWidgetProvider.refreshWidget(applicationContext, widgetManager, appWidgetId, habitId)
            withContext(Dispatchers.Main) {
                val resultIntent = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

// ── Adapter ────────────────────────────────────────────────────────
private class HabitSelectAdapter(
    private val items: List<Pair<HabitEntity, StreakCalculator.StreakResult>>,
    private val onSelect: (Pair<HabitEntity, StreakCalculator.StreakResult>) -> Unit
) : RecyclerView.Adapter<HabitSelectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWidgetHabitSelectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWidgetHabitSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (habit, streak) = items[position]
        with(holder.binding) {
            tvHabitName.text = habit.name
            tvSchedule.text = when (habit.scheduleType) {
                "days_of_week"   -> "Specific days"
                "times_per_week" -> "${habit.timesPerWeek}× per week"
                else             -> "Every day"
            }
            tvStreak.text = "${streak.currentStreak}"
            root.setOnClickListener { onSelect(items[position]) }
        }
    }
}
