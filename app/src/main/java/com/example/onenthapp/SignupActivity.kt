package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
    private var codeTimer: CountDownTimer? = null
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
        val backBtn = findViewById<ImageButton>(R.id.backButton) // 뒤로가기 버튼

        // 🔙 뒤로가기 버튼 클릭 시
        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // ✅ SignupActivity2 가기 위한 버튼 참조
        val nextButton = findViewById<ImageButton>(R.id.nextButton)

        nextButton.setOnClickListener {
            val emailMessage = viewModel.emailStatus.value
            val codeMessage = viewModel.codeStatus.value
            val password = edtPassword.text.toString()
            val passwordConfirm = edtPasswordConfirm.text.toString()

            // ✅ 문자열 비교 대신 불리언 사용
            val emailOk = viewModel.emailOk.value == true
            val codeOk  = viewModel.codeOk.value == true

            // 1️⃣ 이메일 인증 여부 체크
            if (!emailOk) {
                tvEmailError.text = "이메일 인증을 완료해주세요."
                tvEmailError.setTextColor(getColor(R.color.error_text))
                tvEmailError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 2️⃣ 인증 코드 확인 여부 체크
            if (!codeOk) {
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
            tvCodeResult.visibility = View.GONE
            edtCode.apply {
                text.clear()
                setBackgroundResource(R.drawable.edittext_border2)
                isEnabled = true
            }

            // 🔁 새로 요청하면 코드 인증은 다시 해야 하니까 false로 리셋
            viewModel.codeOk.value = false
            viewModel.email.value = edtEmail.text.toString()
            viewModel.requestCode()
        }



        viewModel.emailStatus.observe(this) { message ->
            tvEmailError.text = message
            tvEmailError.visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.emailOk.observe(this) { ok ->
            if (ok == true) {
                tvEmailError.setTextColor(getColor(R.color.main_green))
                edtEmail.setBackgroundResource(R.drawable.edittext_border2) // 초록 테두리 or 기본

                // ✅ 5분 타이머 시작
                startCodeTimer()
            } else {
                tvEmailError.setTextColor(getColor(R.color.error_text))
                edtEmail.setBackgroundResource(R.drawable.edittext_border_error) // 빨간 테두리
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

        viewModel.codeStatus.observe(this) { message ->
            if (message.isNullOrEmpty()) {
                tvCodeResult.visibility = View.GONE
                edtCode.setBackgroundResource(R.drawable.edittext_border2) // 기본 테두리
                return@observe
            }

            tvCodeResult.text = message
            tvCodeResult.visibility = View.VISIBLE

            val ok = message.contains("인증") && message.contains("완료")
            if (ok) {
                tvCodeResult.setTextColor(getColor(R.color.main_green))
                edtCode.setBackgroundResource(R.drawable.edittext_border2) // ✅ 정상
                // 인증 성공 → 타이머 멈춤
                stopCodeTimer()
                edtCode.isEnabled = false
            } else {
                tvCodeResult.setTextColor(getColor(R.color.error_text))
                edtCode.setBackgroundResource(R.drawable.edittext_border_error) // 🔴 에러
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
                    edtPasswordConfirm.setBackgroundResource(R.drawable.edittext_border2) // ✅ 정상 테두리
                } else {
                    tvPasswordConfirmError.text = "비밀번호가 동일하지 않습니다."
                    tvPasswordConfirmError.setTextColor(getColor(R.color.error_text))
                    tvPasswordConfirmError.visibility = View.VISIBLE
                    edtPasswordConfirm.setBackgroundResource(R.drawable.edittext_border_error) // 🔴 빨간 테두리
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

    private fun startCodeTimer(totalMillis: Long = 5 * 60 * 1000L) {
        val timerTv = findViewById<TextView>(R.id.timerTextView)

        // 이미 돌고 있으면 종료
        codeTimer?.cancel()

        codeTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val m = (millisUntilFinished / 1000) / 60
                val s = (millisUntilFinished / 1000) % 60
                timerTv.text = String.format("%02d:%02d", m, s)
                timerTv.setTextColor(getColor(R.color.error_text)) // 빨간색
            }

            override fun onFinish() {
                timerTv.text = "00:00"
                // 만료 안내
                val tvCodeResult = findViewById<TextView>(R.id.tvCodeResult)
                val edtCode = findViewById<EditText>(R.id.verificationCodeEditText)

                tvCodeResult.text = "인증번호가 만료되었습니다. 다시 요청해주세요."
                tvCodeResult.visibility = View.VISIBLE
                tvCodeResult.setTextColor(getColor(R.color.error_text))
                edtCode.setBackgroundResource(R.drawable.edittext_border_error)

                // ⛔ 만료 → 인증 실패 상태로 표시
                viewModel.codeOk.value = false
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
