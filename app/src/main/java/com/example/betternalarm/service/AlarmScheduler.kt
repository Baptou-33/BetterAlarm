package com.example.betternalarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.betternalarm.model.Alarm
import com.example.betternalarm.model.SubAlarm
import com.example.betternalarm.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmScheduler(private val context: Context) {
    
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    
    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.isEnabled) return
        
        val nextTriggerTime = alarm.getNextTriggerTime()
        if (nextTriggerTime <= 0) return
        
        scheduleAlarmAtTime(alarm, nextTriggerTime)
    }
    
    fun scheduleAlarmAtTime(alarm: Alarm, triggerTime: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun scheduleSubAlarm(alarm: Alarm, subAlarm: SubAlarm) {
        if (!subAlarm.isEnabled) return
        
        val baseTime = alarm.getTimeInMillis()
        val subAlarmTime = subAlarm.getTriggerTime(baseTime)
        
        if (subAlarmTime <= System.currentTimeMillis()) return
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_SUB_ALARM_ID, subAlarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarm.id * 1000 + subAlarm.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                subAlarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                subAlarmTime,
                pendingIntent
            )
        }
    }
    
    fun cancelSubAlarm(alarm: Alarm, subAlarm: SubAlarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_SUB_ALARM_ID, subAlarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarm.id * 1000 + subAlarm.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun rescheduleAllAlarms(repository: AlarmRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarms = repository.getAllAlarmsList()
                
                for (alarm in alarms) {
                    cancelAlarm(alarm)
                    if (alarm.isEnabled) {
                        scheduleAlarm(alarm)
                    }
                    
                    val subAlarms = repository.getEnabledSubAlarmsByAlarmId(alarm.id)
                    for (subAlarm in subAlarms) {
                        cancelSubAlarm(alarm, subAlarm)
                        if (subAlarm.isEnabled) {
                            scheduleSubAlarm(alarm, subAlarm)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
