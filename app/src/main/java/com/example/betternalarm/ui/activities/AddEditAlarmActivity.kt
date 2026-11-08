package com.example.betternalarm.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.betternalarm.databinding.ActivityAddEditAlarmBinding
import com.example.betternalarm.model.Alarm
import com.example.betternalarm.model.RepetitionType
import com.example.betternalarm.model.SubAlarm
import com.example.betternalarm.service.AlarmScheduler
import com.example.betternalarm.ui.adapters.SubAlarmAdapter
import com.example.betternalarm.viewmodel.AlarmViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditAlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddEditAlarmBinding
    private lateinit var viewModel: AlarmViewModel
    private lateinit var alarmScheduler: AlarmScheduler
    private var currentAlarm: Alarm? = null
    private var currentAlarmId: Long = -1
    private lateinit var subAlarmAdapter: SubAlarmAdapter
    private val subAlarmsList = mutableListOf<SubAlarm>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        alarmScheduler = AlarmScheduler(this)
        
        setupSubAlarmRecyclerView()
        
        currentAlarmId = intent.getLongExtra("alarm_id", -1)
        
        if (currentAlarmId != -1L) {
            lifecycleScope.launch {
                currentAlarm = viewModel.getAlarmById(currentAlarmId).value
                currentAlarm?.let { alarm ->
                    populateAlarmData(alarm)
                    loadSubAlarms(alarm.id)
                }
            }
        } else {
            currentAlarm = Alarm(
                hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                minute = Calendar.getInstance().get(Calendar.MINUTE),
                repetitionType = RepetitionType.ONCE
            )
            populateAlarmData(currentAlarm!!)
        }
        
        setupListeners()
    }
    
    private fun setupSubAlarmRecyclerView() {
        subAlarmAdapter = SubAlarmAdapter(
            onDelete = { subAlarm ->
                viewModel.deleteSubAlarm(subAlarm)
                subAlarmsList.remove(subAlarm)
                subAlarmAdapter.notifyDataSetChanged()
            }
        )
        
        binding.recyclerViewSubAlarms.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@AddEditAlarmActivity)
            adapter = subAlarmAdapter
        }
    }
    
    private fun populateAlarmData(alarm: Alarm) {
        binding.apply {
            timePickerHour.setIs24HourView(true)
            timePickerHour.hour = alarm.hour
            timePickerHour.minute = alarm.minute
            
            editTextLabel.setText(alarm.label)
            
            spinnerRepetitionType.setSelection(alarm.repetitionType.ordinal)
            
            updateRepetitionDaysUI(alarm.repetitionType, alarm.repetitionDays)
            
            buttonRepetitionOptions.setOnClickListener {
                showRepetitionOptionsDialog(alarm)
            }
            
            editTextSnooze.setText(alarm.snoozeDuration.toString())
            
            switchVibrate.isChecked = alarm.vibrate
        }
    }
    
    private fun loadSubAlarms(alarmId: Long) {
        viewModel.getSubAlarmsByAlarmId(alarmId).observe(this) { subAlarms ->
            subAlarmsList.clear()
            subAlarmsList.addAll(subAlarms)
            subAlarmAdapter.submitList(subAlarmsList.toList())
        }
    }
    
    private fun setupListeners() {
        binding.apply {
            timePickerHour.setOnTimeChangedListener { _, hour, minute ->
                currentAlarm = currentAlarm?.copy(hour = hour, minute = minute)
            }
            
            editTextLabel.setOnEditorActionListener { _, _, _ ->
                currentAlarm = currentAlarm?.copy(label = editTextLabel.text.toString())
                false
            }
            
            val repetitionTypes = arrayOf(
                "Une fois",
                "Tous les jours",
                "Jours de la semaine",
                "Dates spécifiques"
            )
            val adapter = ArrayAdapter(
                this@AddEditAlarmActivity,
                android.R.layout.simple_spinner_item,
                repetitionTypes
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRepetitionType.adapter = adapter
            
            spinnerRepetitionType.onItemSelectedListener = object : 
                android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    val type = RepetitionType.values()[position]
                    currentAlarm = currentAlarm?.copy(repetitionType = type)
                    updateRepetitionDaysUI(type, currentAlarm?.repetitionDays ?: emptyList())
                }
                
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
            
            buttonAddSubAlarm.setOnClickListener {
                showAddSubAlarmDialog()
            }
            
            buttonSave.setOnClickListener {
                saveAlarm()
            }
            
            buttonCancel.setOnClickListener {
                finish()
            }
            
            switchVibrate.setOnCheckedChangeListener { _, isChecked ->
                currentAlarm = currentAlarm?.copy(vibrate = isChecked)
            }
            
            editTextSnooze.setOnEditorActionListener { _, _, _ ->
                val snooze = editTextSnooze.text.toString().toIntOrNull() ?: 5
                currentAlarm = currentAlarm?.copy(snoozeDuration = snooze)
                false
            }
        }
    }
    
    private fun updateRepetitionDaysUI(type: RepetitionType, days: List<Int>) {
        binding.chipGroupDays.removeAllViews()
        
        when (type) {
            RepetitionType.WEEKLY -> {
                val dayNames = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
                for (i in 0..6) {
                    val chip = Chip(this).apply {
                        text = dayNames[i]
                        isCheckable = true
                        isChecked = days.contains(i)
                    }
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            currentAlarm = currentAlarm?.copy(
                                repetitionDays = (currentAlarm?.repetitionDays ?: emptyList()) + i
                            )
                        } else {
                            currentAlarm = currentAlarm?.copy(
                                repetitionDays = (currentAlarm?.repetitionDays ?: emptyList()).filter { it != i }
                            )
                        }
                    }
                    binding.chipGroupDays.addView(chip)
                }
            }
            else -> {}
        }
    }
    
    private fun showRepetitionOptionsDialog(alarm: Alarm) {
        val items = arrayOf("Une fois", "Tous les jours", "Jours de la semaine", "Dates spécifiques")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Type de répétition")
            .setItems(items) { _, which ->
                val type = RepetitionType.values()[which]
                currentAlarm = currentAlarm?.copy(repetitionType = type)
                binding.spinnerRepetitionType.setSelection(which)
                
                when (type) {
                    RepetitionType.SPECIFIC_DATES -> {
                        showDatePickerDialog()
                    }
                    else -> {
                        updateRepetitionDaysUI(type, currentAlarm?.repetitionDays ?: emptyList())
                    }
                }
            }
            .show()
    }
    
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            val timestamp = selectedCalendar.timeInMillis
            
            currentAlarm = currentAlarm?.copy(
                specificDates = (currentAlarm?.specificDates ?: emptyList()) + timestamp
            )
            
            Toast.makeText(
                this,
                "Date ajoutée: ${selectedDay}/${selectedMonth + 1}/${selectedYear}",
                Toast.LENGTH_SHORT
            ).show()
            
        }, year, month, day).show()
    }
    
    private fun showAddSubAlarmDialog() {
        val offsets = listOf(5, 6, 7, 8, 9, 10, 15, 20, 30)
        val offsetNames = offsets.map { "+$it min" }.toTypedArray()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Ajouter une sous-alarme")
            .setItems(offsetNames) { _, which ->
                val offset = offsets[which]
                
                if (currentAlarmId != -1L) {
                    val subAlarm = SubAlarm(
                        alarmId = currentAlarmId,
                        offsetMinutes = offset,
                        label = "Sous-alarme +${offset}min"
                    )
                    viewModel.insertSubAlarm(subAlarm)
                    subAlarmsList.add(subAlarm)
                    subAlarmAdapter.notifyDataSetChanged()
                } else {
                    subAlarmsList.add(
                        SubAlarm(
                            alarmId = 0,
                            offsetMinutes = offset,
                            label = "Sous-alarme +${offset}min"
                        )
                    )
                    subAlarmAdapter.notifyDataSetChanged()
                }
            }
            .setPositiveButton("Autre...") { _, _ ->
                showCustomOffsetDialog()
            }
            .show()
    }
    
    private fun showCustomOffsetDialog() {
        val editText = android.widget.EditText(this)
        editText.hint = "Décalage en minutes"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Décalage personnalisé (minutes)")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val offset = editText.text.toString().toIntOrNull() ?: 0
                if (offset > 0) {
                    if (currentAlarmId != -1L) {
                        val subAlarm = SubAlarm(
                            alarmId = currentAlarmId,
                            offsetMinutes = offset,
                            label = "Sous-alarme +${offset}min"
                        )
                        viewModel.insertSubAlarm(subAlarm)
                        subAlarmsList.add(subAlarm)
                        subAlarmAdapter.notifyDataSetChanged()
                    } else {
                        subAlarmsList.add(
                            SubAlarm(
                                alarmId = 0,
                                offsetMinutes = offset,
                                label = "Sous-alarme +${offset}min"
                            )
                        )
                        subAlarmAdapter.notifyDataSetChanged()
                    }
                }
            }
            .setNegativeButton("Annuler") { _, _ -> }
            .show()
    }
    
    private fun saveAlarm() {
        val alarm = currentAlarm ?: return
        
        val updatedAlarm = alarm.copy(
            hour = binding.timePickerHour.hour,
            minute = binding.timePickerHour.minute,
            label = binding.editTextLabel.text.toString(),
            vibrate = binding.switchVibrate.isChecked,
            snoozeDuration = binding.editTextSnooze.text.toString().toIntOrNull() ?: 5
        )
        
        if (currentAlarmId != -1L) {
            viewModel.updateAlarm(updatedAlarm)
            alarmScheduler.cancelAlarm(updatedAlarm)
            
            if (updatedAlarm.isEnabled) {
                alarmScheduler.scheduleAlarm(updatedAlarm)
            }
            
            Toast.makeText(this, "Alarme mise à jour", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                val newAlarmId = viewModel.insertAlarm(updatedAlarm)
                
                for (subAlarm in subAlarmsList) {
                    viewModel.insertSubAlarm(subAlarm.copy(alarmId = newAlarmId))
                }
                
                if (updatedAlarm.isEnabled) {
                    alarmScheduler.scheduleAlarm(updatedAlarm.copy(id = newAlarmId))
                }
                
                Toast.makeText(this@AddEditAlarmActivity, "Alarme créée", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            return
        }
        
        setResult(RESULT_OK)
        finish()
    }
}
