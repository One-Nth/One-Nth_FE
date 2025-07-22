package com.example.onenthapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BlockedUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockedUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)

        val userList = listOf(
            BlockedUser("user1", R.drawable.avatar),
            BlockedUser("user2", R.drawable.avatar)
        )

        adapter = BlockedUsersAdapter(userList) { user ->
            Toast.makeText(this, "${user.username} 차단 해제", Toast.LENGTH_SHORT).show()
            // TODO: 실제 차단 해제 처리
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
