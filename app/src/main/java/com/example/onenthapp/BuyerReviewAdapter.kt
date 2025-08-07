package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview

class BuyerReviewAdapter : RecyclerView.Adapter<BuyerReviewAdapter.BuyerReviewViewHolder>() {

    private val reviewList = mutableListOf<MyReview>()

    inner class BuyerReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nicknameText: TextView = itemView.findViewById(R.id.textNickname)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val reviewContentText: TextView = itemView.findViewById(R.id.textReviewContent)
        private val reviewImage1: ImageView = itemView.findViewById(R.id.reviewImage1)
        private val reviewImage2: ImageView = itemView.findViewById(R.id.reviewImage2)
        private val reviewImage3: ImageView = itemView.findViewById(R.id.reviewImage3)

        fun bind(review: MyReview) {
            nicknameText.text = "나"
            ratingBar.rating = review.rate.toFloat()
            reviewContentText.text = review.content

            val imageViews = listOf(reviewImage1, reviewImage2, reviewImage3)
            imageViews.forEach { it.visibility = View.GONE }

            review.reviewImageList.take(3).forEachIndexed { index, url ->
                imageViews[index].visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(url)
                    .into(imageViews[index])
            }

            // 거래 후기 클릭해도 아무 동작 안 함 (리뷰 수정 X)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_buyer_review, parent, false)
        return BuyerReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyerReviewViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateList(newList: List<MyReview>) {
        reviewList.clear()
        reviewList.addAll(newList)
        notifyDataSetChanged()
    }
}
