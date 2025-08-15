package com.example.onenthapp.feature.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.R
import com.example.onenthapp.data.map.PostMarkerPreview
import com.example.onenthapp.databinding.ItemMarkerDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class MarkerDetailAdapter(
    private val onItemClick: (PostMarkerPreview) -> Unit,
    private val onBookmarkClick: (Long, Boolean, (Boolean) -> Unit) -> Unit
) : RecyclerView.Adapter<MarkerDetailAdapter.ViewHolder>() {

    private var items: List<PostMarkerPreview> = emptyList()

    fun updateItems(newItems: List<PostMarkerPreview>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMarkerDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemMarkerDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }

            binding.btnBookmark.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    onBookmarkClick(item.id, item.scraped) { success ->
                        if (success) {
                            // 북마크 상태 업데이트
                            val newItems = items.toMutableList()
                            newItems[position] = item.copy(scraped = !item.scraped)
                            items = newItems
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }

        fun bind(item: PostMarkerPreview) {
            binding.tvPlaceName.text = item.placeName
            binding.tvTitle.text = item.title
            binding.tvAddress.text = item.address
            
            // 북마크 상태에 따라 아이콘 변경
            binding.btnBookmark.setImageResource(
                if (item.scraped) R.drawable.ic_bookmini_on else R.drawable.ic_bookmini_off
            )
            
            // 시간 표시 (상대적 시간으로 변환)
            binding.tvTime.text = getRelativeTimeString(item.createdAt)
        }

        private fun getRelativeTimeString(createdAt: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val createdDate = inputFormat.parse(createdAt)
                val currentDate = Date()
                
                val diffInMillis = currentDate.time - (createdDate?.time ?: 0)
                val diffInMinutes = diffInMillis / (1000 * 60)
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                
                when {
                    diffInMinutes < 1 -> "방금전"
                    diffInMinutes < 60 -> "${diffInMinutes}분전"
                    diffInHours < 24 -> "${diffInHours}시간전"
                    diffInDays < 7 -> "${diffInDays}일전"
                    else -> {
                        val outputFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
                        createdDate?.let { outputFormat.format(it) } ?: createdAt
                    }
                }
            } catch (e: Exception) {
                createdAt
            }
        }
    }
}
