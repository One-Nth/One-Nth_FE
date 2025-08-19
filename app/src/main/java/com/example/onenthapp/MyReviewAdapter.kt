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
import com.example.onenthapp.feature.mypage.ReviewEditActivity

class MyReviewAdapter(
    private var reviewList: List<MyReview>  // ← updateList 위해 var
) : RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder>() {

    private var myNickname: String = "나"
    private var myProfileUrl: String? = null

    fun setNickname(nickname: String?) {
        myNickname = if (!nickname.isNullOrBlank()) nickname else "나"
        notifyDataSetChanged()
    }

    /** 닉네임 + 프로필을 한 번에 주입 */
    fun setProfileData(nickname: String?, profileUrl: String?) {
        myNickname = if (!nickname.isNullOrBlank()) nickname else "나"
        myProfileUrl = profileUrl
        notifyDataSetChanged()
    }

    /** 리스트 갱신 */
    fun updateList(newList: List<MyReview>) {
        reviewList = newList
        notifyDataSetChanged()
    }

    inner class MyReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nicknameText: TextView = itemView.findViewById(R.id.textNickname)
        private val profileImage: ImageView? = itemView.findViewById(R.id.profileImage) // 레이아웃에 있으면 세팅
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val reviewContentText: TextView = itemView.findViewById(R.id.textReviewContent)
        private val reviewImage1: ImageView = itemView.findViewById(R.id.reviewImage1)
        private val reviewImage2: ImageView = itemView.findViewById(R.id.reviewImage2)
        private val reviewImage3: ImageView = itemView.findViewById(R.id.reviewImage3)

        fun bind(review: MyReview) {
            nicknameText.text = myNickname

            // 프로필 이미지 (있을 때만)
            profileImage?.let { iv ->
                if (!myProfileUrl.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(myProfileUrl)
                        .placeholder(R.drawable.profile_base)
                        .error(R.drawable.profile_base)
                        .circleCrop()
                        .into(iv)
                } else {
                    iv.setImageResource(R.drawable.profile_base)
                }
            }

            ratingBar.rating = review.rate.toFloat()
            reviewContentText.text = review.content

            val images = listOf(reviewImage1, reviewImage2, reviewImage3)
            images.forEach { it.visibility = View.GONE }
            review.reviewImageList.take(3).forEachIndexed { index, url ->
                images[index].visibility = View.VISIBLE
                Glide.with(itemView.context).load(url).into(images[index])
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ReviewEditActivity::class.java)
                intent.putExtra("reviewId", review.reviewId)
                intent.putExtra("itemType", review.itemType)  // "PURCHASE" or "SHARE"
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_review, parent, false)
        return MyReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size
}
