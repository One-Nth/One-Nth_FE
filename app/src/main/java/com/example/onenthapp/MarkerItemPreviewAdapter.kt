package com.example.onenthapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.MapItemPreview
import com.example.onenthapp.databinding.ItemSearchResultBinding

class MarkerItemPreviewAdapter(
    private val onItemClick: (MapItemPreview) -> Unit
) : ListAdapter<MapItemPreview, MarkerItemPreviewAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemSearchResultBinding.inflate(LayoutInflater.from(p.context), p, false), onItemClick)

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    class VH(
        private val b: ItemSearchResultBinding,
        private val onItemClick: (MapItemPreview) -> Unit
    ) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MapItemPreview) {
            b.bind(item)
            b.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MapItemPreview>() {
            override fun areItemsTheSame(o: MapItemPreview, n: MapItemPreview) = o.id == n.id
            override fun areContentsTheSame(o: MapItemPreview, n: MapItemPreview) = o == n
        }
    }
}