package com.example.betternalarm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.betternalarm.R
import com.example.betternalarm.model.Alarm

class AlarmAdapter(
    private val onAlarmToggle: (Alarm) -> Unit,
    private val onAlarmClick: (Alarm) -> Unit,
    private val onAlarmDelete: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = getItem(position)
        holder.bind(alarm, onAlarmToggle, onAlarmClick, onAlarmDelete)
    }
    
    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val textViewLabel: TextView = itemView.findViewById(R.id.textViewLabel)
        private val textViewRepetition: TextView = itemView.findViewById(R.id.textViewRepetition)
        private val switchEnabled: Switch = itemView.findViewById(R.id.switchEnabled)
        private val textViewSubAlarms: TextView = itemView.findViewById(R.id.textViewSubAlarms)
        
        fun bind(
            alarm: Alarm,
            onAlarmToggle: (Alarm) -> Unit,
            onAlarmClick: (Alarm) -> Unit,
            onAlarmDelete: (Alarm) -> Unit
        ) {
            textViewTime.text = alarm.getFormattedTime()
            textViewLabel.text = alarm.label
            textViewRepetition.text = alarm.getRepetitionSummary()
            switchEnabled.isChecked = alarm.isEnabled
            
            textViewSubAlarms.text = "0 sous-alarmes"
            
            switchEnabled.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    if (buttonView?.isPressed == true) {
                        onAlarmToggle(alarm)
                    }
                }
            })
            
            itemView.setOnClickListener {
                onAlarmClick(alarm)
            }
            
            itemView.setOnLongClickListener {
                onAlarmDelete(alarm)
                true
            }
        }
    }
}

class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
    override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem == newItem
    }
}
