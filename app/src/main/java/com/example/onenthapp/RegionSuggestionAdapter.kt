package com.example.onenthapp

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.map.SimpleRegion
import com.example.onenthapp.databinding.ItemRegionSearchBinding

class RegionSuggestionAdapter(
    private val onClick: (SimpleRegion) -> Unit,
    private val onEndReached: () -> Unit
) : ListAdapter<SimpleRegion, RegionSuggestionAdapter.VH>(DIFF) {
    
    private var currentKeyword: String = ""
    
    fun updateKeyword(keyword: String) {
        currentKeyword = keyword
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRegionSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, currentKeyword, onClick)
        if (position >= itemCount - 2) onEndReached()
    }

    class VH(private val b: ItemRegionSearchBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(item: SimpleRegion, keyword: String, onClick:(SimpleRegion)->Unit) {
            // 키워드가 있으면 하이라이트, 없으면 일반 텍스트
            if (keyword.isNotEmpty()) {
                b.tvRegionName.text = getHighlightedText(item.regionName, keyword)
            } else {
                b.tvRegionName.text = item.regionName
            }
            b.root.setOnClickListener { onClick(item) }
        }
        
        private fun getHighlightedText(fullText: String, keyword: String): SpannableString {
            val spannable = SpannableString(fullText)
            
            if (keyword.isNotEmpty()) {
                val startIndex = fullText.indexOf(keyword, ignoreCase = true)
                if (startIndex >= 0) {
                    val endIndex = startIndex + keyword.length
                    
                    // 초록색으로 하이라이트
                    val greenColor = ContextCompat.getColor(b.root.context, R.color.main_green)
                    spannable.setSpan(
                        ForegroundColorSpan(greenColor),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    
                    // 볼드체로 강조
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            
            return spannable
        }
    }

    companion object {
        val DIFF = object: DiffUtil.ItemCallback<SimpleRegion>() {
            override fun areItemsTheSame(o: SimpleRegion, n: SimpleRegion) = o.regionId == n.regionId
            override fun areContentsTheSame(o: SimpleRegion, n: SimpleRegion) = o == n
        }
    }
}
