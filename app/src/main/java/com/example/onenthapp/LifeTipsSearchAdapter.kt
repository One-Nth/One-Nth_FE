package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.model.TipItem


class LifeTipsSearchAdapter(
    private val items: List<TipItem>,
    private val onItemClick: (TipItem) -> Unit // 클릭 콜백 추가
) : RecyclerView.Adapter<LifeTipsSearchAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvContent = itemView.findViewById<TextView>(R.id.tvContent)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val tvComment = itemView.findViewById<TextView>(R.id.tvComment)
        val tvLike = itemView.findViewById<TextView>(R.id.tvLike)
        val tvViews = itemView.findViewById<TextView>(R.id.tvViews)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content
        holder.tvTime.text = item.timeAgo
        holder.tvComment.text = item.commentCount.toString()
        holder.tvLike.text = item.likeCount.toString()
        holder.tvViews.text = "조회수 ${item.viewCount}"
    }
}
