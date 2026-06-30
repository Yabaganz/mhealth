import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * LABEL: Database Instance
 * The central database holder that provides the connection to the SQLite file.
 */
@Database(entities = [SymptomLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun symptomLogDao(): SymptomLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton initialization prevents multiple database connections,
         * ensuring thread-safe data persistence.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mhealth_database" // Name of the physical SQLite file on the device
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}