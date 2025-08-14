package com.example.onenthapp.feature.alarm

import android.widget.ImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R

class AlarmAdapter(private var alarmList: List<AlarmItem>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.alarmMessage)
        val timeTextView: TextView = itemView.findViewById(R.id.alarmTimeAgo)
        val navigationImageView: ImageView = itemView.findViewById(R.id.alramNavigation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_notification, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = alarmList[position]
        holder.messageTextView.text = item.message
        holder.timeTextView.text = item.timeAgo
        holder.navigationImageView.setImageResource(item.navigationImageResId)
    }

    override fun getItemCount(): Int = alarmList.size

    fun updateData(newList: List<AlarmItem>) {
        alarmList = newList
        notifyDataSetChanged()
    }
}
