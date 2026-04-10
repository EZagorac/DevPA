package com.devpa.app.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devpa.app.databinding.BottomSheetBreakPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class BreakPickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBreakPickerBinding? = null
    private val binding get() = _binding!!

    var onBreakConfirmed: ((dateString: String) -> Unit)? = null

    companion object {
        fun newInstance(
            onBreakConfirmed: (dateString: String) -> Unit
        ): BreakPickerBottomSheet = BreakPickerBottomSheet().also { it.onBreakConfirmed = onBreakConfirmed }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBreakPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000

        // Minimum: tomorrow; maximum: 14 days from today
        binding.datePicker.minDate = now + oneDayMs
        binding.datePicker.maxDate = now + 14 * oneDayMs

        // Pre-select tomorrow
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        binding.datePicker.updateDate(
            tomorrow.get(Calendar.YEAR),
            tomorrow.get(Calendar.MONTH),
            tomorrow.get(Calendar.DAY_OF_MONTH)
        )

        binding.btnConfirmBreak.setOnClickListener {
            val date = LocalDate.of(
                binding.datePicker.year,
                binding.datePicker.month + 1,  // DatePicker month is 0-indexed
                binding.datePicker.dayOfMonth
            )
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            onBreakConfirmed?.invoke(dateString)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
