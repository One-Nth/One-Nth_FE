import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R

data class ChatNotification(
    val chatRoomId: Int,
    val nickname: String,
    val message: String,
    val time: String
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
                    onItemClickListener?.invoke(items[position])
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
        holder.nickname.text = notification.nickname
        holder.message.text = notification.message
        holder.time.text = notification.time
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ChatNotification>) {
        items = newItems
        notifyDataSetChanged()
    }
}
