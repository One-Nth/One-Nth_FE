package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview

class MyReviewAdapter(private val reviewList: List<MyReview>) :
    RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder>() {

    inner class MyReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nicknameText: TextView = itemView.findViewById(R.id.textNickname)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val reviewContentText: TextView = itemView.findViewById(R.id.textReviewContent)
        val reviewImage1: ImageView = itemView.findViewById(R.id.reviewImage1)
        val reviewImage2: ImageView = itemView.findViewById(R.id.reviewImage2)
        val reviewImage3: ImageView = itemView.findViewById(R.id.reviewImage3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_review, parent, false)
        return MyReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.nicknameText.text = "나"
        holder.ratingBar.rating = review.rate.toFloat()
        holder.reviewContentText.text = review.content

        val imageViews = listOf(holder.reviewImage1, holder.reviewImage2, holder.reviewImage3)
        imageViews.forEach { it.visibility = View.GONE }

        review.reviewImageList.take(3).forEachIndexed { index, url ->
            imageViews[index].visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(url)
                .into(imageViews[index])
        }
    }

    override fun getItemCount() = reviewList.size
}
