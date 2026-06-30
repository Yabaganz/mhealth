import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LABEL: Data Repository (Single Source of Truth)
 * Orchestrates data operations. The UI and Background Workers communicate
 * with this class, not the database directly.
 */
class SymptomRepository(private val symptomLogDao: SymptomLogDao) {

    /**
     * Inserts a new health log into the local Room database.
     * Uses Dispatchers.IO to ensure the database write operation
     * happens on a background thread, preventing UI freezing.
     */
    suspend fun addSymptomLog(log: SymptomLogEntity) {
        withContext(Dispatchers.IO) {
            symptomLogDao.insertSymptom(log)
        }
    }

    /**
     * Fetches the complete chronological history of the user's symptoms.
     * @return List of SymptomLogEntity from local storage.
     */
    suspend fun getHealthHistory(): List<SymptomLogEntity> {
        return withContext(Dispatchers.IO) {
            symptomLogDao.getAllLogs()
        }
    }

    /**
     * Retrieves all logs that have not yet been synchronized with the cloud.
     * This is utilized by the automated WorkManager.
     */
    suspend fun getUnsyncedRecords(): List<SymptomLogEntity> {
        return withContext(Dispatchers.IO) {
            symptomLogDao.getPendingSyncLogs()
        }
    }

    /**
     * Marks a specific log as successfully synced after a secure cloud handshake.
     */
    suspend fun markRecordAsSynced(log: SymptomLogEntity) {
        withContext(Dispatchers.IO) {
            // Update the entity's sync flag to true
            val syncedLog = log.copy(isSynced = true)
            symptomLogDao.updateLog(syncedLog)
        }
    }
}