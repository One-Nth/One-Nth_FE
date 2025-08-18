package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.login.AuthRepository
import com.example.onenthapp.feature.login.LoginActivity
import com.example.onenthapp.model.PasswordResetViewModel
import com.example.onenthapp.viewmodel.PasswordResetViewModelFactory

class FindAccountActivity : AppCompatActivity() {

    private lateinit var resetViewModel: PasswordResetViewModel
    private var codeTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_account)

        val repo = AuthRepository()
        resetViewModel = ViewModelProvider(
            this,
            PasswordResetViewModelFactory(repo)
        )[PasswordResetViewModel::class.java]

        val backBtn = findViewById<ImageView>(R.id.backButton)
        val nameEt = findViewById<EditText>(R.id.nameEditText)
        val emailEt = findViewById<EditText>(R.id.emailEditText)
        val btnVerifyEmail = findViewById<ImageButton>(R.id.btnVerifyEmail)
        val verificationCodeEt = findViewById<EditText>(R.id.verificationCodeEditText)
        val newPasswordEt = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEt = findViewById<EditText>(R.id.passwordConfirmEditText)
        val nextBtn = findViewById<ImageButton>(R.id.nextButton)

        val tvEmailError = findViewById<TextView>(R.id.tvEmailError)
        val tvCodeResult = findViewById<TextView>(R.id.tvCodeResult)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)
        val tvPasswordConfirmError = findViewById<TextView>(R.id.tvPasswordConfirmError)

        // ✅ 뒤로가기 버튼
        backBtn.setOnClickListener { finish() }

        // ✅ 이메일 인증 요청 (이름 + 이메일 필요)
        btnVerifyEmail.setOnClickListener {
            val name = nameEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            resetViewModel.requestCode(name, email)
        }

        // ✅ 이메일 발송 결과 Observe
        resetViewModel.emailStatus.observe(this) { msg ->
            if (msg.isNullOrEmpty()) {
                tvEmailError.visibility = View.GONE
                emailEt.setBackgroundResource(R.drawable.edittext_border2)
            } else {
                tvEmailError.visibility = View.VISIBLE
                tvEmailError.text = msg

                if (msg.contains("발송") || msg.contains("완료")) {
                    tvEmailError.setTextColor(getColor(R.color.main_green))
                    emailEt.setBackgroundResource(R.drawable.edittext_border2)

                    // ⏱️ 타이머 시작 & 코드 입력창 초기화
                    verificationCodeEt.apply {
                        text?.clear()
                        isEnabled = true
                        setBackgroundResource(R.drawable.edittext_border2)
                    }
                    startCodeTimer(5 * 60 * 1000L)
                } else {
                    tvEmailError.setTextColor(getColor(R.color.error_text))
                    emailEt.setBackgroundResource(R.drawable.edittext_border_error)
                    stopCodeTimer() // 실패 시 돌고 있던 타이머 중지
                }
            }
        }


        // ✅ 인증 코드 검증 결과 Observe
        resetViewModel.codeStatus.observe(this) { msg ->
            if (msg.isNullOrEmpty()) {
                tvCodeResult.visibility = View.GONE
                verificationCodeEt.setBackgroundResource(R.drawable.edittext_border2)
            } else {
                tvCodeResult.visibility = View.VISIBLE
                tvCodeResult.text = msg

                if (msg.contains("성공") || msg.contains("완료")) {
                    tvCodeResult.setTextColor(getColor(R.color.main_green))
                    verificationCodeEt.setBackgroundResource(R.drawable.edittext_border2)
                    verificationCodeEt.isEnabled = false   // 인증 성공 시 비활성화
                    stopCodeTimer()                        // ⏹️ 타이머 정지
                } else {
                    tvCodeResult.setTextColor(getColor(R.color.error_text))
                    verificationCodeEt.setBackgroundResource(R.drawable.edittext_border_error)
                }
            }
        }

        // ✅ 인증번호 입력 시 실시간 검증
        verificationCodeEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = emailEt.text.toString().trim()
                val code = s.toString().trim()

                // 6자리 이상 입력 시 자동 검증
                if (code.length >= 6) {
                    resetViewModel.verifyCode(email, code)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // ✅ 비밀번호 입력 시 실시간 유효성 검사
        newPasswordEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (isValidPassword(password)) {
                    tvPasswordError.text = "사용 가능한 비밀번호입니다."
                    tvPasswordError.setTextColor(getColor(R.color.main_green))
                    newPasswordEt.setBackgroundResource(R.drawable.edittext_border2) // 정상
                } else {
                    tvPasswordError.text = "영문, 숫자, 특수문자를 포함해 10자 이상 입력하세요."
                    tvPasswordError.setTextColor(getColor(R.color.error_text))
                    newPasswordEt.setBackgroundResource(R.drawable.edittext_border_error) // 빨간 테두리
                }
                tvPasswordError.visibility = View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 비밀번호 확인 입력 시 실시간 일치 여부 검사
        confirmPasswordEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = newPasswordEt.text.toString()
                val confirm = s.toString()

                if (confirm == password && confirm.isNotEmpty()) {
                    tvPasswordConfirmError.text = "비밀번호 확인 완료되었습니다."
                    tvPasswordConfirmError.setTextColor(getColor(R.color.main_green))
                    confirmPasswordEt.setBackgroundResource(R.drawable.edittext_border2) // 정상
                } else {
                    tvPasswordConfirmError.text = "비밀번호가 동일하지 않습니다."
                    tvPasswordConfirmError.setTextColor(getColor(R.color.error_text))
                    confirmPasswordEt.setBackgroundResource(R.drawable.edittext_border_error) // 빨간 테두리
                }
                tvPasswordConfirmError.visibility = View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        // ✅ 비밀번호 재설정 버튼 클릭
        nextBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val newPw = newPasswordEt.text.toString()
            val confirmPw = confirmPasswordEt.text.toString()

            tvPasswordError.visibility = View.GONE
            tvPasswordConfirmError.visibility = View.GONE

            if (!isValidPassword(newPw)) {
                tvPasswordError.visibility = View.VISIBLE
                tvPasswordError.text = "비밀번호 규칙(영문+숫자+특수문자, 10자 이상)을 확인해주세요."
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                tvPasswordConfirmError.visibility = View.VISIBLE
                tvPasswordConfirmError.text = "비밀번호가 일치하지 않습니다."
                return@setOnClickListener
            }

            resetViewModel.resetPassword(email, newPw)
        }

        resetViewModel.resetStatus.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                Log.d("DEBUG", "ResetStatus message: $it")

                when {
                    it.contains("기존") && it.contains("비밀번호") -> {
                        tvPasswordError.text = "기존 비밀번호와 동일합니다. 다른 비밀번호를 입력해주세요."
                        tvPasswordError.setTextColor(getColor(R.color.error_text))
                        tvPasswordError.visibility = View.VISIBLE
                    }
                    it.contains("성공") || it.contains("완료") || it.contains("재설정") -> {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }


    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{10,}$")
        return regex.matches(password)
    }

    private fun startCodeTimer(totalMillis: Long = 5 * 60 * 1000L) {
        val timerTv = findViewById<TextView>(R.id.timerTextView)

        codeTimer?.cancel()

        codeTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(ms: Long) {
                val m = (ms / 1000) / 60
                val s = (ms / 1000) % 60
                timerTv.text = String.format("%02d:%02d", m, s)
                timerTv.setTextColor(getColor(R.color.error_text)) // 빨강
            }
            override fun onFinish() {
                timerTv.text = "00:00"
                val tvCodeResult = findViewById<TextView>(R.id.tvCodeResult)
                val codeEt = findViewById<EditText>(R.id.verificationCodeEditText)

                tvCodeResult.text = "인증번호가 만료되었습니다. 다시 요청해주세요."
                tvCodeResult.visibility = View.VISIBLE
                tvCodeResult.setTextColor(getColor(R.color.error_text))
                codeEt.setBackgroundResource(R.drawable.edittext_border_error)
                codeEt.isEnabled = true   // 재입력 가능하게
            }
        }.start()
    }

    private fun stopCodeTimer() {
        codeTimer?.cancel()
        codeTimer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCodeTimer()
    }


}

