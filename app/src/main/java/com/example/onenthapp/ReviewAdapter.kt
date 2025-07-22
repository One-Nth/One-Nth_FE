package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val reviews: List<ReviewData>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reviewerName: TextView = view.findViewById(R.id.reviewerName)
        val starRating: TextView = view.findViewById(R.id.starRating)
        val reviewText: TextView = view.findViewById(R.id.reviewText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_card, parent, false)  // ✅ 여기가 핵심
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = reviews[position]
        holder.reviewerName.text = item.name
        holder.starRating.text = "★".repeat(item.rating)
        holder.reviewText.text = item.text
    }

    override fun getItemCount(): Int = reviews.size
}
