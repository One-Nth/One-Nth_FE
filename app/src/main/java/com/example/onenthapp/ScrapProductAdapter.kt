package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScrapProductAdapter(private val products: List<Map<String, String>>) :
    RecyclerView.Adapter<ScrapProductAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTag: TextView = itemView.findViewById(R.id.tvTag)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvProductInfo)
        val tvViews: TextView = itemView.findViewById(R.id.tvViews)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scrap_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        holder.tvTag.text = product["tag"]
        holder.tvName.text = product["name"]
        holder.tvInfo.text = product["info"]
        holder.tvViews.text = product["views"]
        holder.tvTime.text = product["time"]

        // 기본 이미지 설정 (필요하면 Glide/Picasso로 URL 이미지 로드)
        holder.ivProduct.setImageResource(R.drawable.rectangle_11)
    }

    override fun getItemCount() = products.size
}
