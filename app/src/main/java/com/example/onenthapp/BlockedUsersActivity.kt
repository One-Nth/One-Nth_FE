package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onenthapp.data.userset.UserSetRepository
import kotlinx.coroutines.launch

class BlockedUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockedUsersAdapter
    private val repository = UserSetRepository()
    private val userList = mutableListOf<BlockedUser>() // 동적으로 갱신

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        adapter = BlockedUsersAdapter(userList) { user ->
            unblockUser(user)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchBlockedUsers()
    }

    private fun fetchBlockedUsers() {
        lifecycleScope.launch {
            try {
                val response = repository.getBlockedUsers()

                Log.d("BlockedUsers", "response.isSuccessful: ${response.isSuccessful}")
                Log.d("BlockedUsers", "response.body(): ${response.body()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result

                    if (result == null) {
                        Log.e("BlockedUsers", "result가 null입니다")
                        Toast.makeText(this@BlockedUsersActivity, "서버 응답 오류 (result 없음)", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val list = result.blockedUserSummaryList.map {
                        BlockedUser(
                            userId = it.userId,
                            username = it.nickname,
                            profileImageUrl = it.profileImageUrl
                        )
                    }

                    adapter.updateList(list)

                    Log.d("BlockedUsers", "응답받은 유저 수: ${list.size}")
                    list.forEach {
                        Log.d("BlockedUsers", "userId=${it.userId}, nickname=${it.username}")
                    }
                } else {
                    Log.e("BlockedUsers", "응답 실패: ${response.errorBody()?.string()}")
                    Toast.makeText(this@BlockedUsersActivity, "차단 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BlockedUsers", "예외 발생", e)
                Toast.makeText(this@BlockedUsersActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun unblockUser(user: BlockedUser) {
        lifecycleScope.launch {
            try {
                val response = repository.unblockUser(user.userId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    userList.remove(user)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@BlockedUsersActivity, "${user.username} 차단 해제됨", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BlockedUsersActivity, "차단 해제 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BlockedUsersActivity, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
