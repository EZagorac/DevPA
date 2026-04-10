package com.devpa.app.ui.briefing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.devpa.app.data.db.HabitDao
import com.devpa.app.data.repository.BriefingRepository
import com.devpa.app.data.repository.JourneyRepository
import com.devpa.app.databinding.FragmentBriefingBinding
import com.devpa.app.util.StreakCalculator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class BriefingViewModel @Inject constructor(
    private val briefingRepository: BriefingRepository,
    private val habitDao: HabitDao,
    private val journeyRepository: JourneyRepository
) : ViewModel() {

    sealed class BriefingState {
        object Idle : BriefingState()
        object Loading : BriefingState()
        data class Success(val text: String) : BriefingState()
        data class Error(val message: String) : BriefingState()
    }

    private val _state = MutableStateFlow<BriefingState>(BriefingState.Idle)
    val state: StateFlow<BriefingState> = _state

    fun generateBriefing(tasks: String) {
        if (tasks.isBlank()) {
            _state.value = BriefingState.Error("Add some tasks first!")
            return
        }
        viewModelScope.launch {
            _state.value = BriefingState.Loading
            val today = StreakCalculator.todayString()
            val habits = habitDao.getHabitsWithTodayStatus(today)
            val bestStreak = habits.mapNotNull { h ->
                val logs = habitDao.getLogDatesForHabit(h.id)
                StreakCalculator.calculate(logs, h.startDate).currentStreak
            }.maxOrNull() ?: 0

            // Use active journey progress instead of portfolio
            val activeId = journeyRepository.prefsRepository.activeJourneyId.let { flow ->
                var id: Long? = null
                val job = kotlinx.coroutines.GlobalScope.launch {
                    flow.collect { id = it; this.coroutineContext[kotlinx.coroutines.Job]?.cancel() }
                }
                job.join()
                id
            }
            val done = if (activeId != null) journeyRepository.stepDao.getCompletedStepCount(activeId) else 0
            val total = if (activeId != null) journeyRepository.stepDao.getTotalStepCount(activeId) else 0

            briefingRepository.generateBriefing(tasks, done, total, bestStreak)
                .fold(
                    onSuccess = { _state.value = BriefingState.Success(it) },
                    onFailure = { _state.value = BriefingState.Error(it.message ?: "Unknown error") }
                )
        }
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class BriefingFragment : Fragment() {

    private var _binding: FragmentBriefingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BriefingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBriefingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGenerateBriefing.setOnClickListener {
            val tasks = binding.etTasks.text.toString()
            viewModel.generateBriefing(tasks)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is BriefingViewModel.BriefingState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvBriefingOutput.visibility = View.GONE
                    }
                    is BriefingViewModel.BriefingState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvBriefingOutput.visibility = View.GONE
                        binding.btnGenerateBriefing.isEnabled = false
                    }
                    is BriefingViewModel.BriefingState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvBriefingOutput.visibility = View.VISIBLE
                        binding.tvBriefingOutput.text = state.text
                        binding.btnGenerateBriefing.isEnabled = true
                    }
                    is BriefingViewModel.BriefingState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvBriefingOutput.visibility = View.VISIBLE
                        binding.tvBriefingOutput.text = state.message
                        binding.btnGenerateBriefing.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
