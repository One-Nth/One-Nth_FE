package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScrapPostAdapter(private val posts: List<Map<String, String>>) :
    RecyclerView.Adapter<ScrapPostAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val tvViews: TextView = itemView.findViewById(R.id.tvViews)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scrap_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        holder.tvCategory.text = post["category"]
        holder.tvTitle.text = post["title"]
        holder.tvContent.text = post["content"]
        holder.tvCommentCount.text = post["commentCount"]
        holder.tvLikeCount.text = post["likeCount"]
        holder.tvViews.text = "조회수 ${post["views"]}"
        holder.tvTime.text = post["time"]
    }

    override fun getItemCount() = posts.size
}
