package com.example.onenthapp.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.R

class ProductRecyclerAdapter(
    private val items: List<ProductItem>
) : RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dropdown_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = items[position]
        holder.itemName.text = product.name

        Glide.with(holder.itemView)
            .load(product.imageUrl)
            .placeholder(R.drawable.box_green_border)
            .into(holder.itemImage)
    }

    override fun getItemCount(): Int = items.size
}
