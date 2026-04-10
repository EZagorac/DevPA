package com.devpa.app.data.repository

import com.devpa.app.data.db.JourneyDao
import com.devpa.app.data.db.JourneyEntity
import com.devpa.app.data.db.JourneyStepDao
import com.devpa.app.data.db.JourneyStepEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyRepository @Inject constructor(
    val journeyDao: JourneyDao,
    val stepDao: JourneyStepDao,
    val prefsRepository: JourneyPrefsRepository
) {
    // ── Journey passthrough ────────────────────────────────────────
    fun getAllJourneys(): Flow<List<JourneyEntity>> = journeyDao.getAllJourneys()
    fun getActiveJourneys(): Flow<List<JourneyEntity>> = journeyDao.getActiveJourneys()
    suspend fun getJourneyById(id: Long): JourneyEntity? = journeyDao.getJourneyById(id)
    suspend fun insertJourney(journey: JourneyEntity): Long = journeyDao.insertJourney(journey)
    suspend fun updateJourney(journey: JourneyEntity) = journeyDao.updateJourney(journey)
    suspend fun deleteJourney(journey: JourneyEntity) = journeyDao.deleteJourney(journey)
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?) =
        journeyDao.updateStatus(id, status, completedAt)

    // ── Step passthrough ───────────────────────────────────────────
    fun getStepsForJourney(journeyId: Long): Flow<List<JourneyStepEntity>> =
        stepDao.getStepsForJourney(journeyId)
    suspend fun getStepsForJourneyOnce(journeyId: Long): List<JourneyStepEntity> =
        stepDao.getStepsForJourneyOnce(journeyId)
    suspend fun getNextThreeSteps(journeyId: Long): List<JourneyStepEntity> =
        stepDao.getNextThreeSteps(journeyId)
    suspend fun getNextStep(journeyId: Long): JourneyStepEntity? = stepDao.getNextStep(journeyId)
    suspend fun insertStep(step: JourneyStepEntity): Long = stepDao.insertStep(step)
    suspend fun insertAllSteps(steps: List<JourneyStepEntity>) = stepDao.insertAll(steps)
    suspend fun updateStep(step: JourneyStepEntity) = stepDao.updateStep(step)
    suspend fun deleteStep(step: JourneyStepEntity) = stepDao.deleteStep(step)
    suspend fun updateProgress(id: Long, pct: Int, isDone: Boolean, completedAt: Long?) =
        stepDao.updateProgress(id, pct, isDone, completedAt)
    suspend fun updateSortOrder(id: Long, sortOrder: Int) = stepDao.updateSortOrder(id, sortOrder)
    suspend fun renameCategory(journeyId: Long, oldName: String, newName: String) =
        stepDao.renameCategory(journeyId, oldName, newName)
    suspend fun deleteCategory(journeyId: Long, name: String) =
        stepDao.deleteCategory(journeyId, name)

    // ── Prefs passthrough ──────────────────────────────────────────
    val activeJourneyId: Flow<Long?> get() = prefsRepository.activeJourneyId
    val viewMode: Flow<String> get() = prefsRepository.viewMode
    suspend fun setActiveJourney(id: Long) = prefsRepository.setActiveJourney(id)
    suspend fun setViewMode(mode: String) = prefsRepository.setViewMode(mode)

    // ── Computed helpers ───────────────────────────────────────────
    suspend fun getJourneyProgressPct(journeyId: Long): Int {
        val total = stepDao.getTotalStepCount(journeyId)
        if (total == 0) return 0
        val sumPct = stepDao.getSumProgressPct(journeyId) ?: 0
        return sumPct / total
    }

    suspend fun checkAndCompleteJourney(journeyId: Long) {
        val total = stepDao.getTotalStepCount(journeyId)
        val completed = stepDao.getCompletedStepCount(journeyId)
        if (total > 0 && total == completed) {
            journeyDao.updateStatus(journeyId, "COMPLETED", System.currentTimeMillis())
        }
    }
}
