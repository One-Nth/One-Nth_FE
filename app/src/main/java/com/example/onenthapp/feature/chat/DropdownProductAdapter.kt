package com.example.onenthapp.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.onenthapp.R

data class ProductItem(
    val name: String,
    val imageUrl: String,
    val itemId: Int,
    val itemType: String,
    val dealConfirmationId: Int
)

class DropdownProductAdapter(
    context: Context,
    private val items: List<ProductItem>
) : ArrayAdapter<ProductItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // 선택된 아이템 보여줄 때도 첫 번째 항목 스타일로 보여주고 싶다면 item_dropdown 사용
        return createView(position, convertView, parent)
    }
    fun getItemByName(name: String): ProductItem? {
        return items.find { it.name == name }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val useHeaderLayout = (position == 0) // 드롭다운에서 첫 번째 항목만 item_dropdown
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_dropdown, parent, false)

        val item = items[position]
        val imageView = view.findViewById<ImageView>(R.id.itemImage)
        val textView = view.findViewById<TextView>(R.id.itemName)

        textView.text = item.name

        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.box_green_border) // 로딩 전 기본 이미지
            .into(imageView)

        val line10 = view.findViewById<ImageView>(R.id.line10)
        val product = view.findViewById<TextView>(R.id.product)

        if (position >= 1) {
            line10.visibility = View.GONE
            product.visibility = View.GONE
        } else {
            line10.visibility = View.VISIBLE
            product.visibility = View.VISIBLE
        }

        return view
    }
}
