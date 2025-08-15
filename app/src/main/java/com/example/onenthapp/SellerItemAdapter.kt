package com.example.onenthapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onenthapp.data.SellerItem
import com.example.onenthapp.databinding.ItemSearchResultBinding

class SellerItemAdapter : RecyclerView.Adapter<SellerItemAdapter.SellerItemViewHolder>() {

    private val itemList = mutableListOf<SellerItem>()

    fun updateList(newList: List<SellerItem>) {
        android.util.Log.d("SellerItemAdapter", "updateList called with ${newList.size} items")
        newList.forEachIndexed { index, item ->
            android.util.Log.d("SellerItemAdapter", "Item $index: ${item.name}, price: ${item.price}")
        }
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class SellerItemViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SellerItem) {
            // 기존 바인딩 방식 사용
            binding.bindSellerItem(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerItemViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        
        // 가로 스크롤을 위한 아이템 폭 설정
        val layoutParams = binding.root.layoutParams
        layoutParams.width = (280 * parent.context.resources.displayMetrics.density).toInt() // 280dp
        binding.root.layoutParams = layoutParams
        
        return SellerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SellerItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size
}

// SellerItem용 바인딩 확장 함수
fun ItemSearchResultBinding.bindSellerItem(item: SellerItem) {
    // 썸네일 이미지만 첫 번째 ImageView에 바인딩
    loadSellerImage(ivPreview1, item.thumbnailUrl)
    
    // 나머지 이미지뷰들은 숨김 처리
    ivPreview2.isVisible = false
    ivPreview3.isVisible = false
    
    // 상태 라벨
    tvStatus.text = when (item.status) {
        "COMPLETED" -> "거래완료"
        "IN_PROGRESS" -> "거래중"
        else -> "판매중"
    }
    
    // 카테고리 라벨
    tvCategory.text = when (item.itemCategory) {
        "ELECTRONICS" -> "전자기기"
        "HOUSEHOLD" -> "생활용품"
        "FOOD" -> "식품"
        "CLOTHING" -> "의류"
        "MISC" -> "잡화"
        else -> item.itemCategory
    }
    
    // 구매 방법
    tvMethod.text = if (item.purchaseMethod == "OFFLINE") "직거래" else "온라인"
    
    // 제목과 가격
    tvTitle.text = item.name
    tvPrice.text = "${item.price}원"
    tvUnit.text = "/ 개"
    
    // 북마크 버튼 숨김 (판매자 본인 물품이므로)
    btnBookmark.isVisible = false
}

// 이미지 로딩 헬퍼 함수
private fun loadSellerImage(imageView: ImageView, url: String?) {
    if (url.isNullOrBlank()) {
        imageView.setImageResource(R.drawable.image_placeholder_bg)
        return
    }
    Glide.with(imageView)
        .load(url)
        .placeholder(R.drawable.image_placeholder_bg)
        .error(R.drawable.image_placeholder_bg)
        .centerCrop()
        .into(imageView)
}


