package com.example.betternalarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.betternalarm.database.AlarmDatabase
import com.example.betternalarm.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmService.ACTION_START_ALARM -> {
                val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1)
                val subAlarmId = intent.getLongExtra(AlarmService.EXTRA_SUB_ALARM_ID, -1)
                startAlarmService(context, alarmId, subAlarmId)
            }
            AlarmService.ACTION_STOP_ALARM -> {
                stopAlarmService(context)
            }
            AlarmService.ACTION_SNOOZE_ALARM -> {
                val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1)
                snoozeAlarm(context, alarmId)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                rescheduleAllAlarms(context)
            }
            Intent.ACTION_TIME_CHANGED -> {
                rescheduleAllAlarms(context)
            }
        }
    }
    
    private fun startAlarmService(context: Context, alarmId: Long, subAlarmId: Long) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_SUB_ALARM_ID, subAlarmId)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
    
    private fun stopAlarmService(context: Context) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
    
    private fun snoozeAlarm(context: Context, alarmId: Long) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
    
    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AlarmDatabase.getDatabase(context)
                val repository = AlarmRepository(db.alarmDao())
                
                val alarms = repository.getAllAlarmsList()
                val alarmScheduler = AlarmScheduler(context)
                
                for (alarm in alarms) {
                    alarmScheduler.cancelAlarm(alarm)
                    if (alarm.isEnabled) {
                        alarmScheduler.scheduleAlarm(alarm)
                    }
                    
                    val subAlarms = repository.getEnabledSubAlarmsByAlarmId(alarm.id)
                    for (subAlarm in subAlarms) {
                        alarmScheduler.cancelSubAlarm(alarm, subAlarm)
                        if (subAlarm.isEnabled) {
                            alarmScheduler.scheduleSubAlarm(alarm, subAlarm)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
