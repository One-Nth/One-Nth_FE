package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.model.SignupViewModel
import com.example.onenthapp.model.SignupViewModelFactory

class SignupActivity : AppCompatActivity() {
    private lateinit var viewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // ✅ Repository & Factory 생성
        val repository = AuthRepository()
        val factory = SignupViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SignupViewModel::class.java]

        // ✅ 뷰 참조
        val edtEmail = findViewById<EditText>(R.id.emailEditText)
        val btnCheck = findViewById<ImageButton>(R.id.btnVerifyEmail)
        val tvEmailError = findViewById<TextView>(R.id.tvEmailError)

        val edtCode = findViewById<EditText>(R.id.verificationCodeEditText)
        val tvCodeResult = findViewById<TextView>(R.id.tvCodeResult)

        val edtPassword = findViewById<EditText>(R.id.passwordEditText)
        val edtPasswordConfirm = findViewById<EditText>(R.id.passwordConfirmEditText)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)
        val tvPasswordConfirmError = findViewById<TextView>(R.id.tvPasswordConfirmError)

        // ✅ SignupActivity2 가기 위한 버튼 참조
        val nextButton = findViewById<ImageButton>(R.id.nextButton)

        nextButton.setOnClickListener {
            val emailMessage = viewModel.emailStatus.value
            val codeMessage = viewModel.codeStatus.value
            val password = edtPassword.text.toString()
            val passwordConfirm = edtPasswordConfirm.text.toString()

            // 1️⃣ 이메일 인증 여부 체크
            if (emailMessage.isNullOrEmpty() || !emailMessage.contains("사용 가능한 아이디")) {
                tvEmailError.text = "이메일 인증을 완료해주세요."
                tvEmailError.setTextColor(getColor(R.color.error_text))
                tvEmailError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 2️⃣ 인증 코드 확인 여부 체크
            if (codeMessage.isNullOrEmpty() || !codeMessage.contains("인증 완료")) {
                tvCodeResult.text = "인증 코드를 확인해주세요."
                tvCodeResult.setTextColor(getColor(R.color.error_text))
                tvCodeResult.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 3️⃣ 비밀번호 유효성 체크
            if (!isValidPassword(password)) {
                tvPasswordError.text = "올바른 비밀번호를 입력해주세요."
                tvPasswordError.setTextColor(getColor(R.color.error_text))
                tvPasswordError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 4️⃣ 비밀번호 일치 여부 체크
            if (password != passwordConfirm) {
                tvPasswordConfirmError.text = "비밀번호가 동일하지 않습니다."
                tvPasswordConfirmError.setTextColor(getColor(R.color.error_text))
                tvPasswordConfirmError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // ✅ 모든 조건 통과 시 다음 화면으로 이동
            val intent = Intent(this, SignupActivity2::class.java)
            // SignupActivity2로 데이터 전달
            intent.putExtra("loginType", "LOCAL")
            intent.putExtra("email", edtEmail.text.toString())
            intent.putExtra("password", edtPassword.text.toString())
            intent.putExtra("confirmPassword", edtPasswordConfirm.text.toString())

            startActivity(intent)

        }

        // ✅ 이메일 인증 코드 요청
        btnCheck.setOnClickListener {
            viewModel.email.value = edtEmail.text.toString()
            viewModel.requestCode()
        }

        // ✅ 이메일 발송 결과 메시지
        viewModel.emailStatus.observe(this) { message ->
            if (message.isNullOrEmpty()) {
                tvEmailError.visibility = View.GONE
            } else {
                tvEmailError.text = message
                tvEmailError.visibility = View.VISIBLE

                if (message.contains("사용 가능한 아이디")) {
                    tvEmailError.setTextColor(getColor(R.color.main_green))
                } else {
                    tvEmailError.setTextColor(getColor(R.color.error_text))
                }
            }
        }

        // ✅ 인증코드 입력 → 6자리 입력 시 자동 검증
        edtCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length == 6) {
                    viewModel.verifyCode(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 인증 코드 검증 결과 메시지
        viewModel.codeStatus.observe(this) { message ->
            if (message.isNullOrEmpty()) {
                tvCodeResult.visibility = View.GONE
            } else {
                tvCodeResult.text = message
                tvCodeResult.visibility = View.VISIBLE

                if (message.contains("인증") && message.contains("완료")) {
                    tvCodeResult.setTextColor(getColor(R.color.main_green))
                } else {
                    tvCodeResult.setTextColor(getColor(R.color.error_text))
                }
            }
        }

        // ✅ 비밀번호 유효성 검사
        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (!isValidPassword(password)) {
                    tvPasswordError.text = "영문, 숫자, 특수문자를 포함해 10자 이상 입력하세요."
                    tvPasswordError.setTextColor(getColor(R.color.error_text))
                    tvPasswordError.visibility = View.VISIBLE
                } else {
                    tvPasswordError.text = "사용 가능한 비밀번호입니다."
                    tvPasswordError.setTextColor(getColor(R.color.main_green))
                    tvPasswordError.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 비밀번호 확인 일치 여부 검사
        edtPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = edtPassword.text.toString()
                val confirm = s.toString()

                if (confirm == password && confirm.isNotEmpty()) {
                    tvPasswordConfirmError.text = "비밀번호 확인 완료되었습니다."
                    tvPasswordConfirmError.setTextColor(getColor(R.color.main_green))
                    tvPasswordConfirmError.visibility = View.VISIBLE
                } else {
                    tvPasswordConfirmError.text = "비밀번호가 동일하지 않습니다."
                    tvPasswordConfirmError.setTextColor(getColor(R.color.error_text))
                    tvPasswordConfirmError.visibility = View.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ✅ 비밀번호 유효성 검사 함수
    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{10,}$")
        return regex.matches(password)
    }
}
