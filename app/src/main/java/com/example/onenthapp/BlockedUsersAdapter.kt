package com.example.onenthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BlockedUsersAdapter(
    private var userList: List<BlockedUser>,
    private val onUnblockClick: (BlockedUser) -> Unit
) : RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder>() {

    fun updateList(newList: List<BlockedUser>) {
        userList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val btnUnblock: ImageButton = view.findViewById(R.id.btnUnblock)

            fun bind(user: BlockedUser) {
                userName.text = user.username

                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.profile_base)   // 로딩 중 표시
                    .error(R.drawable.profile_base)         // 실패 시 표시
                    .circleCrop()                     // ★ 동그랗게 자르기
                    .into(userImage)

                btnUnblock.setOnClickListener { onUnblockClick(user) }
            }

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_users, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
    }
}
