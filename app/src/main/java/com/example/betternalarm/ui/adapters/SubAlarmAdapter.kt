package com.example.betternalarm.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.betternalarm.R
import com.example.betternalarm.model.SubAlarm

class SubAlarmAdapter(
    private val onDelete: (SubAlarm) -> Unit
) : ListAdapter<SubAlarm, SubAlarmAdapter.SubAlarmViewHolder>(SubAlarmDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubAlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_alarm, parent, false)
        return SubAlarmViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SubAlarmViewHolder, position: Int) {
        val subAlarm = getItem(position)
        holder.bind(subAlarm, onDelete)
    }
    
    class SubAlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewLabel: TextView = itemView.findViewById(R.id.textViewSubAlarmLabel)
        private val textViewOffset: TextView = itemView.findViewById(R.id.textViewSubAlarmOffset)
        private val buttonDelete: View = itemView.findViewById(R.id.buttonDeleteSubAlarm)
        
        fun bind(subAlarm: SubAlarm, onDelete: (SubAlarm) -> Unit) {
            textViewLabel.text = subAlarm.label
            textViewOffset.text = subAlarm.getFormattedOffset()
            
            buttonDelete.setOnClickListener {
                onDelete(subAlarm)
            }
        }
    }
}

class SubAlarmDiffCallback : DiffUtil.ItemCallback<SubAlarm>() {
    override fun areItemsTheSame(oldItem: SubAlarm, newItem: SubAlarm): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: SubAlarm, newItem: SubAlarm): Boolean {
        return oldItem == newItem
    }
}
