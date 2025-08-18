package com.example.onenthapp.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R
import com.example.onenthapp.data.chat.ChatMessage
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone

class ChatAdapter(
    private val myMemberId: Int,
    private val peerNickname: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatLayout: LinearLayout = itemView.findViewById(R.id.leftChatLayout)
        val leftMessage: TextView = itemView.findViewById(R.id.leftMessage)
        val leftMessageTime: TextView = itemView.findViewById(R.id.leftMessageTime)
        val rightChatLayout: LinearLayout = itemView.findViewById(R.id.rightChatLayout)
        val rightMessage: TextView = itemView.findViewById(R.id.rightMessage)
        val rightMessageTime: TextView = itemView.findViewById(R.id.rightMessageTime)
        val leftNickname: TextView = itemView.findViewById(R.id.leftNickname)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]
        val isMe = chat.senderMemberId == myMemberId

        val formattedTime = formatServerTime(chat.messageTime)

        if (isMe) {
            holder.rightChatLayout.visibility = View.VISIBLE
            holder.leftChatLayout.visibility = View.GONE

            holder.rightMessage.text = chat.content
            holder.rightMessageTime.text = formattedTime
        } else {
            holder.leftChatLayout.visibility = View.VISIBLE
            holder.rightChatLayout.visibility = View.GONE

            holder.leftMessage.text = chat.content
            holder.leftMessageTime.text = formattedTime

            holder.leftNickname.text = peerNickname // 여기서 닉네임 넣기
            holder.leftNickname.visibility = View.VISIBLE
        }
    }


    override fun getItemCount(): Int = messages.size

    fun setMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ISO 8601 형식 문자열을 한국 시간 기준 "오후 7:30" 형식으로 변환하는 함수
    private fun formatServerTime(isoTime: String): String {
        return try {
            // 밀리초 3자리까지만 남기기 (예: 882433 -> 882)
            val trimmedTime = if (isoTime.length > 23) isoTime.substring(0, 23) + "Z" else isoTime

            val sdfUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.KOREA)
            sdfUtc.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdfUtc.parse(trimmedTime) ?: return isoTime

            val sdfKorea = SimpleDateFormat("a h:m", Locale.KOREA)
            sdfKorea.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            sdfKorea.format(date)
        } catch (e: Exception) {
            isoTime
        }
    }


}
