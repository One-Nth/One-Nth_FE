package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyReview

class BuyerFullReviewAdapter : ListAdapter<MyReview, BuyerFullReviewAdapter.VH>(
    object : DiffUtil.ItemCallback<MyReview>() {
        override fun areItemsTheSame(o: MyReview, n: MyReview) = o.reviewId == n.reviewId
        override fun areContentsTheSame(o: MyReview, n: MyReview) = o == n
    }
) {
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivProfile = v.findViewById<ImageView>(R.id.profileImage)
        private val tvName = v.findViewById<TextView>(R.id.reviewerNameDetail)
        private val tvItemTitle = v.findViewById<TextView>(R.id.productNameText)
        private val ratingBar = v.findViewById<RatingBar>(R.id.ratingBarDetail)
        private val tvContent = v.findViewById<TextView>(R.id.reviewTextDetail)
        private val imageContainer = v.findViewById<LinearLayout>(R.id.imageContainer)

        fun bind(it: MyReview) {
            // 프로필/닉네임
            tvName.text = it.reviewerNickName.ifBlank { "닉네임" }
            val url = it.reviewerProfileImageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(itemView).load(url)
                    .placeholder(R.drawable.profile_base)
                    .error(R.drawable.profile_base)
                    .circleCrop()
                    .into(ivProfile)
            } else ivProfile.setImageResource(R.drawable.profile_base)

            // 상품명 (배치 필요하면 itemId→제목 매핑 나중에 추가)
            tvItemTitle.text = "상품 ID: ${it.itemId}"

            // 별점
            ratingBar.setIsIndicator(true)
            ratingBar.stepSize = 0.5f
            ratingBar.rating = it.rate.toFloat()

            // 내용
            tvContent.text = it.content

            // 이미지 유동
            imageContainer.removeAllViews()
            if (it.reviewImageList.isEmpty()) {
                imageContainer.visibility = View.GONE
            } else {
                imageContainer.visibility = View.VISIBLE
                val margin = (12f * itemView.resources.displayMetrics.density).toInt()
                it.reviewImageList.forEachIndexed { idx, imgUrl ->
                    val iv = ImageView(itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).also { lp ->
                            (lp as LinearLayout.LayoutParams).bottomMargin =
                                if (idx == it.reviewImageList.lastIndex) 0 else margin
                        }
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    Glide.with(itemView).load(imgUrl)
                        .placeholder(R.drawable.rectangle_29)
                        .error(R.drawable.rectangle_29)
                        .into(iv)
                    imageContainer.addView(iv)
                }
            }

            // 전체 목록도 클릭 없음
            itemView.setOnClickListener(null)
            itemView.isClickable = false
            itemView.isFocusable = false
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context)
            .inflate(R.layout.item_buyer_review_list, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
