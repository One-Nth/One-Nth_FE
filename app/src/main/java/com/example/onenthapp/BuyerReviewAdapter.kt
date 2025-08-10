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
    private var profileMap: Map<Long, ProfileLite> = emptyMap() // reviewerId -> 프로필

    /** 리뷰 목록 갱신 */
    fun updateList(newList: List<MyReview>) {
        reviewList.clear()
        reviewList.addAll(newList)
        notifyDataSetChanged()
    }

    /** 프로필 맵 주입 (키: reviewerId) */
    fun setProfiles(map: Map<Long, ProfileLite>) {
        profileMap = map
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
            // ✅ "내가 받은 리뷰" 화면 → 작성자(reviewerId) 정보를 표시
            val p = profileMap[review.reviewerId]
            nicknameText.text = p?.nickname ?: "익명"

            if (!p?.imageUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(p!!.imageUrl)
                    .placeholder(R.drawable.profile_base)
                    .error(R.drawable.profile_base)
                    .circleCrop()
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.profile_base)
            }

            ratingBar.rating = review.rate.toFloat()
            reviewContentText.text = review.content

            val imageViews = listOf(reviewImage1, reviewImage2, reviewImage3)
            imageViews.forEach { it.visibility = View.GONE }
            review.reviewImageList.take(3).forEachIndexed { index, url ->
                imageViews[index].visibility = View.VISIBLE
                Glide.with(itemView.context).load(url).into(imageViews[index])
            }

            // (선택) 상세 열기: 읽기 전용으로
            itemView.setOnClickListener {
                val ctx = itemView.context
                val intent = Intent(ctx, ReviewEditActivity::class.java).apply {
                    putExtra("reviewId", review.reviewId)
                    putExtra("itemType", review.itemType)
                    putExtra("canEdit", false) // 구매자쪽은 수정 불가
                    // 상세 헤더에서 바로 쓰고 싶으면 아래 표시용 값도 전달
                    putExtra("displayNickname", p?.nickname)
                    putExtra("displayProfileUrl", p?.imageUrl)
                }
                ctx.startActivity(intent)
            }
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
}

