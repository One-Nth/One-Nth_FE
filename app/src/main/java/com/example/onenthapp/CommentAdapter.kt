import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.Comment
import com.example.onenthapp.R

class CommentAdapter(
    private val onAction: (Action, Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    enum class Action { Chat, Block }

    private val items = mutableListOf<Comment>()

    fun submitList(list: List<Comment>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNickname: TextView = view.findViewById(R.id.tvCommentNickname)
        private val tvContent: TextView = view.findViewById(R.id.tvCommentContent)
        private val tvLike: TextView = view.findViewById(R.id.tvLike)
        private val btnMore: ImageButton = view.findViewById(R.id.btnMore)

        fun bind(comment: Comment) {
            tvNickname.text = comment.nickname
            tvContent.text = comment.content
            tvLike.text = "좋아요 ${comment.likeCount}"

            btnMore.setOnClickListener {
                showUserActionsPopup(it, comment)
            }
        }

        private fun showUserActionsPopup(anchor: View, item: Comment) {
            val context = anchor.context
            val view = LayoutInflater.from(context).inflate(R.layout.comment_popup_box, null)
            val popup = PopupWindow(
                view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popup.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

            view.findViewById<TextView>(R.id.tvChat).setOnClickListener {
                popup.dismiss()
                onAction(Action.Chat, item)
            }
            view.findViewById<TextView>(R.id.tvBlock).setOnClickListener {
                popup.dismiss()
                onAction(Action.Block, item)
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

private fun dp(context: Context, value: Int): Int {
    return (value * context.resources.displayMetrics.density).toInt()
}
