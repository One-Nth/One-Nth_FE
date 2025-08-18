package com.example.onenthapp.feature.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R

data class ChatNotification(
    val chatRoomId: Int,
    val nickname: String,
    val message: String,
    val time: String,
    val roomName: String,
    val opponentId: Int,
    val isBlocked: Boolean = false // 🔴 차단 여부 필드
)

class ChatNotificationAdapter(
    private var items: List<ChatNotification>,
    private val context: Context
) : RecyclerView.Adapter<ChatNotificationAdapter.ViewHolder>() {

    private var onItemClickListener: ((ChatNotification) -> Unit)? = null

    fun setOnItemClickListener(listener: (ChatNotification) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname: TextView = itemView.findViewById(R.id.nicknameTextView)
        val message: TextView = itemView.findViewById(R.id.messageTextView)
        val time: TextView = itemView.findViewById(R.id.timeTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val notification = items[position]

                    if (notification.isBlocked) {
                        Toast.makeText(context, "차단된 사용자와의 채팅은 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    onItemClickListener?.invoke(notification)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = items[position]

        holder.nickname.text = if (notification.isBlocked) "차단된 사용자" else notification.nickname
        holder.message.text = notification.message
        holder.time.text = notification.time

        val backgroundView = holder.itemView.findViewById<View>(R.id.chatnotificationbackground)

        if (notification.isBlocked) {
            backgroundView.setBackgroundResource(R.drawable.rectangle_45) // 빨간 배경
        } else {
            backgroundView.setBackgroundResource(R.drawable.rectangle_46) // 기본 배경
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ChatNotification>) {
        items = newItems
        notifyDataSetChanged()
    }
}
