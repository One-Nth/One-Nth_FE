package com.example.onenthapp

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
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
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvProductInfo)
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scrap_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val it = items[position]

        h.tvName.text = it.productName
        h.tvInfo.text = it.price.formatWon()

        val url = it.imageUrl
        if (!url.isNullOrBlank()) {
            // radius: dimen이 없으면 12dp fallback
            val radiusPx = runCatching {
                h.itemView.resources.getDimension(R.dimen.product_card_radius)
            }.getOrElse { dp(h.itemView, 10).toFloat() }

            Glide.with(h.itemView)
                .load(url)
                .transform(
                    CenterCrop(),
                    // 순서: topLeft, topRight, bottomRight, bottomLeft
                    GranularRoundedCorners(radiusPx, radiusPx, radiusPx, radiusPx)
                )
                .placeholder(R.drawable.rectangle_11)
                .error(R.drawable.rectangle_11)
                .into(h.ivProduct)
        } else {
            h.ivProduct.setImageResource(R.drawable.rectangle_11)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun dp(v: View, value: Int): Int =
        (value * v.resources.displayMetrics.density).toInt()
}

// 금액 포맷 확장
fun Long.formatWon(): String =
    NumberFormat.getInstance().format(this) + "원"
