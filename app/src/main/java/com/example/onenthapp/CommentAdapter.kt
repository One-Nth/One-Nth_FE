import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
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
    fun submitList(list: List<Comment>) { items.apply { clear(); addAll(list) }; notifyDataSetChanged() }

    inner class CommentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val btnMore: ImageButton = v.findViewById(R.id.btnMore)
        private val tvNickname: TextView = v.findViewById(R.id.tvCommentNickname)
        private val tvContent: TextView = v.findViewById(R.id.tvCommentContent)
        private val tvLike: TextView = v.findViewById(R.id.tvLike)

        fun bind(item: Comment) {
            tvNickname.text = item.nickname
            tvContent.text = item.content
            tvLike.text = "좋아요 ${item.likeCount}"

            btnMore.setOnClickListener { anchor ->
                showUserActionsPopup(anchor, item)
            }
        }

        private fun showUserActionsPopup(anchor: View, item: Comment) {
            val ctx = anchor.context
            val view = LayoutInflater.from(ctx).inflate(R.layout.comment_popup_box, null, false)
            val popup = PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

            // 바깥터치 닫힘 + 그림자
//            popup.isOutsideTouchable = true
            popup.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
//            popup.elevation = 12f

            view.findViewById<TextView>(R.id.tvChat).setOnClickListener {
                popup.dismiss()
                onAction(Action.Chat, item)
            }
            view.findViewById<TextView>(R.id.tvBlock).setOnClickListener {
                popup.dismiss()
                onAction(Action.Block, item)
            }

            val xoff = dp(anchor.context, -8)
            val yoff = dp(anchor.context, 0)
            popup.showAsDropDown(anchor, xoff, yoff)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false))

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}

private fun dp(ctx: Context, v: Int): Int =
    (v * ctx.resources.displayMetrics.density).toInt()

