package com.example.onenthapp

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyPostProductItem
import java.time.Duration
import java.time.OffsetDateTime

class ScrapProductAdapter(
    private var items: List<MyPostProductItem>
) : RecyclerView.Adapter<ScrapProductAdapter.VH>() {

    fun submitList(newItems: List<MyPostProductItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvTag: TextView = itemView.findViewById(R.id.tvTag)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvProductInfo)
//        val tvViews: TextView = itemView.findViewById(R.id.tvViews)
//        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
//        val tvBadge: TextView? = itemView.findViewById(R.id.tvBadge) // 없으면 null
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scrap_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val it = items[position]
//        h.tvTag.text = when (it.itemType.uppercase()) {
//            "PURCHASE" -> "같이 사요"
//            "SHARE" -> "함께 나눠요"
//            else -> it.itemType
//        }
        h.tvName.text = it.productName
        h.tvInfo.text = "${it.price.formatWon()}"
//        h.tvViews.text = "조회수 -"
//        h.tvTime.text = it.createdTime.toRelative()

        // 스크랩 화면에서는 기본적으로 배지 없음
//        h.tvBadge?.visibility = View.GONE

        val url = it.imageUrl
        if (!url.isNullOrBlank()) {
            Glide.with(h.itemView)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.rectangle_11) // 적절한 플레이스홀더
                .error(R.drawable.rectangle_11)
                .into(h.ivProduct)
        } else {
            h.ivProduct.setImageResource(R.drawable.rectangle_11)
        }
    }

    override fun getItemCount(): Int = items.size
}

    fun Long.formatWon(): String =
        NumberFormat.getInstance().format(this) + "원"

    private fun String.toRelative(): String = try {
        val odt = OffsetDateTime.parse(this)
        val minutes = Duration.between(odt, OffsetDateTime.now()).toMinutes()
        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            minutes < 60 * 24 -> "${minutes / 60}시간 전"
            minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}일 전"
        else -> odt.toLocalDate().toString()
    }
} catch (_: Exception) { this }