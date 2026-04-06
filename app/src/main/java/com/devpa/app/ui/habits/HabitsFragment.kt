package com.devpa.app.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
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
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Data model for the UI ──────────────────────────────────────────
data class HabitUiState(
    val habit: HabitEntity,
    val streak: StreakCalculator.StreakResult,
    val last14Days: List<Pair<String, Boolean>>  // dateString to isToday
)

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitDao: HabitDao
) : ViewModel() {

    private val _habits = MutableStateFlow<List<HabitUiState>>(emptyList())
    val habits: StateFlow<List<HabitUiState>> = _habits

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitDao.getAllHabits().collect { entities ->
                val today = StreakCalculator.todayString()
                val uiStates = entities.map { habit ->
                    val logs = habitDao.getLogDatesForHabit(habit.id)
                    val streak = StreakCalculator.calculate(logs, habit.startDate)
                    val last14 = StreakCalculator.lastNDays(14).map { (date, isToday) ->
                        date to isToday
                    }
                    // Enrich with completion info
                    val last14WithCompletion = last14.map { (date, isToday) ->
                        Pair(date + if (logs.contains(date)) ":done" else if (isToday) ":today" else "", isToday)
                    }
                    HabitUiState(habit, streak, last14)
                }
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
            } else {
                habitDao.insertLog(HabitLogEntity(habitId = habitId, date = today))
            }
        }
    }

    fun addHabit(name: String) {
        viewModelScope.launch {
            habitDao.insertHabit(
                HabitEntity(name = name, startDate = StreakCalculator.todayString())
            )
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitDao.deleteHabit(habit)
        }
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HabitsAdapter(
            onToggle = { viewModel.toggleHabit(it) },
            onDelete = { viewModel.deleteHabit(it) }
        )
        binding.recyclerHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHabits.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.habits.collect { adapter.submitList(it) }
        }

        binding.fabAddHabit.setOnClickListener { showAddHabitDialog() }
    }

    private fun showAddHabitDialog() {
        val input = EditText(requireContext()).apply { hint = "e.g. Write or push code" }
        AlertDialog.Builder(requireContext())
            .setTitle("New habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) viewModel.addHabit(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── RecyclerView Adapter ───────────────────────────────────────────
class HabitsAdapter(
    private val onToggle: (Long) -> Unit,
    private val onDelete: (HabitEntity) -> Unit
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
        with(holder.binding) {
            tvHabitName.text = item.habit.name
            tvStreak.text = "${item.streak.currentStreak}"
            tvDaysTracked.text = "${item.streak.daysSinceStart}d"
            tvBestStreak.text = "${item.streak.longestStreak}"

            checkHabit.isChecked = item.streak.doneToday
            checkHabit.setOnClickListener { onToggle(item.habit.id) }

            root.setOnLongClickListener {
                onDelete(item.habit)
                true
            }
        }
    }
}
