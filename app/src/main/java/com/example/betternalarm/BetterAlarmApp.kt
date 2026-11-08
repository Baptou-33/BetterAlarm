package com.example.betternalarm

import android.app.Application
import com.example.betternalarm.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BetterAlarmApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        val alarmScheduler = AlarmScheduler(this)
        CoroutineScope(Dispatchers.IO).launch {
            val db = database.AlarmDatabase.getDatabase(this@BetterAlarmApp)
            val repository = repository.AlarmRepository(db.alarmDao())
            alarmScheduler.rescheduleAllAlarms(repository)
        }
    }
}
