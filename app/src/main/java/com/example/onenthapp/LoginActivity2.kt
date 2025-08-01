package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val nicknameEt = findViewById<EditText>(R.id.nicknameEditText)
        val loginBtn = findViewById<ImageButton>(R.id.nextButton)

        loginBtn.setOnClickListener {
            val nickname = nicknameEt.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(this, "환영합니다, $nickname 님!", Toast.LENGTH_SHORT).show()

                // ✅ MainActivity로 이동
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("nickname", nickname) // 🔹 닉네임을 다음 화면으로 전달
                startActivity(intent)
                finish()
            }
        }
    }
}
