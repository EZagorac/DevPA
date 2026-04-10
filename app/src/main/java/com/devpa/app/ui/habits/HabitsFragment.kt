package com.devpa.app.ui.habits

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.data.db.HabitDao
import com.devpa.app.data.db.HabitEntity
import com.devpa.app.data.db.HabitLogEntity
import com.devpa.app.databinding.FragmentHabitsBinding
import com.devpa.app.databinding.ItemHabitBinding
import com.devpa.app.util.StreakCalculator
import com.devpa.app.widget.DevPAWidgetProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

// ── Dot state ──────────────────────────────────────────────────────
enum class DotState { DONE_SCHEDULED, DONE_TODAY, TODAY_NOT_DONE, BREAK, MISSED }

// ── Data model for the UI ──────────────────────────────────────────
data class HabitUiState(
    val habit: HabitEntity,
    val streak: StreakCalculator.StreakResult,
    val last14Days: List<Triple<String, Boolean, Boolean>>,  // (dateString, isToday, isScheduled)
    val logDateSet: Set<String>
)

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class HabitsViewModel @Inject constructor(
    application: Application,
    private val habitDao: HabitDao
) : AndroidViewModel(application) {

    private val _habits = MutableStateFlow<List<HabitUiState>>(emptyList())
    val habits: StateFlow<List<HabitUiState>> = _habits

    private val _lastUncheckedId = MutableStateFlow<Long?>(null)
    val lastUncheckedId: StateFlow<Long?> = _lastUncheckedId

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            combine(
                habitDao.getAllHabits(),
                habitDao.getAllLogs()
            ) { habits, allLogs ->
                val logsByHabit = allLogs.groupBy { it.habitId }.mapValues { e -> e.value.map { it.date } }
                habits.map { habit ->
                    val logDates = logsByHabit[habit.id] ?: emptyList()
                    val streak = StreakCalculator.calculate(
                        logDates = logDates,
                        startDate = habit.startDate,
                        scheduleType = habit.scheduleType,
                        scheduleDays = habit.scheduleDays,
                        timesPerWeek = habit.timesPerWeek,
                        breakUntil = habit.breakUntil,
                        breakStartStreak = habit.breakStartStreak
                    )
                    val last14 = StreakCalculator.lastNDays(14, habit.scheduleType, habit.scheduleDays)
                    HabitUiState(habit, streak, last14, logDates.toSet())
                }
            }.collect { uiStates ->
                _habits.value = uiStates
            }
        }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val today = StreakCalculator.todayString()
            val isDone = habitDao.isCompletedOn(habitId, today) > 0
            if (isDone) {
                habitDao.deleteLog(habitId, today)
                _lastUncheckedId.value = habitId
            } else {
                habitDao.insertLog(HabitLogEntity(habitId = habitId, date = today))
                _lastUncheckedId.value = null
            }
            refreshWidgetsForHabit(habitId)
        }
    }

    private suspend fun refreshWidgetsForHabit(habitId: Long) {
        val app = getApplication<Application>()
        val widgetManager = AppWidgetManager.getInstance(app)
        val widgetIds = widgetManager.getAppWidgetIds(
            ComponentName(app, DevPAWidgetProvider::class.java)
        )
        val prefs = app.getSharedPreferences(DevPAWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
        for (widgetId in widgetIds) {
            val storedHabitId = prefs.getLong(
                "${DevPAWidgetProvider.PREF_HABIT_ID_PREFIX}$widgetId", -1L
            )
            if (storedHabitId == habitId) {
                withContext(Dispatchers.IO) {
                    DevPAWidgetProvider.refreshWidget(app, widgetManager, widgetId, habitId)
                }
            }
        }
    }

    fun addHabit(name: String, scheduleType: String, scheduleDays: String, timesPerWeek: Int) {
        viewModelScope.launch {
            habitDao.insertHabit(
                HabitEntity(
                    name = name,
                    startDate = StreakCalculator.todayString(),
                    scheduleType = scheduleType,
                    scheduleDays = scheduleDays,
                    timesPerWeek = timesPerWeek
                )
            )
        }
    }

    fun updateHabitName(id: Long, name: String) {
        viewModelScope.launch { habitDao.updateHabitName(id, name) }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch { habitDao.deleteHabit(habit) }
    }

    fun activateBreak(habitId: Long, breakUntil: String, currentStreak: Int) {
        viewModelScope.launch { habitDao.updateBreak(habitId, breakUntil, currentStreak) }
    }

    fun endBreak(habitId: Long) {
        viewModelScope.launch { habitDao.updateBreak(habitId, null, 0) }
    }

    fun clearLastUnchecked() {
        _lastUncheckedId.value = null
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HabitsAdapter(
            onToggle = { viewModel.toggleHabit(it) },
            onNameUpdated = { id, name -> viewModel.updateHabitName(id, name) },
            onDelete = { viewModel.deleteHabit(it) },
            onTakeBreak = { habitId, currentStreak ->
                BreakPickerBottomSheet.newInstance { dateString ->
                    viewModel.activateBreak(habitId, dateString, currentStreak)
                }.show(childFragmentManager, "break_picker")
            },
            onEndBreak = { viewModel.endBreak(it) }
        )

        binding.recyclerHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHabits.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.habits.collect { habits ->
                adapter.submitList(habits)

                val empty = habits.isEmpty()
                binding.recyclerHabits.visibility = if (empty) View.GONE else View.VISIBLE
                binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE

                val bestStreak = habits.maxOfOrNull { it.streak.longestStreak } ?: 0
                val doneToday = habits.count { it.streak.doneToday && !it.streak.isOnBreak }
                val total = habits.count { !it.streak.isOnBreak }
                binding.tvHeaderSubtitle.text = "Best streak: $bestStreak days · $doneToday/$total done today"
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.lastUncheckedId.collect { habitId ->
                if (habitId != null) {
                    viewModel.clearLastUnchecked()
                    Snackbar.make(binding.root, "Removed today's check-in", Snackbar.LENGTH_LONG)
                        .setAction("Undo") { viewModel.toggleHabit(habitId) }
                        .show()
                }
            }
        }

        binding.fabAddHabit.setOnClickListener {
            AddHabitBottomSheet.newInstance { name, scheduleType, scheduleDays, timesPerWeek ->
                viewModel.addHabit(name, scheduleType, scheduleDays, timesPerWeek)
            }.show(childFragmentManager, "add_habit")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── RecyclerView Adapter ───────────────────────────────────────────
class HabitsAdapter(
    private val onToggle: (Long) -> Unit,
    private val onNameUpdated: (Long, String) -> Unit,
    private val onDelete: (HabitEntity) -> Unit,
    private val onTakeBreak: (Long, Int) -> Unit,
    private val onEndBreak: (Long) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var items: List<HabitUiState> = emptyList()

    fun submitList(list: List<HabitUiState>) {
        items = list
        notifyDataSetChanged()
    }

    inner class HabitViewHolder(val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HabitViewHolder(ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val item = items[position]
        val habit = item.habit
        val streak = item.streak
        val ctx = holder.itemView.context

        with(holder.binding) {

            // ── Zone 1 ─────────────────────────────────────────────
            tvHabitName.text = habit.name
            checkHabit.isChecked = streak.doneToday
            checkHabit.isClickable = false
            checkHabit.isFocusable = false

            root.setOnClickListener {
                onToggle(habit.id)
                // Snackbar on uncheck is driven by ViewModel's lastUncheckedId StateFlow
            }

            btnOverflow.setOnClickListener { v ->
                val popup = PopupMenu(ctx, v)
                popup.menu.add(0, 1, 0, "Edit name")
                popup.menu.add(0, 2, 1, "Delete")
                popup.menu.add(0, 3, 2, if (streak.isOnBreak) "End break early" else "Take a break")
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> showEditNameDialog(ctx, habit)
                        2 -> showDeleteConfirmDialog(ctx, habit)
                        3 -> if (streak.isOnBreak) onEndBreak(habit.id)
                             else onTakeBreak(habit.id, streak.currentStreak)
                    }
                    true
                }
                popup.show()
            }

            // ── Zone 1.5 — Streak at risk ──────────────────────────
            val scheduledToday = StreakCalculator.isScheduledToday(habit.scheduleType, habit.scheduleDays)
            val showWarning = streak.currentStreak >= 5 &&
                    !streak.doneToday && !streak.isOnBreak && scheduledToday
            tvStreakWarning.visibility = if (showWarning) View.VISIBLE else View.GONE

            // ── Zone 2 — Stats ─────────────────────────────────────
            tvStreak.text = "${streak.currentStreak}"
            tvStreak.setTextColor(
                if (streak.isOnBreak) Color.parseColor("#FFC04A") else Color.parseColor("#7FFF6E")
            )
            tvDaysTracked.text = "${streak.daysSinceStart}"
            tvBestStreak.text = "${streak.longestStreak}"

            if (streak.isOnBreak && habit.breakUntil != null) {
                val today = LocalDate.now()
                val breakUntilDate = LocalDate.parse(habit.breakUntil)
                val daysLeft = ChronoUnit.DAYS.between(today, breakUntilDate).toInt() + 1
                tvCompletionRate.text = "Break — returns in $daysLeft days"
                tvCompletionRate.setTextColor(Color.parseColor("#4FD1C5"))
            } else {
                val rate = (streak.completionRate * 100).roundToInt()
                tvCompletionRate.text = "$rate% completion rate"
                tvCompletionRate.setTextColor(Color.parseColor("#888A8F"))
            }

            // ── Zone 3 — Dot history row ───────────────────────────
            dotRow.removeAllViews()
            val breakUntilDate = habit.breakUntil?.let { LocalDate.parse(it) }

            for ((date, isToday, isScheduled) in item.last14Days) {
                if (!isScheduled) continue  // skip non-scheduled days entirely

                val isDone = item.logDateSet.contains(date)
                val dateLocal = LocalDate.parse(date)
                val isBreakDay = breakUntilDate != null && !dateLocal.isAfter(breakUntilDate)

                val dotState = when {
                    isBreakDay            -> DotState.BREAK
                    isToday && isDone     -> DotState.DONE_TODAY
                    isToday               -> DotState.TODAY_NOT_DONE
                    isDone                -> DotState.DONE_SCHEDULED
                    else                  -> DotState.MISSED
                }

                dotRow.addView(buildDotView(ctx, dotState))
            }
        }
    }

    private fun showEditNameDialog(ctx: android.content.Context, habit: HabitEntity) {
        val input = EditText(ctx).apply {
            setText(habit.name)
            selectAll()
        }
        AlertDialog.Builder(ctx)
            .setTitle("Edit name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) onNameUpdated(habit.id, name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(ctx: android.content.Context, habit: HabitEntity) {
        AlertDialog.Builder(ctx)
            .setTitle("Delete habit")
            .setMessage("Delete \"${habit.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> onDelete(habit) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Dot rendering ──────────────────────────────────────────────

    private fun buildDotView(ctx: android.content.Context, state: DotState): View {
        val density = ctx.resources.displayMetrics.density
        val size = (10 * density).toInt()
        val margin = (3 * density).toInt()

        return View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(margin, 0, margin, 0)
            }
            background = when (state) {
                DotState.DONE_SCHEDULED -> filledDotDrawable(density, "#7FFF6E")
                DotState.DONE_TODAY     -> filledDotDrawable(density, "#FFC04A")
                DotState.TODAY_NOT_DONE -> dashedBorderDrawable(density)
                DotState.BREAK          -> filledDotDrawable(density, "#4A5568")
                DotState.MISSED         -> filledDotDrawable(density, "#1E2026")
            }
        }
    }

    private fun filledDotDrawable(density: Float, colorHex: String): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 2f * density
            setColor(Color.parseColor(colorHex))
        }

    private fun dashedBorderDrawable(density: Float): Drawable = object : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f * density
            color = Color.parseColor("#3FFFFFFF")
            pathEffect = DashPathEffect(floatArrayOf(4f * density, 3f * density), 0f)
        }

        override fun draw(canvas: Canvas) {
            val r = 2f * density
            val half = paint.strokeWidth / 2f
            canvas.drawRoundRect(
                RectF(half, half, bounds.width() - half, bounds.height() - half),
                r, r, paint
            )
        }

        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        @Suppress("DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
