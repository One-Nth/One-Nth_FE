package com.example.onenthapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.SimpleRegion
import com.example.onenthapp.databinding.ItemRegionSearchBinding

class RegionSuggestionAdapter(
    private val onClick: (SimpleRegion) -> Unit,
    private val onEndReached: () -> Unit
) : ListAdapter<SimpleRegion, RegionSuggestionAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRegionSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClick)
        if (position >= itemCount - 2) onEndReached()
    }

    class VH(private val b: ItemRegionSearchBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(item: SimpleRegion, onClick:(SimpleRegion)->Unit) {
            b.tvRegionName.text = item.regionName
            //b.btnAdd.setOnClickListener { onClick(item) }
            b.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        val DIFF = object: DiffUtil.ItemCallback<SimpleRegion>() {
            override fun areItemsTheSame(o: SimpleRegion, n: SimpleRegion) = o.regionId == n.regionId
            override fun areContentsTheSame(o: SimpleRegion, n: SimpleRegion) = o == n
        }
    }
}
