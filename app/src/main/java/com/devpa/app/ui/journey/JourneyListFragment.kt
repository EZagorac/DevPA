package com.devpa.app.ui.journey

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.R
import com.devpa.app.data.db.JourneyEntity
import com.devpa.app.data.repository.JourneyRepository
import com.devpa.app.data.repository.SeedDataUseCase
import com.devpa.app.databinding.FragmentJourneyListBinding
import com.devpa.app.databinding.ItemJourneyCardBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Data class for list display ────────────────────────────────────
data class JourneyCardState(
    val journey: JourneyEntity,
    val progressPct: Int,
    val completedCount: Int,
    val totalCount: Int,
    val isActive: Boolean
)

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class JourneyListViewModel @Inject constructor(
    val repository: JourneyRepository,
    val seedDataUseCase: SeedDataUseCase
) : ViewModel() {

    private val _journeys = MutableStateFlow<List<JourneyCardState>>(emptyList())
    val journeys: StateFlow<List<JourneyCardState>> = _journeys

    init {
        viewModelScope.launch {
            combine(
                repository.getAllJourneys(),
                repository.activeJourneyId
            ) { journeys, activeId ->
                journeys.map { journey ->
                    val total = repository.stepDao.getTotalStepCount(journey.id)
                    val completed = repository.stepDao.getCompletedStepCount(journey.id)
                    val pct = if (total == 0) 0 else {
                        (repository.stepDao.getSumProgressPct(journey.id) ?: 0) / total
                    }
                    JourneyCardState(journey, pct, completed, total, journey.id == activeId)
                }
            }.collect { _journeys.value = it }
        }
    }

    fun setActiveJourney(id: Long) {
        viewModelScope.launch { repository.setActiveJourney(id) }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val completedAt = if (status == "COMPLETED") System.currentTimeMillis() else null
            repository.updateStatus(id, status, completedAt)
        }
    }

    fun deleteJourney(journey: JourneyEntity) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteJourney(journey) }
    }

    fun createJourneyFromTemplate(
        name: String, emoji: String, colour: String, templateKey: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.journeyDao.getJourneyCount()
            val id = repository.insertJourney(
                JourneyEntity(name = name, iconEmoji = emoji, colourHex = colour, sortOrder = count)
            )
            seedDataUseCase.seedStepsForTemplate(id, templateKey)
        }
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class JourneyListFragment : Fragment() {

    private var _binding: FragmentJourneyListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JourneyListViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJourneyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = JourneyCardAdapter(
            onTap = { state ->
                viewModel.setActiveJourney(state.journey.id)
                findNavController().navigate(R.id.journeyDetailFragment)
            },
            onLongPress = { state, anchorView ->
                showJourneyMenu(state, anchorView)
            }
        )

        binding.recyclerJourneys.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerJourneys.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.journeys.collect { adapter.submitList(it) }
        }

        binding.fabAddJourney.setOnClickListener {
            AddJourneyBottomSheet.newInstance { name, emoji, colour, templateKey ->
                viewModel.createJourneyFromTemplate(name, emoji, colour, templateKey)
            }.show(childFragmentManager, "add_journey")
        }
    }

    private fun showJourneyMenu(state: JourneyCardState, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "Set as active")
        val pauseLabel = if (state.journey.status == "PAUSED") "Resume" else "Pause"
        popup.menu.add(0, 2, 1, pauseLabel)
        popup.menu.add(0, 3, 2, "Delete")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> viewModel.setActiveJourney(state.journey.id)
                2 -> {
                    val newStatus = if (state.journey.status == "PAUSED") "ACTIVE" else "PAUSED"
                    viewModel.updateStatus(state.journey.id, newStatus)
                }
                3 -> AlertDialog.Builder(requireContext())
                    .setTitle("Delete journey")
                    .setMessage("Delete \"${state.journey.name}\"? All steps will be permanently deleted.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteJourney(state.journey) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            true
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── Adapter ────────────────────────────────────────────────────────
class JourneyCardAdapter(
    private val onTap: (JourneyCardState) -> Unit,
    private val onLongPress: (JourneyCardState, View) -> Unit
) : RecyclerView.Adapter<JourneyCardAdapter.ViewHolder>() {

    private var items: List<JourneyCardState> = emptyList()

    fun submitList(list: List<JourneyCardState>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemJourneyCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemJourneyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val state = items[position]
        val journey = state.journey
        with(holder.binding) {
            tvJourneyEmoji.text = journey.iconEmoji
            tvJourneyName.text = journey.name
            tvDescription.text = journey.description.takeIf { it.isNotBlank() } ?: ""
            tvDescription.visibility = if (journey.description.isBlank()) View.GONE else View.VISIBLE
            progressBar.progress = state.progressPct
            tvProgressLabel.text = "${state.completedCount} / ${state.totalCount} · ${state.progressPct}%"

            // Colour accent bar
            try {
                viewColorAccent.setBackgroundColor(Color.parseColor(journey.colourHex))
            } catch (_: IllegalArgumentException) {
                viewColorAccent.setBackgroundColor(Color.parseColor("#7FFF6E"))
            }

            // Status badge
            tvStatusBadge.text = journey.status
            tvStatusBadge.setTextColor(when (journey.status) {
                "PAUSED" -> Color.parseColor("#FFC04A")
                "COMPLETED" -> Color.parseColor("#4FD1C5")
                else -> Color.parseColor("#7FFF6E")
            })

            // Active tint
            root.setBackgroundColor(
                if (state.isActive) Color.parseColor("#1A7FFF6E") else Color.TRANSPARENT
            )

            root.setOnClickListener { onTap(state) }
            root.setOnLongClickListener { onLongPress(state, root); true }
        }
    }
}
