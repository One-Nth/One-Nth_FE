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
    private val onDeleteClick: (Int, String) -> Unit,
    private val onToggleClick: (Int, String, Boolean) -> Unit,
    private val isEditModeProvider: () -> Boolean
) : ListAdapter<KeywordAlertSummary, KeywordAdapter.KeywordViewHolder>(KeywordDiffCallback()) {

    companion object {
        private const val TAG = "KeywordAdapter"
    }

    inner class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keywordText: TextView = itemView.findViewById(R.id.alertKeywordText)
        val keywordIcon: ImageView = itemView.findViewById(R.id.alertIcon)

        fun bind(keyword: KeywordAlertSummary) {
            Log.d(TAG, "Binding keyword: ${keyword.keyword}, enabled: ${keyword.enabled}")
            try {
                keywordText.text = "# ${keyword.keyword}"

                // 알림 상태에 따른 아이콘 변경
                if (keyword.enabled) {
                    keywordIcon.setImageResource(R.drawable.ic_alarmbell)
                    keywordIcon.alpha = 1.0f
                } else {
                    keywordIcon.setImageResource(R.drawable.ic_alarmbell)
                    keywordIcon.alpha = 0.5f
                }

                // 편집 모드에 따른 처리
                val editMode = isEditModeProvider()
                Log.d(TAG, "Edit mode: $editMode")

                if (editMode) {
                    // 편집 모드일 때는 클릭으로 삭제 선택
                    itemView.setOnClickListener {
                        Log.d(TAG, "Delete clicked for keyword: ${keyword.keyword}")
                        onDeleteClick(keyword.keywordAlertId, keyword.keywordAlertType)
                    }
                    itemView.background = itemView.context.getDrawable(R.drawable.edittext_border2) // 선택 가능한 상태 표시
                } else {
                    // 일반 모드일 때는 알림 아이콘 클릭으로 on/off 토글
                    keywordIcon.setOnClickListener {
                        Log.d(TAG, "Toggle clicked for keyword: ${keyword.keyword}, current enabled: ${keyword.enabled}")
                        onToggleClick(keyword.keywordAlertId, keyword.keywordAlertType, !keyword.enabled)
                    }
                    itemView.setOnClickListener(null)
                    itemView.background = itemView.context.getDrawable(R.drawable.edittext_border2)
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
}