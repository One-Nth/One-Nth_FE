package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.model.SignupViewModel
import com.example.onenthapp.model.SignupViewModelFactory

class SignupActivity2 : AppCompatActivity() {

    private lateinit var viewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)

        // ✅ ViewModel 초기화
        viewModel = ViewModelProvider(
            this,
            SignupViewModelFactory(AuthRepository())
        )[SignupViewModel::class.java]

        val edtName = findViewById<EditText>(R.id.nameEditText)
        val edtNickname = findViewById<EditText>(R.id.nicknameEditText)
        val tvNicknameError = findViewById<TextView>(R.id.tvNicknameError)
        val edtRegion = findViewById<EditText>(R.id.locationEditText)
        val checkBoxAgree = findViewById<CheckBox>(R.id.cbEmailAgreement)
        val btnComplete = findViewById<ImageButton>(R.id.nextButton)

        // ✅ 1번째 화면에서 전달받은 이메일 / 비밀번호 가져오기
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val confirmPassword = intent.getStringExtra("confirmPassword") ?: ""

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
            val region = edtRegion.text.toString()
            val agree = checkBoxAgree.isChecked

            if (name.isEmpty() || nickname.isEmpty() || region.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidNickname(nickname)) {
                Toast.makeText(this, "닉네임 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 서버에 회원가입 요청
            viewModel.signup(
                name = name,
                email = email,
                password = password,
                confirmPassword = password,
                nickname = nickname,
                regionName = region,
                marketingAgree = agree
            )
        }

        // ✅ 회원가입 결과 관찰
        viewModel.signupStatus.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            if (message?.contains("회원가입 성공") == true) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun isValidNickname(nickname: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]{2,10}$")
        return regex.matches(nickname)
    }
}
