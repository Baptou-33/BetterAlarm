package com.example.betternalarm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.betternalarm.dao.AlarmDao
import com.example.betternalarm.model.Alarm
import com.example.betternalarm.model.AlarmTypeConverters
import com.example.betternalarm.model.SubAlarm

@Database(
    entities = [Alarm::class, SubAlarm::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AlarmTypeConverters::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    
    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null
        
        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
