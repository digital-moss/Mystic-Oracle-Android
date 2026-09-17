package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // "RUNE" or "ICHING"
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<ReadingEntity>>

    @Insert
    suspend fun insertReading(reading: ReadingEntity)

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteReading(id: Long)

    @Query("DELETE FROM readings")
    suspend fun clearAll()
}

@Database(entities = [ReadingEntity::class], version = 1, exportSchema = false)
abstract class OracleDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile
        private var INSTANCE: OracleDatabase? = null

        fun getDatabase(context: Context): OracleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OracleDatabase::class.java,
                    "oracle_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
