import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LABEL: Background Task (Worker)
 * Periodically wakes up to run the localized clinical triage rules against
 * new database entries and triggers emergency notifications without internet access.
 */
class SymptomTriageWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val repository: SymptomRepository // Typically injected via a WorkerFactory or Dependency Injection
) : CoroutineWorker(context, workerParams) {

    private val CHANNEL_ID = "CRITICAL_TRIAGE_ALERTS"
    private val NOTIFICATION_ID = 808

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve all entries currently residing on the device that haven't been evaluated/synced
            val pendingRecords = repository.getUnsyncedRecords()

            if (pendingRecords.isEmpty()) {
                return@withContext Result.success()
            }

            for (record in pendingRecords) {
                // Localized Triage Rule Execution (Bypasses cloud AI dependencies completely)
                val isSevereCondition = record.severityLevel >= 4
                val isHighRiskSymptom = record.symptomName.equals("Fever", ignoreCase = true) ||
                        record.symptomName.equals("Cough", ignoreCase = true)

                if (isSevereCondition && isHighRiskSymptom) {
                    // Instantly fire a high-priority physical OS alert notification
                    triggerEmergencyNotification(record.symptomName, record.severityLevel)

                    // Mark the record as processed locally to prevent redundant alerting loops
                    repository.markRecordAsSynced(record)
                }
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            // If database locks or system resource limits fail, request a retry interval from the OS
            return@withContext Result.retry()
        }
    }

    /**
     * Constructs an offline system-level notification alert with a high-priority interrupt configuration.
     */
    private fun triggerEmergencyNotification(symptom: String, severity: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Register the channel structural parameters on Android 8.0 (API 26) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Medical Triage Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts users regarding immediate high-risk clinical triage patterns discovered locally."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Implicit intent mapping to launch the main application package automatically on user tap action
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error) // Direct system alert icon indicator
            .setContentTitle("Critical Health Alert Detected")
            .setContentText("Severe symptoms logged (${symptom} Level ${severity}/5). Click for guidance.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Your logged entry for ${symptom} shows a severity level of ${severity}/5. This meets the criteria for potential clinical risk. Please visit the nearest healthcare facility or contact a community health worker immediately."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}