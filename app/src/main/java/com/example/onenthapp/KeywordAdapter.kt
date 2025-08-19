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
import com.example.onenthapp.data.userset.KeywordAlertSummary

class KeywordAdapter(
    private val onDeleteClick: (id: Int, type: String, position: Int) -> Unit,
    private val onToggleClick: (id: Int, type: String, newState: Boolean) -> Unit,
    private val isEditModeProvider: () -> Boolean
) : ListAdapter<KeywordAlertSummary, KeywordAdapter.KeywordViewHolder>(KeywordDiffCallback()) {

    companion object {
        private const val TAG = "KeywordAdapter"
    }

    // ✅ 토글 성공 후 한 아이템의 enabled만 갱신
    fun updateEnabledById(id: Int, type: String, enabled: Boolean) {
        val idx = currentList.indexOfFirst {
            it.keywordAlertId == id && it.keywordAlertType.equals(type, true)
        }
        if (idx != -1) {
            val newList = currentList.toMutableList()
            val old = newList[idx]
            newList[idx] = old.copy(enabled = enabled)
            submitList(newList)
        }
    }

    /** 현재 리스트에서 안전하게 아이템 얻기 */
    fun getItemAt(position: Int): KeywordAlertSummary? = currentList.getOrNull(position)

    /** 현재 리스트에서 해당 position 즉시 제거 후 submitList() */
    fun removeAt(position: Int): KeywordAlertSummary? {
        val list = currentList.toMutableList()
        if (position !in list.indices) return null
        val removed = list.removeAt(position)
        submitList(list)
        return removed
    }

    /** 해당 위치에 아이템 복구(롤백) */
    fun insertAt(position: Int, item: KeywordAlertSummary) {
        val list = currentList.toMutableList()
        val p = position.coerceIn(0, list.size)
        list.add(p, item)
        submitList(list)
    }

    inner class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val keywordText: TextView = itemView.findViewById(R.id.alertKeywordText)
        private val keywordIcon: ImageView = itemView.findViewById(R.id.alertIcon)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon) // 추가

        fun bind(keyword: KeywordAlertSummary) {
            Log.d(TAG, "Binding keyword: ${keyword.keyword}, enabled: ${keyword.enabled}")
            try {
                keywordText.text = "# ${keyword.keyword}"

                // 알림 상태에 따른 아이콘 투명도 처리
                keywordIcon.setImageResource(R.drawable.ic_alarmbell)


                // ✅ enabled 상태에 따라 아이콘 변경
                val iconRes = if (keyword.enabled) {
                    R.drawable.ic_alarmbell   // 켜짐 아이콘
                } else {
                    R.drawable.ic_alarmbell_off  // 꺼짐 아이콘
                }
                keywordIcon.setImageResource(iconRes)

                val editMode = isEditModeProvider()
                Log.d(TAG, "Edit mode: $editMode")

                if (editMode) {
                    // 편집 모드: 삭제 아이콘 표시, 알림 아이콘 비활성화
                    deleteIcon.visibility = View.VISIBLE
                    keywordIcon.visibility=View.GONE

                    deleteIcon.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                        onDeleteClick(keyword.keywordAlertId, keyword.keywordAlertType, pos)
                    }
                    keywordIcon.setOnClickListener(null)

                } else {
                    // 일반 모드: 삭제 아이콘 숨기고, 알림 아이콘 토글 기능 활성화
                    deleteIcon.visibility = View.GONE
                    keywordIcon.visibility=View.VISIBLE
                    keywordIcon.setOnClickListener {
                        onToggleClick(keyword.keywordAlertId, keyword.keywordAlertType, !keyword.enabled)
                    }
                    deleteIcon.setOnClickListener(null)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error binding keyword: ${e.message}", e)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        Log.d(TAG, "Creating view holder")
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alert_keyword, parent, false)
            KeywordViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view holder: ${e.message}", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        Log.d(TAG, "Binding view holder at position: $position")
        try {
            holder.bind(getItem(position))
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d(TAG, "Item count: $count")
        return count
    }

    class KeywordDiffCallback : DiffUtil.ItemCallback<KeywordAlertSummary>() {
        override fun areItemsTheSame(oldItem: KeywordAlertSummary, newItem: KeywordAlertSummary): Boolean {
            return oldItem.keywordAlertId == newItem.keywordAlertId
        }

        override fun areContentsTheSame(oldItem: KeywordAlertSummary, newItem: KeywordAlertSummary): Boolean {
            return oldItem == newItem
        }
    }
    // KeywordAdapter 안
    fun currentItems(): List<KeywordAlertSummary> = currentList.toList()

}
