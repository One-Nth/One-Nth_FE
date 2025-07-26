import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.Comment
import com.example.onenthapp.R

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvNickname: TextView = itemView.findViewById(R.id.tvCommentNickname)
        val tvContent: TextView = itemView.findViewById(R.id.tvCommentContent)
        val tvLike: TextView = itemView.findViewById(R.id.tvLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvNickname.text = comment.nickname
        holder.tvContent.text = comment.content
        holder.tvLike.text = "좋아요 ${comment.likeCount}"
    }

    override fun getItemCount(): Int = comments.size
}
