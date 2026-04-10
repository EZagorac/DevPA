package com.devpa.app.ui.journey

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.view.WindowManager
import com.devpa.app.databinding.BottomSheetAddStepBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AddStepBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddStepBinding? = null
    private val binding get() = _binding!!

    var onStepAdded: ((label: String, category: String?, description: String?, dueDate: String?) -> Unit)? = null

    private var selectedDueDate: String? = null
    private var existingCategories: List<String> = emptyList()

    companion object {
        fun newInstance(
            existingCategories: List<String>,
            onStepAdded: (label: String, category: String?, description: String?, dueDate: String?) -> Unit
        ) = AddStepBottomSheet().also {
            it.existingCategories = existingCategories
            it.onStepAdded = onStepAdded
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Auto-open keyboard on label field
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        // Category autocomplete
        if (existingCategories.isNotEmpty()) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, existingCategories)
            (binding.etCategory as? AutoCompleteTextView)?.setAdapter(adapter)
        }

        // Due date picker
        binding.btnSetDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = LocalDate.of(year, month + 1, day)
                    selectedDueDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    binding.tvSelectedDate.text = selectedDueDate
                    binding.tvSelectedDate.visibility = View.VISIBLE
                    binding.btnClearDate.visibility = View.VISIBLE
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnClearDate.setOnClickListener {
            selectedDueDate = null
            binding.tvSelectedDate.visibility = View.GONE
            binding.btnClearDate.visibility = View.GONE
        }

        binding.etStepLabel.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.btnAddStep.isEnabled = s?.isNotBlank() == true
            }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        })

        binding.btnAddStep.setOnClickListener {
            val label = binding.etStepLabel.text.toString().trim()
            if (label.isEmpty()) return@setOnClickListener
            val category = binding.etCategory.text.toString().trim().takeIf { it.isNotEmpty() }
            val description = binding.etDescription.text.toString().trim().takeIf { it.isNotEmpty() }
            onStepAdded?.invoke(label, category, description, selectedDueDate)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
