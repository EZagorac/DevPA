package com.devpa.app.ui.habits

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.devpa.app.databinding.BottomSheetAddHabitBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class AddHabitBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddHabitBinding? = null
    private val binding get() = _binding!!

    var onHabitAdded: ((name: String, scheduleType: String, scheduleDays: String, timesPerWeek: Int) -> Unit)? = null

    private var timesPerWeekCount = 3

    companion object {
        fun newInstance(
            onHabitAdded: (name: String, scheduleType: String, scheduleDays: String, timesPerWeek: Int) -> Unit
        ): AddHabitBottomSheet = AddHabitBottomSheet().also { it.onHabitAdded = onHabitAdded }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Open keyboard automatically
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        setupQuickPickChips()
        setupSchedulePicker()
        setupStepper()
        setupAddButton()
    }

    private fun setupQuickPickChips() {
        val chips = listOf(
            binding.chipWrite to "Write or push code",
            binding.chipGame  to "Work on a game project",
            binding.chipLearn to "Learn something new",
            binding.chipApply to "Apply to a job"
        )
        for ((chip, label) in chips) {
            chip.setOnClickListener { binding.editHabitName.setText(label) }
        }
    }

    private fun setupSchedulePicker() {
        binding.radioSchedule.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbEveryDay.id -> {
                    binding.scrollDayChips.visibility = View.GONE
                    binding.layoutStepper.visibility = View.GONE
                }
                binding.rbSpecificDays.id -> {
                    binding.scrollDayChips.visibility = View.VISIBLE
                    binding.layoutStepper.visibility = View.GONE
                }
                binding.rbTimesPerWeek.id -> {
                    binding.scrollDayChips.visibility = View.GONE
                    binding.layoutStepper.visibility = View.VISIBLE
                }
            }
            updateAddButtonState()
        }

        // Re-validate when day chips change
        val dayChipIds = listOf(
            binding.chipMon, binding.chipTue, binding.chipWed, binding.chipThu,
            binding.chipFri, binding.chipSat, binding.chipSun
        )
        for (chip in dayChipIds) {
            chip.setOnCheckedChangeListener { _, _ -> updateAddButtonState() }
        }
    }

    private fun setupStepper() {
        binding.tvTimesCount.text = timesPerWeekCount.toString()

        binding.btnMinus.setOnClickListener {
            if (timesPerWeekCount > 1) {
                timesPerWeekCount--
                binding.tvTimesCount.text = timesPerWeekCount.toString()
            }
        }
        binding.btnPlus.setOnClickListener {
            if (timesPerWeekCount < 7) {
                timesPerWeekCount++
                binding.tvTimesCount.text = timesPerWeekCount.toString()
            }
        }
    }

    private fun setupAddButton() {
        binding.editHabitName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateAddButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.btnAddHabit.setOnClickListener {
            val name = binding.editHabitName.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener

            val scheduleType: String
            val scheduleDays: String
            val timesPerWeek: Int

            when (binding.radioSchedule.checkedRadioButtonId) {
                binding.rbSpecificDays.id -> {
                    scheduleType = "days_of_week"
                    scheduleDays = buildScheduleDays()
                    timesPerWeek = 0
                }
                binding.rbTimesPerWeek.id -> {
                    scheduleType = "times_per_week"
                    scheduleDays = ""
                    timesPerWeek = timesPerWeekCount
                }
                else -> {
                    scheduleType = "daily"
                    scheduleDays = ""
                    timesPerWeek = 0
                }
            }

            onHabitAdded?.invoke(name, scheduleType, scheduleDays, timesPerWeek)
            dismiss()
        }
    }

    private fun buildScheduleDays(): String {
        // Day numbers: Mon=1, Tue=2, ..., Sun=7
        val dayMap = listOf(
            binding.chipMon to 1, binding.chipTue to 2, binding.chipWed to 3,
            binding.chipThu to 4, binding.chipFri to 5, binding.chipSat to 6,
            binding.chipSun to 7
        )
        return dayMap.filter { (chip, _) -> chip.isChecked }.joinToString(",") { (_, num) -> num.toString() }
    }

    private fun updateAddButtonState() {
        val hasName = binding.editHabitName.text.toString().isNotBlank()
        val scheduleValid = when (binding.radioSchedule.checkedRadioButtonId) {
            binding.rbSpecificDays.id -> {
                listOf(
                    binding.chipMon, binding.chipTue, binding.chipWed, binding.chipThu,
                    binding.chipFri, binding.chipSat, binding.chipSun
                ).any { it.isChecked }
            }
            else -> true
        }
        binding.btnAddHabit.isEnabled = hasName && scheduleValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
