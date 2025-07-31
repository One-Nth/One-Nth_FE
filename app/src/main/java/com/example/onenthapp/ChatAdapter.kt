package com.example.onenthapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatLayout: LinearLayout = itemView.findViewById(R.id.leftChatLayout)
        val leftMessage: TextView = itemView.findViewById(R.id.leftMessage)
        val rightChatLayout: LinearLayout = itemView.findViewById(R.id.rightChatLayout)
        val rightMessage: TextView = itemView.findViewById(R.id.rightMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat, parent, false)
            Log.d("ChatAdapter", "onCreateViewHolder called")
            ChatViewHolder(view)
        } catch (e: Exception) {
            Log.e("ChatAdapter", "Inflate failed: ${e.message}")
            throw e
        }
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]
        val isMe = chat.sender == "유저" // 본인 메시지 구분 (필요시 수정)

        Log.d("ChatAdapter", "Position: $position, Sender: ${chat.sender}, Message: ${chat.message}, isMe: $isMe")


        if (isMe) {
            // 오른쪽 메시지 보여주기
            holder.rightChatLayout.visibility = View.VISIBLE
            holder.leftChatLayout.visibility = View.GONE

            holder.rightMessage.text = chat.message
            holder.rightMessage.setBackgroundResource(R.drawable.bg_chat_bubble_received)
        } else {
            // 왼쪽 메시지 보여주기
            holder.leftChatLayout.visibility = View.VISIBLE
            holder.rightChatLayout.visibility = View.GONE

            holder.leftMessage.text = chat.message
            holder.leftMessage.setBackgroundResource(R.drawable.bg_chat_bubble)
        }
    }

    override fun getItemCount(): Int = messages.size
}
