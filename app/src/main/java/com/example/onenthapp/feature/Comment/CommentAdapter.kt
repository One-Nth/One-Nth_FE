// com.example.onenthapp.ui.comment.CommentAdapter (패키지는 네 구조에 맞게)
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.R
import com.example.onenthapp.data.notificationboard.UiComment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CommentAdapter(
    private val onAction: (Action, UiComment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    enum class Action { Chat, Block }

    private val items = mutableListOf<UiComment>()

    fun submitList(list: List<UiComment>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        private val tvNickname: TextView = view.findViewById(R.id.tvCommentNickname)
        private val tvContent: TextView = view.findViewById(R.id.tvCommentContent)
        private val tvLike: TextView = view.findViewById(R.id.tvLike)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val btnMore: ImageButton = view.findViewById(R.id.btnMore)

        fun bind(c: UiComment) {
            tvNickname.text = c.nickname
            tvContent.text  = c.content
            tvTime.text     = formatCommentTime(c.createdAt)

            // 서버가 댓글 좋아요 수를 아직 안 준다면 0 또는 숨김 처리
            tvLike.text = "좋아요 0"  // 필요 시 GONE 처리 가능

            val url = c.profileImageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(itemView).load(url)
                    .circleCrop()
                    .placeholder(R.drawable.profile_base)
                    .error(R.drawable.profile_base)
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.profile_base)
            }

            btnMore.setOnClickListener { showUserActionsPopup(it, c) }
        }

        private fun showUserActionsPopup(anchor: View, item: UiComment) {
            val context = anchor.context
            val view = LayoutInflater.from(context).inflate(R.layout.comment_popup_box, null)
            val popup = PopupWindow(
                view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                )
            }

            view.findViewById<TextView>(R.id.tvChat).setOnClickListener {
                popup.dismiss(); onAction(Action.Chat, item)
            }
            view.findViewById<TextView>(R.id.tvBlock).setOnClickListener {
                popup.dismiss(); onAction(Action.Block, item)
            }

            popup.showAsDropDown(anchor, dp(anchor.context, -8), dp(anchor.context, 0))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

private fun dp(context: Context, value: Int): Int =
    (value * context.resources.displayMetrics.density).toInt()

private fun formatCommentTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val trimmed = raw.substringBefore('.')
        if ('T' in trimmed) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(trimmed) ?: return trimmed.replace('T', ' ')
            val diff = System.currentTimeMillis() - date.time
            val m = diff / 60000
            val h = m / 60
            val d = h / 24
            when {
                m < 1  -> "방금 전"
                m < 60 -> "${m}분 전"
                h < 24 -> "${h}시간 전"
                d < 7  -> "${d}일 전"
                else   -> trimmed.replace('T', ' ')
            }
        } else if (Regex("""\d{4}\.\d{2}\.\d{2}""").matches(trimmed)) {
            trimmed
        } else trimmed
    } catch (_: Exception) {
        raw
    }
}
