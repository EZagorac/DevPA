package com.devpa.app.ui.journey

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import com.devpa.app.databinding.BottomSheetAddJourneyBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddJourneyBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddJourneyBinding? = null
    private val binding get() = _binding!!

    var onJourneyCreated: ((name: String, emoji: String, colour: String, templateKey: String) -> Unit)? = null

    private var selectedEmoji = "🎯"
    private var selectedColour = "#7FFF6E"
    private var selectedTemplateKey = "blank"
    private var templatePrefillName = ""

    private val colourOptions = listOf("#7FFF6E", "#60A5FA", "#FFC04A", "#4FD1C5", "#FF6B6B", "#888A8F")
    private val emojiOptions = listOf("🎮", "⚡", "💼", "🚀", "🎯", "📚", "💪", "✨")

    companion object {
        fun newInstance(
            onJourneyCreated: (name: String, emoji: String, colour: String, templateKey: String) -> Unit
        ) = AddJourneyBottomSheet().also { it.onJourneyCreated = onJourneyCreated }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddJourneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEmojiPicker()
        setupColourPicker()
        setupTemplatePicker()
        setupCreateButton()

        binding.etJourneyName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateCreateButton()
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        })
    }

    private fun setupEmojiPicker() {
        val chipGroup = binding.chipGroupEmoji
        chipGroup.removeAllViews()
        emojiOptions.forEach { emoji ->
            val tv = TextView(requireContext()).apply {
                text = emoji
                textSize = 22f
                setPadding(20, 12, 20, 12)
                setOnClickListener {
                    selectedEmoji = emoji
                    binding.tvEmojiPreview.text = emoji
                    refreshEmojiSelection(chipGroup)
                }
                tag = emoji
            }
            chipGroup.addView(tv)
        }
    }

    private fun refreshEmojiSelection(container: android.widget.LinearLayout) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i) as? TextView ?: continue
            child.alpha = if (child.tag == selectedEmoji) 1f else 0.4f
        }
    }

    private fun setupColourPicker() {
        val colourRow = binding.colourPickerRow
        colourRow.removeAllViews()
        colourOptions.forEach { hex ->
            val size = (32 * resources.displayMetrics.density).toInt()
            val margin = (8 * resources.displayMetrics.density).toInt()
            val dot = View(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                }
                tag = hex
                setOnClickListener {
                    selectedColour = hex
                    refreshColourSelection(colourRow)
                }
            }
            colourRow.addView(dot)
        }
        refreshColourSelection(colourRow)
    }

    private fun refreshColourSelection(container: android.widget.LinearLayout) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            val hex = child.tag as? String ?: continue
            val gd = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(hex))
                if (hex == selectedColour) setStroke((3 * resources.displayMetrics.density).toInt(), Color.WHITE)
            }
            child.background = gd
        }
    }

    private fun setupTemplatePicker() {
        val templates = listOf(
            "Game Dev Portfolio" to "game_dev_portfolio",
            "Job Search" to "job_search",
            "Game Jam Sprint" to "game_jam",
            "Indie Launch" to "indie_launch",
            "Blank" to "blank"
        )
        val chipGroup = binding.chipGroupTemplates
        chipGroup.removeAllViews()
        templates.forEach { (label, key) ->
            com.google.android.material.chip.Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = key == "blank"
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        selectedTemplateKey = key
                        if (binding.etJourneyName.text.isNullOrBlank() && label != "Blank") {
                            binding.etJourneyName.setText(label)
                            templatePrefillName = label
                        }
                    }
                }
                chipGroup.addView(this)
            }
        }
    }

    private fun setupCreateButton() {
        binding.btnCreateJourney.setOnClickListener {
            val name = binding.etJourneyName.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener
            onJourneyCreated?.invoke(name, selectedEmoji, selectedColour, selectedTemplateKey)
            dismiss()
        }
        updateCreateButton()
    }

    private fun updateCreateButton() {
        binding.btnCreateJourney.isEnabled = binding.etJourneyName.text?.isNotBlank() == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
