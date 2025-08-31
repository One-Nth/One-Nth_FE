package com.example.onenthapp.feature.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R

class AlarmAdapter(private var alarmList: List<AlarmItem>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var onItemClickListener: ((AlarmItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (AlarmItem) -> Unit) {
        onItemClickListener = listener
    }

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: ConstraintLayout = itemView.findViewById(R.id.alarmRootLayout)
        val messageTextView: TextView = itemView.findViewById(R.id.alarmMessage)
        val timeTextView: TextView = itemView.findViewById(R.id.alarmTimeAgo)
        val typeTextView: TextView = itemView.findViewById(R.id.alarmTypeBox)
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
        holder.typeTextView.text = item.type

        // 읽지 않은 알림이면 배경색 변경
        holder.rootLayout.setBackgroundColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (!item.isRead) R.color.light_red else R.color.main_white
            )
        )

        // 클릭 리스너 연결
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = alarmList.size

    fun updateData(newList: List<AlarmItem>) {
        alarmList = newList
        notifyDataSetChanged()
    }
}
