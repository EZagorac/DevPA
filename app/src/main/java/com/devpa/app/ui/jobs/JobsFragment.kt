package com.devpa.app.ui.jobs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.data.repository.ClaudeApiService
import com.devpa.app.data.repository.ClaudeMessage
import com.devpa.app.data.repository.ClaudeRequest
import com.devpa.app.databinding.FragmentJobsBinding
import com.devpa.app.databinding.ItemJobBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobItem(
    val title: String,
    val company: String,
    val location: String,
    val type: String,
    val url: String
)

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val api: ClaudeApiService
) : ViewModel() {

    sealed class JobsState {
        object Idle : JobsState()
        object Loading : JobsState()
        data class Success(val jobs: List<JobItem>) : JobsState()
        data class Error(val message: String) : JobsState()
    }

    private val _state = MutableStateFlow<JobsState>(JobsState.Idle)
    val state: StateFlow<JobsState> = _state

    fun loadJobs() {
        viewModelScope.launch {
            _state.value = JobsState.Loading
            try {
                // NOTE: In full implementation, use Indeed's Publisher API or
                // job search API directly. Claude is used here for demo/parsing.
                // Indeed API docs: https://opensource.indeedeng.io/api-documentation/
                val response = api.sendMessage(
                    request = ClaudeRequest(
                        system = "Return ONLY a raw JSON array of job listings. No markdown. No backticks.",
                        messages = listOf(ClaudeMessage(
                            content = "Generate 6 realistic current Unity Developer and Indie Game Developer job listings. Return JSON array: [{title, company, location, type, url}]"
                        ))
                    )
                )
                val text = response.content.firstOrNull { it.type == "text" }?.text ?: "[]"
                val jobs = parseJobsJson(text)
                _state.value = if (jobs.isEmpty()) JobsState.Error("No listings found")
                               else JobsState.Success(jobs)
            } catch (e: Exception) {
                _state.value = JobsState.Error(e.message ?: "Failed to load jobs")
            }
        }
    }

    private fun parseJobsJson(json: String): List<JobItem> {
        return try {
            val clean = json.replace("```json", "").replace("```", "").trim()
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
            val list: List<Map<String, String>> = gson.fromJson(clean, type)
            list.map { map ->
                JobItem(
                    title    = map["title"] ?: "",
                    company  = map["company"] ?: "",
                    location = map["location"] ?: "",
                    type     = map["type"] ?: "Full-time",
                    url      = map["url"] ?: "https://indeed.com"
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}

@AndroidEntryPoint
class JobsFragment : Fragment() {

    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JobsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = JobsAdapter { url ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        binding.recyclerJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerJobs.adapter = adapter

        binding.btnRefresh.setOnClickListener { viewModel.loadJobs() }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is JobsViewModel.JobsState.Idle -> viewModel.loadJobs()
                    is JobsViewModel.JobsState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerJobs.visibility = View.GONE
                    }
                    is JobsViewModel.JobsState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerJobs.visibility = View.VISIBLE
                        adapter.submitList(state.jobs)
                    }
                    is JobsViewModel.JobsState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerJobs.visibility = View.VISIBLE
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

class JobsAdapter(
    private val onViewClick: (String) -> Unit
) : RecyclerView.Adapter<JobsAdapter.ViewHolder>() {

    private var items: List<JobItem> = emptyList()

    fun submitList(list: List<JobItem>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvJobTitle.text = item.title
            tvJobMeta.text  = "${item.company} · ${item.location}"
            tvJobType.text  = item.type
            btnView.setOnClickListener { onViewClick(item.url) }
        }
    }
}
