import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * LABEL: Database Schema (Entity)
 * Defines the structure of the 'symptom_logs' table.
 * Each entry is treated as an independent health record stored locally.
 */

@Entity(tableName = "symptom_logs")
data class SymptomLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,               // Unique ID for each record
    val timestamp: Long,          // System time of the entry (for tracking history)
    val symptomName: String,      // e.g., "Fever", "Cough", "Abdominal Pain"
    val severityLevel: Int,       // Scale of 1 to 5
    val optionalNotes: String?,   // Additional user details
    val calculatedRiskScore: String, // "Low", "Medium", "High" (from Local Triage)
    val bmiSnapshot: Double,        // Recorded BMI at time of log
    val isSynced: Boolean = false  // Track status for Secure Synchronization
)