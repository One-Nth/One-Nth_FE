package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.databinding.ItemSearchResultBinding
import com.example.onenthapp.model.SearchResult
import com.bumptech.glide.Glide

class SearchAdapter(private val onItemClick: (SearchResult) -> Unit)
    : ListAdapter<SearchResult, SearchAdapter.VH>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : VH {
        val binding = ItemSearchResultBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemSearchResultBinding, private val onItemClick:(SearchResult) -> Unit )
        :RecyclerView.ViewHolder(b.root) {
        fun bind(item: SearchResult) {
            // 카테고리
            b.tvCategory.text = item.category
//            // 이미지 (첫 장만)
//            item.imageUrls.firstOrNull()?.let {
//                Glide.with(b.ivPreview1).load(it).into(b.ivPreview1)
//            } ?: b.ivPreview1.setImageResource(R.drawable.image_tissue_1)
            // 제목·가격·단위
            b.tvTitle.text = item.title
            b.tvPrice.text = "${item.price}원"
            b.tvUnit.text = "/ ${item.unit}"
            // 북마크
            b.btnBookmark.setImageResource(
                if (item.isBookmarked) R.drawable.ic_bookmark_on
                else R.drawable.ic_bookmark_off
            )
            // 이미지 로딩 (Glide 예시) - 최대 3개
            val imageViews = listOf(b.ivPreview1, b.ivPreview2, b.ivPreview3)
            imageViews.forEach { it.visibility = View.GONE } // 일단 모두 숨김

            item.imageUrls.take(3).forEachIndexed { index, imageUrl ->
                if (index < imageViews.size) {
                    imageViews[index].visibility = View.VISIBLE

                    // 임시로 더미 이미지 설정 (제공된 XML 처럼)
                    when (index) {
                        0 -> imageViews[index].setImageResource(R.drawable.image_tissue_1)
                        1 -> imageViews[index].setImageResource(R.drawable.image_tissue_3)
                        2 -> {
                            // 세 번째 이미지가 없을 경우를 대비해 XML 처럼 배경색만 설정하거나,
                            // 실제 이미지가 있다면 로드
                            // imageViews[index].setImageResource(R.drawable.some_other_image)
                            imageViews[index].setBackgroundColor(
                                itemView.context.getColor(R.color.light_gray) // 예시 색상
                            )
                            imageViews[index].setImageDrawable(null) // 이전 이미지 제거
                        }
                    }
                }
            }
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
    override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem.id == newItem.id // 고유 ID로 비교
    }

    override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem == newItem // 데이터 클래스의 equals 비교
    }
}