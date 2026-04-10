package com.devpa.app.ui.journey

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.R
import com.devpa.app.data.db.JourneyEntity
import com.devpa.app.data.db.JourneyStepEntity
import com.devpa.app.data.repository.JourneyRepository
import com.devpa.app.databinding.FragmentJourneyDetailBinding
import com.devpa.app.databinding.ItemJourneyStepBinding
import com.devpa.app.widget.JourneyWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ── UI State ───────────────────────────────────────────────────────
data class JourneyDetailUiState(
    val journey: JourneyEntity? = null,
    val steps: List<JourneyStepEntity> = emptyList(),
    val viewMode: String = "list",
    val progressPct: Int = 0,
    val completedCount: Int = 0,
    val totalCount: Int = 0
)

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class JourneyDetailViewModel @Inject constructor(
    application: Application,
    val repository: JourneyRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(JourneyDetailUiState())
    val uiState: StateFlow<JourneyDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                repository.activeJourneyId,
                repository.viewMode
            ) { journeyId, viewMode -> journeyId to viewMode }
                .flatMapLatest { (journeyId, viewMode) ->
                    if (journeyId == null) return@flatMapLatest flowOf(JourneyDetailUiState(viewMode = viewMode))
                    repository.getStepsForJourney(journeyId).map { steps ->
                        val journey = repository.getJourneyById(journeyId)
                        val completed = steps.count { it.isDone }
                        val total = steps.size
                        val pct = if (total == 0) 0 else steps.sumOf { it.progressPct } / total
                        JourneyDetailUiState(journey, steps, viewMode, pct, completed, total)
                    }
                }
                .collect { _uiState.value = it }
        }
    }

    fun toggleStepDone(step: JourneyStepEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (step.isDone) {
                repository.updateProgress(step.id, 0, false, null)
            } else {
                repository.updateProgress(step.id, 100, true, System.currentTimeMillis())
                val journeyId = step.journeyId
                repository.checkAndCompleteJourney(journeyId)
            }
            refreshWidgetsForJourney(step.journeyId)
        }
    }

    fun updateStepProgress(stepId: Long, pct: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDone = pct == 100
            val completedAt = if (isDone) System.currentTimeMillis() else null
            repository.updateProgress(stepId, pct, isDone, completedAt)
        }
    }

    fun reorderSteps(from: Int, to: Int) {
        val steps = _uiState.value.steps.toMutableList()
        if (from < 0 || to < 0 || from >= steps.size || to >= steps.size) return
        val movedStep = steps.removeAt(from)
        steps.add(to, movedStep)
        viewModelScope.launch(Dispatchers.IO) {
            steps.forEachIndexed { index, step ->
                repository.updateSortOrder(step.id, index)
            }
        }
    }

    fun setViewMode(mode: String) {
        viewModelScope.launch { repository.setViewMode(mode) }
    }

    fun addStep(label: String, description: String?, category: String?, dueDate: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val journeyId = _uiState.value.journey?.id ?: return@launch
            val currentMax = _uiState.value.steps.maxOfOrNull { it.sortOrder } ?: -1
            repository.insertStep(
                JourneyStepEntity(
                    journeyId = journeyId,
                    label = label,
                    description = description?.takeIf { it.isNotBlank() },
                    category = category?.takeIf { it.isNotBlank() },
                    dueDate = dueDate,
                    sortOrder = currentMax + 1
                )
            )
        }
    }

    fun deleteStep(step: JourneyStepEntity) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteStep(step) }
    }

    fun updateStepNotes(stepId: Long, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val step = _uiState.value.steps.find { it.id == stepId } ?: return@launch
            repository.updateStep(step.copy(notes = notes.takeIf { it.isNotBlank() }))
        }
    }

    private suspend fun refreshWidgetsForJourney(journeyId: Long) {
        val app = getApplication<Application>()
        val manager = AppWidgetManager.getInstance(app)
        val ids = manager.getAppWidgetIds(ComponentName(app, JourneyWidgetProvider::class.java))
        val prefs = app.getSharedPreferences(JourneyWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
        for (id in ids) {
            val stored = prefs.getLong("${JourneyWidgetProvider.PREF_JOURNEY_ID_PREFIX}$id", -1L)
            if (stored == journeyId) {
                withContext(Dispatchers.IO) {
                    JourneyWidgetProvider.refreshWidget(app, manager, id, journeyId)
                }
            }
        }
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class JourneyDetailFragment : Fragment() {

    private var _binding: FragmentJourneyDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JourneyDetailViewModel by viewModels()
    private lateinit var stepsAdapter: JourneyStepsAdapter
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJourneyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepsAdapter = JourneyStepsAdapter(
            onCheckClick = { viewModel.toggleStepDone(it) },
            onLongPress = { step ->
                val categories = viewModel.uiState.value.steps.mapNotNull { it.category }.distinct()
                StepDetailBottomSheet.newInstance(step, categories) { action ->
                    when (action) {
                        is StepDetailAction.UpdateProgress -> viewModel.updateStepProgress(step.id, action.pct)
                        is StepDetailAction.UpdateNotes -> viewModel.updateStepNotes(step.id, action.notes)
                        is StepDetailAction.ToggleDone -> viewModel.toggleStepDone(step)
                        is StepDetailAction.Delete -> viewModel.deleteStep(step)
                    }
                }.show(childFragmentManager, "step_detail")
            }
        )

        binding.recyclerSteps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSteps.adapter = stepsAdapter

        setupDragToReorder()

        binding.fabAddStep.setOnClickListener {
            val categories = viewModel.uiState.value.steps.mapNotNull { it.category }.distinct()
            AddStepBottomSheet.newInstance(categories) { label, category, description, dueDate ->
                viewModel.addStep(label, description, category, dueDate)
            }.show(childFragmentManager, "add_step")
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                updateHeader(state)
                stepsAdapter.submitList(state.steps, state.viewMode)
                val isDragMode = state.viewMode == "list"
                itemTouchHelper?.attachToRecyclerView(if (isDragMode) binding.recyclerSteps else null)

                val empty = state.steps.isEmpty()
                binding.recyclerSteps.visibility = if (empty) View.GONE else View.VISIBLE
                binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateHeader(state: JourneyDetailUiState) {
        val journey = state.journey ?: return
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title =
            "${journey.iconEmoji} ${journey.name}"

        binding.tvProgressLabel.text = "${state.completedCount} / ${state.totalCount} steps · ${state.progressPct}%"
        binding.progressBarHeader.progress = state.progressPct

        binding.tvJourneyEmoji.text = journey.iconEmoji
        binding.tvJourneyName.text = journey.name

        binding.btnToggleView.setOnClickListener {
            viewModel.setViewMode(if (state.viewMode == "list") "group" else "list")
        }

        binding.btnSwitcher.text = "${journey.iconEmoji} ${journey.name.take(12)}${if (journey.name.length > 12) "…" else ""}"
        binding.btnSwitcher.setOnClickListener {
            JourneySwitcherBottomSheet.newInstance {
                // switcher dismissed — nav controller handles back stack
            }.show(childFragmentManager, "switcher")
        }
    }

    private fun setupDragToReorder() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, from: RecyclerView.ViewHolder, to: RecyclerView.ViewHolder): Boolean {
                val fromPos = from.adapterPosition
                val toPos = to.adapterPosition
                // Only allow reordering step items (not headers) in list mode
                if (from is JourneyStepsAdapter.StepViewHolder && to is JourneyStepsAdapter.StepViewHolder) {
                    stepsAdapter.moveItem(fromPos, toPos)
                    viewModel.reorderSteps(fromPos, toPos)
                }
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
            override fun isLongPressDragEnabled() = false
        }
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── Adapter ────────────────────────────────────────────────────────
private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_STEP = 1

sealed class StepListItem {
    data class Header(val category: String, val done: Int, val total: Int) : StepListItem()
    data class Step(val entity: JourneyStepEntity) : StepListItem()
}

class JourneyStepsAdapter(
    private val onCheckClick: (JourneyStepEntity) -> Unit,
    private val onLongPress: (JourneyStepEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<StepListItem> = emptyList()
    private var rawSteps: List<JourneyStepEntity> = emptyList()

    fun submitList(steps: List<JourneyStepEntity>, viewMode: String) {
        rawSteps = steps
        items = if (viewMode == "group") buildGroupedList(steps) else steps.map { StepListItem.Step(it) }
        notifyDataSetChanged()
    }

    fun moveItem(from: Int, to: Int) {
        val mutable = items.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        items = mutable
        notifyItemMoved(from, to)
    }

    private fun buildGroupedList(steps: List<JourneyStepEntity>): List<StepListItem> {
        val result = mutableListOf<StepListItem>()
        val grouped = steps.groupBy { it.category ?: "" }
        for ((cat, catSteps) in grouped) {
            val displayCat = if (cat.isBlank()) "Uncategorised" else cat
            result += StepListItem.Header(displayCat, catSteps.count { it.isDone }, catSteps.size)
            catSteps.forEach { result += StepListItem.Step(it) }
        }
        return result
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is StepListItem.Header -> VIEW_TYPE_HEADER
        is StepListItem.Step -> VIEW_TYPE_STEP
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(android.R.id.text1)
    }

    inner class StepViewHolder(val binding: ItemJourneyStepBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            view.setBackgroundColor(android.graphics.Color.parseColor("#1E2026"))
            (view.findViewById<TextView>(android.R.id.text1)).apply {
                setTextColor(android.graphics.Color.parseColor("#888A8F"))
                textSize = 11f
                setPadding(32, 16, 32, 8)
            }
            HeaderViewHolder(view)
        } else {
            StepViewHolder(ItemJourneyStepBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is StepListItem.Header -> {
                (holder as HeaderViewHolder).tvCategory.text = "${item.category}  ${item.done}/${item.total}"
            }
            is StepListItem.Step -> {
                val step = item.entity
                with((holder as StepViewHolder).binding) {
                    tvStepLabel.text = step.label
                    tvProgress.text = "${step.progressPct}%"
                    progressCircular.progress = step.progressPct
                    checkStep.isChecked = step.isDone

                    if (step.category != null) {
                        tvCategory.visibility = View.VISIBLE
                        tvCategory.text = step.category
                    } else {
                        tvCategory.visibility = View.GONE
                    }

                    if (step.description != null) {
                        tvDescription.visibility = View.VISIBLE
                        tvDescription.text = step.description
                    } else {
                        tvDescription.visibility = View.GONE
                    }

                    checkStep.setOnClickListener { onCheckClick(step) }
                    root.setOnLongClickListener { onLongPress(step); true }
                }
            }
        }
    }
}
