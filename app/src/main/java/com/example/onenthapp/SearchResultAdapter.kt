package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.item.ItemSearch
import com.example.onenthapp.databinding.ItemSearchResultBinding

class SearchResultAdapter(
    private val onItemClick: (ItemSearch) -> Unit,
    private val onToggleBookmark: (item: ItemSearch, before: Boolean, onDone: (Boolean)->Unit) -> Unit
) : ListAdapter<ItemSearch, SearchResultAdapter.ViewHolder>(ItemSearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick, onToggleBookmark)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemSearchResultBinding,
        private val onItemClick: (ItemSearch) -> Unit,
        private val onToggleBookmark: (item: ItemSearch, before: Boolean, onDone: (Boolean)->Unit) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemSearch) {
            // 제목
            binding.tvTitle.text = item.title
            
            // 가격 (nullable 안전 처리)
            binding.tvPrice.text = item.price?.takeIf { it.isNotBlank() }?.let { "${it}원" } ?: "가격 협의"
            
            // 카테고리
            binding.tvCategory.text = when (item.category) {
                "ELECTRONICS" -> "전자제품"
                "HOUSEHOLD" -> "생활용품"
                "FOOD" -> "식품"
                "CLOTHING" -> "의류"
                "MISC" -> "잡화"
                else -> item.category
            }
            
            // 상태
            binding.tvStatus.text = when (item.status) {
                        "DEFAULT" -> "판매중"
                        "IN_PROGRESS" -> "거래확정"
                        "COMPLETED" -> "거래완료"
                        else -> item.status
                    }
            
            // 이미지: imageUrls 기준으로 최대 3장 표시 (thumbnailUrl 사용 안 함)
            if (!item.imageUrls.isNullOrEmpty()) {
                val urls = item.imageUrls
                // 1번
                Glide.with(binding.ivPreview1)
                    .load(urls[0])
                    .placeholder(R.drawable.image_placeholder_bg)
                    .error(R.drawable.image_placeholder_bg)
                    .centerCrop()
                    .into(binding.ivPreview1)
                // 2번
                if (urls.size >= 2) {
                    binding.ivPreview2.visibility = View.VISIBLE
                    Glide.with(binding.ivPreview2)
                        .load(urls[1])
                        .placeholder(R.drawable.image_placeholder_bg)
                        .error(R.drawable.image_placeholder_bg)
                        .centerCrop()
                        .into(binding.ivPreview2)
                } else {
                    binding.ivPreview2.visibility = View.GONE
                }
                // 3번
                if (urls.size >= 3) {
                    binding.ivPreview3.visibility = View.VISIBLE
                    Glide.with(binding.ivPreview3)
                        .load(urls[2])
                        .placeholder(R.drawable.image_placeholder_bg)
                        .error(R.drawable.image_placeholder_bg)
                        .centerCrop()
                        .into(binding.ivPreview3)
                } else {
                    binding.ivPreview3.visibility = View.GONE
                }
            } else {
                binding.ivPreview1.setImageResource(R.drawable.image_placeholder_bg)
                binding.ivPreview2.visibility = View.GONE
                binding.ivPreview3.visibility = View.GONE
            }
            
            // 거래방법은 표시 제외
            binding.tvMethod.text = if(item.purchaseMethod == "OFFLINE") "직거래" else "택배 거래"
            
            // 단위 고정 표시 (이미지 개수 등과 무관)
            binding.tvUnit.text = "/ 개"
            
            // 북마크 상태아이콘 + 클릭 연동 (낙관적 업데이트)
            var current = item.bookmarked
            fun render() {
                binding.btnBookmark.setImageResource(if (current) R.drawable.ic_bookmark_on else R.drawable.ic_bookmark_off)
            }
            render()
            binding.btnBookmark.setOnClickListener {
                val before = current
                current = !before
                render()
                onToggleBookmark(item, before) { ok ->
                    if (!ok) {
                        current = before
                        render()
                    }
                }
            }
            
            // 아이템 클릭 리스너
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

class ItemSearchDiffCallback : DiffUtil.ItemCallback<ItemSearch>() {
    override fun areItemsTheSame(oldItem: ItemSearch, newItem: ItemSearch): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ItemSearch, newItem: ItemSearch): Boolean {
        return oldItem == newItem
    }
}
