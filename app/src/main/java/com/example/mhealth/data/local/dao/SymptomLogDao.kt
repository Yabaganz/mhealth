import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * LABEL: Data Access Object (DAO)
 * Provides methods for the application to interact with the database.
 */
@Dao
interface SymptomLogDao {

    // Insert a new log entry into local storage
    interface SymptomLogDao {
        @Insert
        suspend fun insertLog(log: SymptomLog): Long

    // Retrieve all logs for health history monitoring
    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<SymptomLogEntity>

    // Query for unsynced logs to facilitate 'Secured Synchronization'
    @Query("SELECT * FROM symptom_logs WHERE isSynced = 0")
    suspend fun getPendingSyncLogs(): List<SymptomLogEntity>

    @Query("SELECT * FROM symptom_logs WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getLogsForUser(userId: String): Flow<List<SymptomLog>>

    @Query("SELECT * FROM symptom_logs WHERE user_id = :userId AND alert_triggered = 1")
    suspend fun getAlertLogs(userId: String): List<SymptomLog>

    @Query("SELECT * FROM symptom_logs WHERE user_id = :userId AND timestamp >= :startTime")
    suspend fun getLogsSince(userId: String, startTime: Long): List<SymptomLog>

    @Query("DELETE FROM symptom_logs WHERE user_id = :userId AND timestamp < :cutoffTime")
    suspend fun deleteOldLogs(userId: String, cutoffTime: Long): Int

    // Update status after successful sync
    @Update
    suspend fun updateLog(log: SymptomLogEntity)

    // delete a log
    @Delete
    suspend fun deleteLog(log: SymptomLog)

   }

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUser(userId: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Update
    suspend fun updateUser(user: User)
}