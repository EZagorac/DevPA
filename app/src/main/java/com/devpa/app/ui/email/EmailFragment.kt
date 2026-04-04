package com.devpa.app.ui.email

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
import com.devpa.app.databinding.FragmentEmailBinding
import com.devpa.app.databinding.ItemEmailBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Data model ─────────────────────────────────────────────────────
enum class EmailUrgency { URGENT, REPLY, INFO }

data class EmailItem(
    val subject: String,
    val from: String,
    val originalSender: String?,   // non-null if forwarded
    val urgency: EmailUrgency,
    val snippet: String
)

// ── ViewModel ──────────────────────────────────────────────────────
@HiltViewModel
class EmailViewModel @Inject constructor(
    private val api: ClaudeApiService
) : ViewModel() {

    sealed class EmailState {
        object Loading : EmailState()
        data class Success(val emails: List<EmailItem>) : EmailState()
        object Empty : EmailState()
        data class Error(val message: String) : EmailState()
    }

    private val _state = MutableStateFlow<EmailState>(EmailState.Loading)
    val state: StateFlow<EmailState> = _state

    // NOTE: In the full implementation, this ViewModel would use the Gmail API
    // client directly (via Google Sign-In credentials) rather than going through
    // Claude. Claude is used here to parse and triage the raw email data.
    //
    // Full Gmail integration steps:
    // 1. User signs in with GoogleSignIn (see GmailAuthHelper below)
    // 2. Get credential from GoogleSignInAccount
    // 3. Build Gmail service: Gmail.Builder(transport, jsonFactory, credential)
    // 4. Fetch messages: gmail.users().messages().list("me").execute()
    // 5. Pass raw data to Claude for urgency classification

    fun loadEmails(rawEmailJson: String) {
        viewModelScope.launch {
            _state.value = EmailState.Loading
            try {
                val system = """
                    You are an email triage assistant. Parse the provided email data and return
                    ONLY a JSON array. Each item: { subject, from, originalSender (null if not forwarded),
                    urgency ("urgent"|"reply"|"info"), snippet }.
                    Filter out Google account/security alerts. No markdown, no backticks.
                """.trimIndent()

                val response = api.sendMessage(
                    request = ClaudeRequest(
                        system = system,
                        messages = listOf(ClaudeMessage(content = "Triage these emails: $rawEmailJson"))
                    )
                )

                val text = response.content.firstOrNull { it.type == "text" }?.text ?: "[]"
                val emails = parseEmailJson(text)

                _state.value = if (emails.isEmpty()) EmailState.Empty
                               else EmailState.Success(emails)
            } catch (e: Exception) {
                _state.value = EmailState.Error(e.message ?: "Failed to load emails")
            }
        }
    }

    private fun parseEmailJson(json: String): List<EmailItem> {
        // Simple JSON parsing — in production use Gson/Moshi
        return try {
            val clean = json.replace("```json", "").replace("```", "").trim()
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String?>>>() {}.type
            val list: List<Map<String, String?>> = gson.fromJson(clean, type)
            list.map { map ->
                EmailItem(
                    subject = map["subject"] ?: "(no subject)",
                    from = map["from"] ?: "",
                    originalSender = map["originalSender"],
                    urgency = when (map["urgency"]) {
                        "urgent" -> EmailUrgency.URGENT
                        "reply"  -> EmailUrgency.REPLY
                        else     -> EmailUrgency.INFO
                    },
                    snippet = map["snippet"] ?: ""
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}

// ── Fragment ───────────────────────────────────────────────────────
@AndroidEntryPoint
class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EmailAdapter()
        binding.recyclerEmail.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEmail.adapter = adapter

        binding.btnRefresh.setOnClickListener {
            // TODO Phase 5: fetch real Gmail data here and pass to viewModel.loadEmails()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is EmailViewModel.EmailState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerEmail.visibility = View.GONE
                        binding.tvEmpty.visibility = View.GONE
                    }
                    is EmailViewModel.EmailState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerEmail.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.submitList(state.emails)
                    }
                    is EmailViewModel.EmailState.Empty -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerEmail.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = "Inbox zero 🎉 Nothing needs your attention"
                    }
                    is EmailViewModel.EmailState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = "Could not load emails. Connect Gmail in Settings."
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

// ── Adapter ────────────────────────────────────────────────────────
class EmailAdapter : RecyclerView.Adapter<EmailAdapter.ViewHolder>() {

    private var items: List<EmailItem> = emptyList()

    fun submitList(list: List<EmailItem>) {
        items = list.sortedBy { it.urgency.ordinal }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemEmailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemEmailBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvSubject.text = item.subject
            tvFrom.text = item.originalSender?.let { "↩ $it (forwarded)" } ?: item.from
            tvSnippet.text = item.snippet
            tvUrgency.text = item.urgency.name

            val (dotColor, badgeColor, textColor) = when (item.urgency) {
                EmailUrgency.URGENT -> Triple(0xFFFF6B6B, 0x26FF6B6B, 0xFFFF6B6B)
                EmailUrgency.REPLY  -> Triple(0xFFFFC04A, 0x26FFC04A, 0xFFFFC04A)
                EmailUrgency.INFO   -> Triple(0xFF60A5FA, 0x1A60A5FA, 0xFF60A5FA)
            }

            viewDot.setBackgroundColor(dotColor.toInt())
            tvUrgency.setTextColor(textColor.toInt())
            tvFrom.setTextColor(
                if (item.originalSender != null) 0xFF4FD1C5.toInt()
                else 0xFF888A8F.toInt()
            )
        }
    }
}
