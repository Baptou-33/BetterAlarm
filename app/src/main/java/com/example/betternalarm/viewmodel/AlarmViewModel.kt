package com.example.betternalarm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.betternalarm.database.AlarmDatabase
import com.example.betternalarm.model.Alarm
import com.example.betternalarm.model.RepetitionType
import com.example.betternalarm.model.SubAlarm
import com.example.betternalarm.repository.AlarmRepository
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AlarmRepository
    
    val allAlarms: LiveData<List<Alarm>>
    val enabledAlarms: LiveData<List<Alarm>>
    
    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
        enabledAlarms = repository.enabledAlarms
    }
    
    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insertAlarm(alarm)
    }
    
    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.updateAlarm(alarm)
    }
    
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
    }
    
    fun getAlarmById(alarmId: Long): LiveData<Alarm?> {
        return object : LiveData<Alarm?>() {
            override fun onActive() {
                super.onActive()
                viewModelScope.launch {
                    val alarm = repository.getAlarmById(alarmId)
                    postValue(alarm)
                }
            }
        }
    }
    
    fun insertSubAlarm(subAlarm: SubAlarm) = viewModelScope.launch {
        repository.insertSubAlarm(subAlarm)
    }
    
    fun updateSubAlarm(subAlarm: SubAlarm) = viewModelScope.launch {
        repository.updateSubAlarm(subAlarm)
    }
    
    fun deleteSubAlarm(subAlarm: SubAlarm) = viewModelScope.launch {
        repository.deleteSubAlarm(subAlarm)
    }
    
    fun getSubAlarmsByAlarmId(alarmId: Long): LiveData<List<SubAlarm>> {
        return object : LiveData<List<SubAlarm>>() {
            override fun onActive() {
                super.onActive()
                viewModelScope.launch {
                    val subAlarms = repository.getSubAlarmsByAlarmId(alarmId)
                    postValue(subAlarms)
                }
            }
        }
    }
    
    fun createAlarmWithSubAlarms(
        hour: Int,
        minute: Int,
        label: String,
        repetitionType: RepetitionType = RepetitionType.ONCE,
        repetitionDays: List<Int> = emptyList(),
        specificDates: List<Long> = emptyList(),
        subAlarmsOffsets: List<Int> = emptyList()
    ) = viewModelScope.launch {
        val alarm = Alarm(
            hour = hour,
            minute = minute,
            label = label,
            repetitionType = repetitionType,
            repetitionDays = repetitionDays,
            specificDates = specificDates
        )
        
        val alarmId = repository.insertAlarm(alarm)
        
        subAlarmsOffsets.forEach { offset ->
            val subAlarm = SubAlarm(
                alarmId = alarmId,
                offsetMinutes = offset,
                label = "Sous-alarme +${offset}min"
            )
            repository.insertSubAlarm(subAlarm)
        }
    }
    
    fun toggleAlarmEnabled(alarm: Alarm) = viewModelScope.launch {
        val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        repository.updateAlarm(updatedAlarm)
    }
    
    fun toggleSubAlarmEnabled(subAlarm: SubAlarm) = viewModelScope.launch {
        val updatedSubAlarm = subAlarm.copy(isEnabled = !subAlarm.isEnabled)
        repository.updateSubAlarm(updatedSubAlarm)
    }
}
