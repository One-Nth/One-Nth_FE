package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.post.SearchPostDto
import com.example.onenthapp.data.post.TipItem

class LifeTipsSearchAdapter(
    private val onItemClick: (TipItem) -> Unit
) : RecyclerView.Adapter<LifeTipsSearchAdapter.ViewHolder>() {

    private val items = mutableListOf<TipItem>()

    fun submitItems(list: List<TipItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        private val tvContent = view.findViewById<TextView>(R.id.tvContent)
        private val tvTime = view.findViewById<TextView>(R.id.tvTime)
        private val tvComment = view.findViewById<TextView>(R.id.tvComment)
        private val tvLike = view.findViewById<TextView>(R.id.tvLike)
        private val tvViews = view.findViewById<TextView>(R.id.tvViews)
        // 썸네일 이미지
        private val ivThumb = view.findViewById<ImageView?>(R.id.ivThumbnail)
        // 첨부 이미지 개수 마크
        private val tvImageCount = view.findViewById<TextView?>(R.id.tvImageCount)

        fun bind(item: TipItem) {
            tvTitle.text = item.title
            tvContent.text = item.content
            tvTime.text = item.timeAgo
            tvComment.text = item.commentCount.toString()
            tvLike.text = item.likeCount.toString()
            tvViews.text = "조회수 ${item.viewCount}"

            // 썸네일 처리
            val hasImage = item.imageUrls.isNotEmpty()
            ivThumb?.let { img ->
                img.visibility = if (hasImage) View.VISIBLE else View.GONE
                if (hasImage) {
                    Glide.with(img.context)
                        .load(item.imageUrls[0])
                        .into(img)
                }
            }

            // 첨부 이미지 개수 마크 처리
            tvImageCount?.let { countView ->
                if (hasImage && item.imageUrls.size > 1) {
                    countView.visibility = View.VISIBLE
                    countView.text = "+${item.imageUrls.size - 1}"
                } else {
                    countView.visibility = View.GONE
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tips_result, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
