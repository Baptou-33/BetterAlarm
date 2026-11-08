package com.example.betternalarm.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_alarms",
    foreignKeys = [
        ForeignKey(
            entity = Alarm::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["alarmId"])]
)
data class SubAlarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    var offsetMinutes: Int,
    var label: String = "Sous-alarme",
    var isEnabled: Boolean = true,
    var ringtoneUri: String = "default",
    var volume: Int = 100,
    var vibrate: Boolean = true
) {
    fun getTriggerTime(baseAlarmTime: Long): Long {
        return baseAlarmTime + offsetMinutes * 60 * 1000
    }
    
    fun getFormattedOffset(): String {
        return if (offsetMinutes > 0) "+${offsetMinutes}min" else "${offsetMinutes}min"
    }
}
