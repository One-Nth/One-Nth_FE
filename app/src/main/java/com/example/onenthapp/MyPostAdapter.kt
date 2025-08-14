package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.post.MyPostItem

class MyPostAdapter : ListAdapter<MyPostItem, MyPostAdapter.VH>(diff) {

    companion object {
        private val diff = object : DiffUtil.ItemCallback<MyPostItem>() {
            override fun areItemsTheSame(oldItem: MyPostItem, newItem: MyPostItem) =
                oldItem.postId == newItem.postId
            override fun areContentsTheSame(oldItem: MyPostItem, newItem: MyPostItem) =
                oldItem == newItem
        }

        // postType → 한글 라벨 매핑
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

        fun bind(it: MyPostItem) {
            // ▸ 카테고리 라벨
            tvCategory.text = labelFor(it.postType)

            // ▸ 제목
            tvTitle.text = it.postTitle

            // ▸ 보조 텍스트
            tvContent.text = it.content

            // ▸ 카운트들
            tvComment.text = it.commentCount.toString()
            tvLike.text = it.likeCount.toString()
            tvViews.text = "조회수 ${it.viewCount}"

            // ▸ 시간
            tvTime.text = formatTime(it.createdTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scrap_post, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    private fun formatTime(raw: String): String = try {
        // 서버 포맷 다양성 대비, 실패 시 원문 표시
        raw.replace('T', ' ').take(19)
    } catch (_: Throwable) { raw }
}
