package com.example.betternalarm.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.betternalarm.databinding.ActivityMainBinding
import com.example.betternalarm.service.AlarmScheduler
import com.example.betternalarm.ui.adapters.AlarmAdapter
import com.example.betternalarm.viewmodel.AlarmViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AlarmViewModel
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var alarmAdapter: AlarmAdapter
    
    companion object {
        private const val REQUEST_CODE_NOTIFICATION = 1001
        private const val REQUEST_CODE_ALARM = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        alarmScheduler = AlarmScheduler(this)
        
        setupRecyclerView()
        
        binding.fabAddAlarm.setOnClickListener {
            val intent = Intent(this, AddEditAlarmActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ALARM)
        }
        
        checkPermissions()
        
        viewModel.allAlarms.observe(this) { alarms ->
            alarmAdapter.submitList(alarms)
        }
    }
    
    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onAlarmToggle = { alarm ->
                viewModel.toggleAlarmEnabled(alarm)
                if (alarm.isEnabled) {
                    alarmScheduler.scheduleAlarm(alarm)
                } else {
                    alarmScheduler.cancelAlarm(alarm)
                }
            },
            onAlarmClick = { alarm ->
                val intent = Intent(this, AddEditAlarmActivity::class.java).apply {
                    putExtra("alarm_id", alarm.id)
                }
                startActivityForResult(intent, REQUEST_CODE_ALARM)
            },
            onAlarmDelete = { alarm ->
                viewModel.deleteAlarm(alarm)
                alarmScheduler.cancelAlarm(alarm)
                Toast.makeText(this, "Alarme supprimée", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = alarmAdapter
        }
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(
                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                )
                startActivity(intent)
            }
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_CODE_NOTIFICATION
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions accordées", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Permissions nécessaires pour le bon fonctionnement",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_ALARM && resultCode == RESULT_OK) {
            viewModel.allAlarms.value?.let { alarms ->
                for (alarm in alarms) {
                    if (alarm.isEnabled) {
                        alarmScheduler.scheduleAlarm(alarm)
                    }
                }
            }
        }
    }
}
