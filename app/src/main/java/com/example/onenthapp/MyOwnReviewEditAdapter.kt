package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.example.onenthapp.data.MyReview

class MyOwnReviewEditAdapter(
    private val onItemClick: (MyReview) -> Unit
) : RecyclerView.Adapter<MyOwnReviewEditAdapter.VH>() {

    private val items = mutableListOf<MyReview>()

    fun submit(list: List<MyReview>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val iv: ImageView = v.findViewById(R.id.ivItemImage)
        private val tvName: TextView = v.findViewById(R.id.tvItemName)

        fun bind(review: MyReview) {
            // 상품명
            tvName.text = review.itemTitle.ifBlank { "상품명 없음" }

            // 가격(현재 API에 가격 없음 → 자리표시자)

            // 썸네일(리뷰 이미지 첫 장 사용)
            val radius = itemView.resources.getDimension(R.dimen.product_card_radius)
            val thumb = review.reviewImageList.firstOrNull()
            if (!thumb.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(thumb)
                    .transform(
                        CenterCrop(),
                        // 순서: topLeft, topRight, bottomRight, bottomLeft
                        GranularRoundedCorners(radius, radius, radius, radius)
                    )
                    .placeholder(R.drawable.rectangle_11)
                    .error(R.drawable.rectangle_11)
                    .into(iv)
            } else {
                iv.setImageResource(R.drawable.rectangle_11)
            }

            // 클릭 콜백
            itemView.setOnClickListener { onItemClick(review) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
