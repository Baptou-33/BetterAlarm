package com.example.betternalarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Calendar

@Entity(tableName = "alarms")
@TypeConverters(AlarmTypeConverters::class)
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var hour: Int,
    var minute: Int,
    var label: String = "Alarme",
    var isEnabled: Boolean = true,
    var ringtoneUri: String = "default",
    var volume: Int = 100,
    var vibrate: Boolean = true,
    var repetitionType: RepetitionType = RepetitionType.ONCE,
    var repetitionDays: List<Int> = emptyList(),
    var specificDates: List<Long> = emptyList(),
    var snoozeDuration: Int = 5,
    var createdAt: Long = System.currentTimeMillis(),
    var lastTriggered: Long? = null
) {
    fun getTimeInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    fun getNextTriggerTime(): Long {
        val now = System.currentTimeMillis()
        val alarmTime = getTimeInMillis()
        
        return when (repetitionType) {
            RepetitionType.ONCE -> {
                if (alarmTime > now) alarmTime else 0
            }
            RepetitionType.DAILY -> {
                if (alarmTime > now) alarmTime else alarmTime + 24 * 60 * 60 * 1000
            }
            RepetitionType.WEEKLY -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = now
                
                for (i in 0..6) {
                    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + i) % 7
                    if (repetitionDays.contains(dayOfWeek - 1)) {
                        calendar.add(Calendar.DAY_OF_YEAR, i)
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        return calendar.timeInMillis
                    }
                }
                0
            }
            RepetitionType.SPECIFIC_DATES -> {
                val sortedDates = specificDates.sorted()
                val nextDate = sortedDates.find { it > now } ?: sortedDates.firstOrNull()
                if (nextDate != null) {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = nextDate
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    calendar.timeInMillis
                } else {
                    0
                }
            }
        }
    }
    
    fun getFormattedTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    fun getRepetitionSummary(): String {
        return when (repetitionType) {
            RepetitionType.ONCE -> "Une fois"
            RepetitionType.DAILY -> "Tous les jours"
            RepetitionType.WEEKLY -> {
                val days = listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")
                repetitionDays.sorted().map { days[it] }.joinToString(", ")
            }
            RepetitionType.SPECIFIC_DATES -> {
                "Dates spécifiques (${specificDates.size})"
            }
        }
    }
}

enum class RepetitionType {
    ONCE,
    DAILY,
    WEEKLY,
    SPECIFIC_DATES
}
