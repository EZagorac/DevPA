package com.devpa.app.ui.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.data.db.PortfolioDao
import com.devpa.app.data.db.PortfolioItemEntity
import com.devpa.app.databinding.FragmentPortfolioBinding
import com.devpa.app.databinding.ItemPortfolioBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// Default portfolio items — seeded on first launch
val DEFAULT_PORTFOLIO_ITEMS = listOf(
    "Profile" to "Set up GitHub profile with bio & photo",
    "Profile" to "Pin 3–5 best repos to GitHub",
    "Projects" to "Game #1 — complete & playable",
    "Projects" to "Game #2 — different genre or mechanic",
    "Projects" to "Game #3 — shows a specific skill (AI, physics, proc-gen...)",
    "Writing" to "Devlog or postmortem for each game",
    "Publishing" to "Publish at least 1 game on itch.io",
    "Media" to "Record a gameplay trailer (60–90 sec)",
    "Portfolio" to "Portfolio website or polished itch.io profile page",
    "Job Search" to "Game dev focused resume",
    "Job Search" to "Cover letter template",
    "Job Search" to "Apply to your first job",
    "Networking" to "Connect with 5 devs or studios on LinkedIn",
    "Networking" to "Get 1 piece of public feedback or review",
    "Community" to "Submit to a game jam"
)

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val portfolioDao: PortfolioDao
) : ViewModel() {

    data class PortfolioUiState(
        val items: List<PortfolioItemEntity> = emptyList(),
        val completedCount: Int = 0,
        val totalCount: Int = 0
    )

    private val _state = MutableStateFlow(PortfolioUiState())
    val state: StateFlow<PortfolioUiState> = _state

    init {
        seedDefaultsIfEmpty()
        observePortfolio()
    }

    private fun seedDefaultsIfEmpty() {
        viewModelScope.launch {
            if (portfolioDao.getTotalCount() == 0) {
                val defaults = DEFAULT_PORTFOLIO_ITEMS.map { (cat, label) ->
                    PortfolioItemEntity(label = label, category = cat)
                }
                portfolioDao.insertAll(defaults)
            }
        }
    }

    private fun observePortfolio() {
        viewModelScope.launch {
            combine(
                portfolioDao.getAllItems(),
                portfolioDao.getCompletedCountFlow(),
                portfolioDao.getTotalCountFlow()
            ) { items, done, total ->
                PortfolioUiState(items, done, total)
            }.collect { _state.value = it }
        }
    }

    fun toggleItem(item: PortfolioItemEntity) {
        viewModelScope.launch {
            portfolioDao.updateDoneStatus(
                id = item.id,
                isDone = !item.isDone,
                completedAt = if (!item.isDone) System.currentTimeMillis() else null
            )
        }
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PortfolioViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PortfolioAdapter { viewModel.toggleItem(it) }
        binding.recyclerPortfolio.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPortfolio.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                adapter.submitList(state.items)
                val pct = if (state.totalCount > 0)
                    (state.completedCount * 100 / state.totalCount) else 0
                binding.progressPortfolio.progress = pct
                binding.tvProgress.text = "${state.completedCount} / ${state.totalCount} complete"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── Adapter ────────────────────────────────────────────────────────
class PortfolioAdapter(
    private val onToggle: (PortfolioItemEntity) -> Unit
) : RecyclerView.Adapter<PortfolioAdapter.ViewHolder>() {

    private var items: List<PortfolioItemEntity> = emptyList()

    fun submitList(list: List<PortfolioItemEntity>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPortfolioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPortfolioBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvItemLabel.text = item.label
            tvCategory.text = item.category
            checkItem.isChecked = item.isDone
            checkItem.setOnClickListener { onToggle(item) }
            tvItemLabel.alpha = if (item.isDone) 0.4f else 1f
        }
    }
}
