package ca.gbc.comp3074.labex4.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Customer::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // I never write the body of this, KSP generates the real implementation at build time
    abstract fun customerDao(): CustomerDao

    companion object {
        // @Volatile so every thread sees INSTANCE as soon as it's set
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // second null check inside synchronized, in case another thread built it while I was waiting
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    // applicationContext so the db never holds onto an Activity and leaks it
                    context.applicationContext,
                    AppDatabase::class.java,
                    "customer_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
