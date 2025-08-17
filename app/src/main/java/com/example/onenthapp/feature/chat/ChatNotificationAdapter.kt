import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R

data class ChatNotification(
    val chatRoomId: Int,
    val nickname: String,
    val message: String,
    val time: String,
    val roomName: String,        // ✅ WebSocket용
    val opponentId : Int,
)

class ChatNotificationAdapter(
    private var items: List<ChatNotification>,
    private val context: Context
) : RecyclerView.Adapter<ChatNotificationAdapter.ViewHolder>() {

    // 클릭 리스너 저장할 변수 선언
    private var onItemClickListener: ((ChatNotification) -> Unit)? = null

    // 클릭 리스너 등록 함수
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
                    val isBlockedUser = notification.nickname.isNullOrBlank() || notification.nickname == "알 수 없음"
                    if (isBlockedUser) {
                        // 차단된 사용자이므로 진입 불가 토스트 띄우기
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
        val isBlockedUser = notification.nickname.isNullOrBlank() || notification.nickname == "알 수 없음"

        // 닉네임 처리
        holder.nickname.text = if (isBlockedUser) "차단된 사용자" else notification.nickname
        holder.message.text = notification.message
        holder.time.text = notification.time

        // 배경 처리
        val backgroundView = holder.itemView.findViewById<View>(R.id.chatnotificationbackground)
        if (isBlockedUser) {
            backgroundView.setBackgroundResource(R.drawable.rectangle_45)
        } else {
            // 기본 배경으로 복구 (기본 배경 리소스명으로 교체 필요)
            backgroundView.setBackgroundResource(R.drawable.rectangle_46)
        }
    }


    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ChatNotification>) {
        items = newItems
        notifyDataSetChanged()
    }
}
