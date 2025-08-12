import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.Comment
import com.example.onenthapp.R
import com.example.onenthapp.data.notificationboard.CommentItem

class CommentAdapter(
    private var comments: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvNickname: TextView = itemView.findViewById(R.id.tvCommentNickname)
        val tvContent: TextView = itemView.findViewById(R.id.tvCommentContent)
        val tvLike: TextView = itemView.findViewById(R.id.tvLike)

        fun bind(comment: Comment) {
            tvNickname.text = comment.nickname
            tvContent.text = comment.content
            tvLike.text = "좋아요 ${comment.likeCount}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(comments: List<Comment>) {
        this.comments = comments.toMutableList()
        notifyDataSetChanged()
    }

}
