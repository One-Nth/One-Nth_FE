package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.MyPostProductItem
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime

class MyPostProductAdapter(
    private var items: List<MyPostProductItem>,
    private val showDelete: Boolean = false,
    private val onDeleteClick: ((MyPostProductItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<MyPostProductAdapter.VH>() {

    fun submitList(newItems: List<MyPostProductItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    /** 현재 리스트 가져오기(롤백용) */
    fun currentItems(): List<MyPostProductItem> = items

    /** 삭제 성공 시 UI에서 바로 제거하고 싶을 때 사용 */
    fun removeAt(position: Int) {
        if (position !in items.indices) return
        items = items.toMutableList().apply { removeAt(position) }
        notifyItemRemoved(position)
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvInfo: TextView = itemView.findViewById(R.id.tvProductInfo)
        private val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: MyPostProductItem, pos: Int) {
            tvName.text = item.productName
            tvInfo.text = "${item.price.formatWon()}"


            val url = item.imageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.rectangle_11)
                    .error(R.drawable.rectangle_11)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.rectangle_11)
            }

            btnDelete.visibility = if (showDelete) View.VISIBLE else View.GONE
            btnDelete.setOnClickListener { onDeleteClick?.invoke(item, pos) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_scrap_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}

