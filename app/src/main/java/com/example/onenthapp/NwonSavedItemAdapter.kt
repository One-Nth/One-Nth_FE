package com.example.onenthapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.onenthapp.data.nwonsaved.MyHistoryItem

class NwonSavedItemAdapter(
    private val onItemClick: ((MyHistoryItem) -> Unit)? = null
) : ListAdapter<MyHistoryItem, NwonSavedItemAdapter.ItemViewHolder>(DiffCallback()) {

    companion object {
        private const val TAG = "NwonSavedItemAdapter"

        class DiffCallback : DiffUtil.ItemCallback<MyHistoryItem>() {
            override fun areItemsTheSame(oldItem: MyHistoryItem, newItem: MyHistoryItem): Boolean {
                return oldItem.itemId == newItem.itemId
            }

            override fun areContentsTheSame(oldItem: MyHistoryItem, newItem: MyHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.d(TAG, "onCreateViewHolder 호출")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_history, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder 호출 - position: $position")
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d(TAG, "getItemCount: $count")
        return count
    }

    override fun submitList(list: List<MyHistoryItem>?) {
        Log.d(TAG, "submitList 호출 - 아이템 수: ${list?.size}")
        super.submitList(list)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivItemImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvDetails: TextView? = itemView.findViewById(R.id.tvItemDetail)

        init {
            Log.d("ItemViewHolder", "ViewHolder 생성됨")
            Log.d("ItemViewHolder", "ivImage 찾기: ${if (ivImage != null) "성공" else "실패"}")
            Log.d("ItemViewHolder", "tvName 찾기: ${if (tvName != null) "성공" else "실패"}")
            Log.d("ItemViewHolder", "tvDetails 찾기: ${if (tvDetails != null) "성공" else "실패"}")

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(item: MyHistoryItem) {
            Log.d("ItemViewHolder", "bind 호출 - 아이템: ${item.itemName}")

            // 상품명 설정
            tvName.text = item.itemName

            // 상품 타입 표시 (tvDetails가 있는 경우)
            tvDetails?.text = " "

            // 이미지 로드
            if (item.itemImageUrl.isNotEmpty()) {
                Log.d("ItemViewHolder", "이미지 로드 시도: ${item.itemImageUrl}")
                Glide.with(itemView.context)
                    .load(item.itemImageUrl)
                    .transform(RoundedCorners(8))
                    .placeholder(R.drawable.rectangle_11)
                    .error(R.drawable.rectangle_11)
                    .into(ivImage)
            } else {
                Log.d("ItemViewHolder", "이미지 URL이 비어있음 - 기본 이미지 사용")
                ivImage.setImageResource(R.drawable.rectangle_11)
            }
        }
    }

}