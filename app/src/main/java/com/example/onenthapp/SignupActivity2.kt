package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)

        val edtName = findViewById<EditText>(R.id.nameEditText)
        val edtNickname = findViewById<EditText>(R.id.nicknameEditText)
        val tvNicknameError = findViewById<TextView>(R.id.tvNicknameError)
        val checkBoxAgree = findViewById<CheckBox>(R.id.cbEmailAgreement)
        val btnComplete = findViewById<ImageButton>(R.id.nextButton)

        // ✅ 닉네임 유효성 검사
        edtNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nickname = s.toString()
                if (!isValidNickname(nickname)) {
                    tvNicknameError.text = "닉네임은 2~10자 이내 영어 및 숫자만 가능해요."
                    tvNicknameError.visibility = View.VISIBLE
                } else {
                    tvNicknameError.text = ""
                    tvNicknameError.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 회원가입 완료 버튼 클릭
        btnComplete.setOnClickListener {
            val name = edtName.text.toString()
            val nickname = edtNickname.text.toString()
            val agree = checkBoxAgree.isChecked

            if (name.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "이름과 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidNickname(nickname)) {
                Toast.makeText(this, "닉네임 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 회원가입 성공 시 로그인 상태 저장
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("isLoggedIn", true).apply()

            // ✅ 메인 화면으로 이동 (백스택 초기화)
            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun isValidNickname(nickname: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]{2,10}$")
        return regex.matches(nickname)
    }
}