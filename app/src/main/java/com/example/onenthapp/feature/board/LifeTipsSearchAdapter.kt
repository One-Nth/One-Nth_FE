package com.example.onenthapp.feature.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.R
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

            val count = item.imageUrls.size
            if (count == 0) {
                // 첨부 없음 → 썸네일 영역 숨김
                ivThumb.visibility = View.GONE
            } else {
                ivThumb.visibility = View.VISIBLE
                Glide.with(ivThumb.context).load(item.imageUrls[0]).centerCrop().into(ivThumb)

                // 이미지가 2장 이상이면 배지 표시
                if (count > 1) {                 // 2장 이상일 때만 배지 노출
                    tvImageCount.visibility = View.VISIBLE
                    tvImageCount.text = count.toString()
                } else {
                    tvImageCount.visibility = View.GONE
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
