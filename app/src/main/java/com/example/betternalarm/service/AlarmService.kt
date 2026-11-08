package com.example.betternalarm.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.betternalarm.R
import com.example.betternalarm.database.AlarmDatabase
import com.example.betternalarm.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {
    
    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "SNOOZE_ALARM"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_SUB_ALARM_ID = "sub_alarm_id"
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 1001
    }
    
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false
    private var vibrator: Vibrator? = null
    private var isVibrating = false
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaPlayer = MediaPlayer()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
                val subAlarmId = intent.getLongExtra(EXTRA_SUB_ALARM_ID, -1)
                startAlarm(alarmId, subAlarmId)
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
            ACTION_SNOOZE_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
                snoozeAlarm(alarmId)
            }
        }
        return START_STICKY
    }
    
    private fun startAlarm(alarmId: Long, subAlarmId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AlarmDatabase.getDatabase(this@AlarmService)
            val repository = AlarmRepository(db.alarmDao())
            
            val alarm = repository.getAlarmById(alarmId)
            val subAlarm = if (subAlarmId != -1L) {
                repository.getSubAlarmsByAlarmId(alarmId).find { it.id == subAlarmId }
            } else {
                null
            }
            
            if (alarm != null) {
                startAlarmWithSound(alarm, subAlarm)
            }
        }
    }
    
    private fun startAlarmWithSound(alarm: com.example.betternalarm.model.Alarm, subAlarm: com.example.betternalarm.model.SubAlarm?) {
        try {
            stopAlarm()
            
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, notificationUri)
            mediaPlayer.isLooping = true
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
            
            if ((subAlarm != null && subAlarm.vibrate) || alarm.vibrate) {
                startVibration()
            }
            
            showAlarmNotification(alarm, subAlarm)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun stopAlarm() {
        if (isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isPlaying = false
        }
        
        if (isVibrating) {
            vibrator?.cancel()
            isVibrating = false
        }
        
        stopForeground(true)
        stopSelf()
    }
    
    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 500, 200, 200),
                intArrayOf(0, 255, 0, 255),
                -1
            )
            vibrator?.vibrate(effect)
        } else {
            vibrator?.vibrate(longArrayOf(0, 500, 200, 200), -1)
        }
        isVibrating = true
    }
    
    private fun showAlarmNotification(alarm: com.example.betternalarm.model.Alarm, subAlarm: com.example.betternalarm.model.SubAlarm?) {
        val label = if (subAlarm != null) subAlarm.label else alarm.label
        val text = if (subAlarm != null) {
            "Sous-alarme: ${subAlarm.getFormattedOffset()}"
        } else {
            alarm.getFormattedTime()
        }
        
        val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(label)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, com.example.betternalarm.ui.activities.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ),
                true
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop, "Arrêter", stopPendingIntent)
            .addAction(R.drawable.ic_snooze, "Répéter", snoozePendingIntent)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun snoozeAlarm(alarmId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AlarmDatabase.getDatabase(this@AlarmService)
            val repository = AlarmRepository(db.alarmDao())
            
            val alarm = repository.getAlarmById(alarmId)
            if (alarm != null) {
                stopAlarm()
                
                val snoozeTime = System.currentTimeMillis() + alarm.snoozeDuration * 60 * 1000
                val alarmScheduler = AlarmScheduler(this@AlarmService)
                alarmScheduler.scheduleAlarmAtTime(alarm, snoozeTime)
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal pour les notifications d'alarme"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
