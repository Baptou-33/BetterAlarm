package com.example.betternalarm.repository

import androidx.lifecycle.LiveData
import com.example.betternalarm.dao.AlarmDao
import com.example.betternalarm.model.Alarm
import com.example.betternalarm.model.SubAlarm

class AlarmRepository(private val alarmDao: AlarmDao) {
    
    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAllAlarms()
    val enabledAlarms: LiveData<List<Alarm>> = alarmDao.getEnabledAlarms()
    
    suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }
    
    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }
    
    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }
    
    suspend fun getAlarmById(alarmId: Long): Alarm? {
        return alarmDao.getAlarmById(alarmId)
    }
    
    suspend fun getAllAlarmsList(): List<Alarm> {
        return alarmDao.getAllAlarmsList()
    }
    
    suspend fun insertSubAlarm(subAlarm: SubAlarm): Long {
        return alarmDao.insertSubAlarm(subAlarm)
    }
    
    suspend fun updateSubAlarm(subAlarm: SubAlarm) {
        alarmDao.updateSubAlarm(subAlarm)
    }
    
    suspend fun deleteSubAlarm(subAlarm: SubAlarm) {
        alarmDao.deleteSubAlarm(subAlarm)
    }
    
    suspend fun getSubAlarmsByAlarmId(alarmId: Long): List<SubAlarm> {
        return alarmDao.getSubAlarmsByAlarmId(alarmId)
    }
    
    suspend fun getEnabledSubAlarmsByAlarmId(alarmId: Long): List<SubAlarm> {
        return alarmDao.getEnabledSubAlarmsByAlarmId(alarmId)
    }
    
    suspend fun deleteAllSubAlarmsForAlarm(alarmId: Long) {
        val subAlarms = getSubAlarmsByAlarmId(alarmId)
        subAlarms.forEach { deleteSubAlarm(it) }
    }
}
