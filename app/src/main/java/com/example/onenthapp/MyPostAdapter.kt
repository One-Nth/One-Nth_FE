package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.post.MyPostItem

// MyPostAdapter.kt
class MyPostAdapter(
    private val onItemClick: ((MyPostItem) -> Unit)? = null
) : ListAdapter<MyPostItem, MyPostAdapter.VH>(diff) {

    private var showExtraIcon: Boolean = false   // 기본 OFF
    private var onExtraClick: ((MyPostItem) -> Unit)? = null

    companion object {
        private val diff = object : DiffUtil.ItemCallback<MyPostItem>() {
            override fun areItemsTheSame(oldItem: MyPostItem, newItem: MyPostItem) =
                oldItem.postId == newItem.postId
            override fun areContentsTheSame(oldItem: MyPostItem, newItem: MyPostItem) =
                oldItem == newItem
        }

        private fun labelFor(type: String?): String = when (type?.uppercase()) {
            "LIFE_TIP"   -> "생활꿀팁"
            "DISCOUNT"   -> "할인 정보"
            "RESTAURANT" -> "우리동네 맛집/카페"
            else         -> "기타"
        }
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvCategory = v.findViewById<TextView>(R.id.tvCategory)
        private val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        private val tvContent = v.findViewById<TextView>(R.id.tvContent)
        private val tvComment = v.findViewById<TextView>(R.id.tvCommentCount)
        private val tvLike = v.findViewById<TextView>(R.id.tvLikeCount)
        private val tvViews = v.findViewById<TextView>(R.id.tvViews)
        private val tvTime = v.findViewById<TextView>(R.id.tvTime)
        private val ivExtra = v.findViewById<ImageView>(R.id.ivExtra)

        fun bind(item: MyPostItem) {
            tvCategory.text = labelFor(item.postType)
            tvTitle.text = item.postTitle
            tvContent.text = item.content
            tvComment.text = item.commentCount.toString()
            tvLike.text = item.likeCount.toString()
            tvViews.text = "조회수 ${item.viewCount}"
            tvTime.text = formatTime(item.createdTime)

            // ✅ 아이템 클릭 → 콜백에 현재 아이템 전달
            itemView.setOnClickListener { onItemClick?.invoke(item) }

            // 우측 상단(끝) 아이콘 노출/숨김 + 클릭
            ivExtra.visibility = if (showExtraIcon) View.VISIBLE else View.GONE
            if (showExtraIcon) {
                ivExtra.setOnClickListener { onExtraClick?.invoke(item) }
            } else {
                ivExtra.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scrap_post, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatTime(raw: String): String = try {
        raw.replace('T', ' ').take(19)
    } catch (_: Throwable) { raw }

    /** 내 글 보기 화면에서만 우측 아이콘 노출 여부 설정 */
    fun setShowExtraIcon(value: Boolean) {
        showExtraIcon = value
        notifyDataSetChanged()
    }

    /** 우측 아이콘 클릭 콜백 설정 */
    fun setOnExtraClick(listener: ((MyPostItem) -> Unit)?) {
        onExtraClick = listener
    }
}
