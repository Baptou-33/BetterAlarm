package com.example.betternalarm.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.betternalarm.model.Alarm
import com.example.betternalarm.model.SubAlarm

@Dao
interface AlarmDao {
    @Insert
    suspend fun insertAlarm(alarm: Alarm): Long
    
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): LiveData<List<Alarm>>
    
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour, minute")
    fun getEnabledAlarms(): LiveData<List<Alarm>>
    
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): Alarm?
    
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    suspend fun getAllAlarmsList(): List<Alarm>
    
    @Insert
    suspend fun insertSubAlarm(subAlarm: SubAlarm): Long
    
    @Update
    suspend fun updateSubAlarm(subAlarm: SubAlarm)
    
    @Delete
    suspend fun deleteSubAlarm(subAlarm: SubAlarm)
    
    @Query("SELECT * FROM sub_alarms WHERE alarmId = :alarmId ORDER BY offsetMinutes")
    suspend fun getSubAlarmsByAlarmId(alarmId: Long): List<SubAlarm>
    
    @Query("SELECT * FROM sub_alarms WHERE alarmId = :alarmId AND isEnabled = 1 ORDER BY offsetMinutes")
    suspend fun getEnabledSubAlarmsByAlarmId(alarmId: Long): List<SubAlarm>
}
