package com.devpa.app.ui.journey

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
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
import com.devpa.app.databinding.BottomSheetJourneySwitcherBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SwitcherItemState(
    val journey: JourneyEntity,
    val progressPct: Int,
    val isActive: Boolean
)

@HiltViewModel
class JourneySwitcherViewModel @Inject constructor(
    private val repository: JourneyRepository
) : ViewModel() {
    private val _items = MutableStateFlow<List<SwitcherItemState>>(emptyList())
    val items: StateFlow<List<SwitcherItemState>> = _items

    init {
        viewModelScope.launch {
            combine(repository.getAllJourneys(), repository.activeJourneyId) { journeys, activeId ->
                journeys.map { journey ->
                    val pct = repository.getJourneyProgressPct(journey.id)
                    SwitcherItemState(journey, pct, journey.id == activeId)
                }
            }.collect { _items.value = it }
        }
    }

    fun setActive(id: Long) {
        viewModelScope.launch { repository.setActiveJourney(id) }
    }
}

@AndroidEntryPoint
class JourneySwitcherBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetJourneySwitcherBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JourneySwitcherViewModel by viewModels()

    var onDismissed: (() -> Unit)? = null

    companion object {
        fun newInstance(onDismissed: () -> Unit) =
            JourneySwitcherBottomSheet().also { it.onDismissed = onDismissed }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetJourneySwitcherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SwitcherAdapter { id ->
            viewModel.setActive(id)
            dismiss()
        }
        binding.recyclerJourneys.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerJourneys.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.items.collect { adapter.submitList(it) }
        }

        binding.btnManageJourneys.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.journeyListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissed?.invoke()
        _binding = null
    }
}

class SwitcherAdapter(
    private val onSelect: (Long) -> Unit
) : RecyclerView.Adapter<SwitcherAdapter.ViewHolder>() {

    private var items: List<SwitcherItemState> = emptyList()

    fun submitList(list: List<SwitcherItemState>) {
        items = list; notifyDataSetChanged()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tv_switcher_emoji)
        val tvName: TextView = view.findViewById(R.id.tv_switcher_name)
        val tvProgress: TextView = view.findViewById(R.id.tv_switcher_progress)
        val tvCheck: TextView = view.findViewById(R.id.tv_switcher_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journey_switcher_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val state = items[position]
        holder.tvEmoji.text = state.journey.iconEmoji
        holder.tvName.text = state.journey.name
        holder.tvProgress.text = "${state.progressPct}%"
        holder.tvCheck.visibility = if (state.isActive) View.VISIBLE else View.INVISIBLE
        holder.view.setOnClickListener { onSelect(state.journey.id) }
    }
}
