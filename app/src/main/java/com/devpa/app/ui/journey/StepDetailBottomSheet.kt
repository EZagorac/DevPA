package com.devpa.app.ui.journey

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import com.devpa.app.data.db.JourneyStepEntity
import com.devpa.app.databinding.BottomSheetStepDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class StepDetailAction {
    data class UpdateProgress(val pct: Int) : StepDetailAction()
    data class UpdateNotes(val notes: String) : StepDetailAction()
    object ToggleDone : StepDetailAction()
    object Delete : StepDetailAction()
}

class StepDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetStepDetailBinding? = null
    private val binding get() = _binding!!

    var onAction: ((StepDetailAction) -> Unit)? = null
    private lateinit var step: JourneyStepEntity
    private var existingCategories: List<String> = emptyList()

    private var seekDebounceJob: Job? = null
    private var notesDebounceJob: Job? = null
    private val debounceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        fun newInstance(
            step: JourneyStepEntity,
            existingCategories: List<String>,
            onAction: (StepDetailAction) -> Unit
        ) = StepDetailBottomSheet().also {
            it.step = step
            it.existingCategories = existingCategories
            it.onAction = onAction
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetStepDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStepTitle.text = step.label

        // Category chip
        if (step.category != null) {
            binding.tvCategoryChip.visibility = View.VISIBLE
            binding.tvCategoryChip.text = step.category
        } else {
            binding.tvCategoryChip.visibility = View.GONE
        }

        // Progress seek bar
        binding.seekProgress.progress = step.progressPct
        binding.tvProgressValue.text = "${step.progressPct}% complete"
        binding.seekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                binding.tvProgressValue.text = "$progress% complete"
                seekDebounceJob?.cancel()
                seekDebounceJob = debounceScope.launch {
                    delay(300)
                    onAction?.invoke(StepDetailAction.UpdateProgress(progress))
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) = Unit
        })

        // Notes
        binding.etNotes.setText(step.notes ?: "")
        binding.etNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                notesDebounceJob?.cancel()
                notesDebounceJob = debounceScope.launch {
                    delay(500)
                    onAction?.invoke(StepDetailAction.UpdateNotes(s.toString()))
                }
            }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        })

        // Due date
        val dueDate = step.dueDate
        if (dueDate != null) {
            binding.tvDueDate.visibility = View.VISIBLE
            val isOverdue = try {
                LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE).isBefore(LocalDate.now())
            } catch (_: Exception) { false }
            binding.tvDueDate.text = "Due: $dueDate"
            binding.tvDueDate.setTextColor(if (isOverdue) Color.parseColor("#FFC04A") else Color.parseColor("#888A8F"))
        } else {
            binding.tvDueDate.visibility = View.GONE
        }

        // Toggle done button
        binding.btnToggleDone.text = if (step.isDone) "Mark incomplete" else "Mark complete"
        binding.btnToggleDone.setOnClickListener {
            onAction?.invoke(StepDetailAction.ToggleDone)
            dismiss()
        }

        // Delete button
        binding.btnDeleteStep.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete step")
                .setMessage("Delete \"${step.label}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    onAction?.invoke(StepDetailAction.Delete)
                    dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        debounceScope.cancel()
        _binding = null
    }
}
