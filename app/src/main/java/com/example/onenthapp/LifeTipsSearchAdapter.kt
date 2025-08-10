package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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

        fun bind(item: TipItem) {
            tvTitle.text = item.title
            tvContent.text = item.content
            tvTime.text = item.timeAgo
            tvComment.text = item.commentCount.toString()
            tvLike.text = item.likeCount.toString()
            tvViews.text = "조회수 ${item.viewCount}"

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
