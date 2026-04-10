package com.devpa.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devpa.app.data.db.DatabaseProvider
import com.devpa.app.data.db.JourneyEntity
import com.devpa.app.databinding.ActivityJourneyWidgetConfigureBinding
import com.devpa.app.databinding.ItemJourneyWidgetSelectBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JourneyWidgetConfigureActivity : AppCompatActivity() {

    private var _binding: ActivityJourneyWidgetConfigureBinding? = null
    private val binding get() = _binding!!
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setResult(RESULT_CANCELED)

        _binding = ActivityJourneyWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerJourneys.layoutManager = LinearLayoutManager(this)
        binding.btnCancel.setOnClickListener { finish() }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseProvider.getInstance(applicationContext)
            val journeys = db.journeyDao().getAllJourneysSync()
            val items = journeys.map { journey ->
                val stepDao = db.journeyStepDao()
                val total = stepDao.getTotalStepCount(journey.id)
                val pct = if (total == 0) 0 else (stepDao.getSumProgressPct(journey.id) ?: 0) / total
                journey to pct
            }

            withContext(Dispatchers.Main) {
                binding.recyclerJourneys.adapter = JourneySelectAdapter(items) { journey ->
                    onJourneySelected(journey.id)
                }
            }
        }
    }

    private fun onJourneySelected(journeyId: Long) {
        getSharedPreferences(JourneyWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong("${JourneyWidgetProvider.PREF_JOURNEY_ID_PREFIX}$appWidgetId", journeyId)
            .apply()

        val manager = AppWidgetManager.getInstance(this)
        lifecycleScope.launch(Dispatchers.IO) {
            JourneyWidgetProvider.refreshWidget(applicationContext, manager, appWidgetId, journeyId)
            withContext(Dispatchers.Main) {
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

class JourneySelectAdapter(
    private val items: List<Pair<JourneyEntity, Int>>,
    private val onSelect: (JourneyEntity) -> Unit
) : RecyclerView.Adapter<JourneySelectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemJourneyWidgetSelectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemJourneyWidgetSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (journey, pct) = items[position]
        with(holder.binding) {
            tvJourneyEmoji.text = journey.iconEmoji
            tvJourneyName.text = journey.name
            tvJourneyProgress.text = "$pct%"
            try {
                viewColorAccent.setBackgroundColor(Color.parseColor(journey.colourHex))
            } catch (_: IllegalArgumentException) {
                viewColorAccent.setBackgroundColor(Color.parseColor("#7FFF6E"))
            }
            root.setOnClickListener { onSelect(journey) }
        }
    }
}
