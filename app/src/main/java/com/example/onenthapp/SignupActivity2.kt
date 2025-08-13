package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.model.KakaoLoginModelFactory
import com.example.onenthapp.model.KakaoViewModel
import com.example.onenthapp.model.SignupViewModel
import com.example.onenthapp.model.SignupViewModelFactory



class SignupActivity2 : AppCompatActivity() {

    private lateinit var signupVm: SignupViewModel   // LOCAL 전용
    private lateinit var kakaoVm: KakaoViewModel     // KAKAO 전용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup2)

        // ViewModels
        signupVm = ViewModelProvider(
            this,
            SignupViewModelFactory(AuthRepository())
        )[SignupViewModel::class.java]

        kakaoVm = ViewModelProvider(
            this,
            KakaoLoginModelFactory(AuthRepository())
        )[KakaoViewModel::class.java]


        val edtName = findViewById<EditText>(R.id.nameEditText)
        val edtNickname = findViewById<EditText>(R.id.nicknameEditText)
        val tvNicknameError = findViewById<TextView>(R.id.tvNicknameError)
        val edtRegion = findViewById<EditText>(R.id.locationEditText)
        val checkBoxAgree = findViewById<CheckBox>(R.id.cbEmailAgreement)
        val btnComplete = findViewById<ImageButton>(R.id.nextButton)

        val loginType = intent.getStringExtra("loginType") ?: "LOCAL"
        val isKakao = "KAKAO".equals(loginType, ignoreCase = true)

        val email = intent.getStringExtra("email") ?: ""
        val prefillName = intent.getStringExtra("prefillName") ?: ""
        val prefillNick = intent.getStringExtra("prefillNick") ?: ""
        val serialId = intent.getStringExtra("serialId") ?: ""  // KAKAO 전용

        val password = intent.getStringExtra("password") ?: ""  // LOCAL 전용
        val confirmPassword = intent.getStringExtra("confirmPassword") ?: password

        edtName.setText(prefillName)
        edtNickname.setText(prefillNick)

        // 닉네임 유효성
        edtNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nick = s.toString()
                if (!isValidNickname(nick)) {
                    tvNicknameError.text = "닉네임은 2~10자, 영문/숫자만 가능해요."
                    tvNicknameError.setTextColor(ContextCompat.getColor(this@SignupActivity2, R.color.error_text))
                    tvNicknameError.visibility = View.VISIBLE

                    // 빨간 테두리 적용
                    edtNickname.background = ContextCompat.getDrawable(this@SignupActivity2, R.drawable.edittext_border_error)
                } else {
                    tvNicknameError.text = ""
                    tvNicknameError.visibility = View.GONE

                    // 기본 테두리로 복구
                    edtNickname.background = ContextCompat.getDrawable(this@SignupActivity2, R.drawable.edittext_border2)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        btnComplete.setOnClickListener {
            btnComplete.isEnabled = false

            val name = edtName.text.toString().trim()
            val nickname = edtNickname.text.toString().trim()
            val region = edtRegion.text.toString().trim()
            val marketingAgree = checkBoxAgree.isChecked

            if (name.isBlank() || nickname.isBlank() || region.isBlank()) {
                toast("모든 정보를 입력해주세요.")
                btnComplete.isEnabled = true
                return@setOnClickListener
            }
            if (!isValidNickname(nickname)) {
                toast("닉네임은 2~10자, 영문/숫자만 가능해요.")
                btnComplete.isEnabled = true
                return@setOnClickListener
            }

            if (isKakao) {
                if (serialId.isBlank()) {
                    toast("카카오 식별자(serialId)가 없습니다. 다시 시도해주세요.")
                    btnComplete.isEnabled = true
                    return@setOnClickListener
                }
                kakaoVm.kakaoSignup(
                    email = email,
                    socialId = serialId,   // ← 중요
                    name = name,
                    nickname = nickname,
                    regionName = region,
                    marketingAgree = marketingAgree
                )
            } else {
                if (email.isBlank() || password.isBlank()) {
                    toast("이메일/비밀번호가 누락되었습니다.")
                    btnComplete.isEnabled = true
                    return@setOnClickListener
                }
                signupVm.localSignup(
                    name = name,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    nickname = nickname,
                    regionName = region,
                    marketingAgree = marketingAgree
                )
            }
        }

        // 결과 옵저버 (둘 다 동일하게 처리)
        val onMessage: (String?) -> Unit = { msg ->
            if (!msg.isNullOrBlank()) toast(msg)
            btnComplete.isEnabled = true
        }
        val onSuccess: (Boolean?) -> Unit = { ok ->
            if (ok == true) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }

        signupVm.signupStatus.observe(this, onMessage)
        signupVm.signupSuccess.observe(this, onSuccess)

        kakaoVm.signupStatus.observe(this, onMessage)
        kakaoVm.signupSuccess.observe(this, onSuccess)
    }

    private fun isValidNickname(nickname: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]{2,10}$")
        return regex.matches(nickname)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
