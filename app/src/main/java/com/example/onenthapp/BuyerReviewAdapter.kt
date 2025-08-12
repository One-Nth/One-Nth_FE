package com.example.onenthapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview


data class ProfileLite(val nickname: String?, val imageUrl: String?)

class BuyerReviewAdapter : RecyclerView.Adapter<BuyerReviewAdapter.BuyerReviewViewHolder>() {

    private val reviewList = mutableListOf<MyReview>()

    fun updateList(newList: List<MyReview>) {
        reviewList.clear()
        reviewList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class BuyerReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nicknameText: TextView = itemView.findViewById(R.id.textNickname)
        private val avatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val reviewContentText: TextView = itemView.findViewById(R.id.textReviewContent)
        private val reviewImage1: ImageView = itemView.findViewById(R.id.reviewImage1)
        private val reviewImage2: ImageView = itemView.findViewById(R.id.reviewImage2)
        private val reviewImage3: ImageView = itemView.findViewById(R.id.reviewImage3)

        fun bind(review: MyReview) {
            nicknameText.text = review.reviewerNickName.ifBlank { "익명" }

            val url = review.reviewerProfileImageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(itemView.context).load(url)
                    .placeholder(R.drawable.profile_base)
                    .error(R.drawable.profile_base)
                    .circleCrop()
                    .into(avatar)
            } else avatar.setImageResource(R.drawable.profile_base)

            ratingBar.setIsIndicator(true)
            ratingBar.stepSize = 0.5f          // ✅ 반개 별
            ratingBar.rating = review.rate.toFloat()

            reviewContentText.text = review.content

            val imageViews = listOf(reviewImage1, reviewImage2, reviewImage3)
            imageViews.forEach { it.visibility = View.GONE }
            review.reviewImageList.take(3).forEachIndexed { idx, imgUrl ->
                imageViews[idx].visibility = View.VISIBLE
                Glide.with(itemView.context).load(imgUrl).into(imageViews[idx])
            }

            // ✅ 미리보기는 클릭 금지
            itemView.setOnClickListener(null)
            itemView.isClickable = false
            itemView.isFocusable = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BuyerReviewViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_buyer_review, parent, false))

    override fun onBindViewHolder(holder: BuyerReviewViewHolder, position: Int) =
        holder.bind(reviewList[position])

    override fun getItemCount(): Int = reviewList.size
}
