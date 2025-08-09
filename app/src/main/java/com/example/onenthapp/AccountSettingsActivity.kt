package com.example.onenthapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.onenthapp.model.MemberViewModel
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream


class AccountSettingsActivity : AppCompatActivity() {

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadProfileImage(uri)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        val nicknameInput = findViewById<EditText>(R.id.nicknameInput)
        val profileImageView = findViewById<ImageView>(R.id.profileImage) // 레이아웃의 프로필 이미지뷰 ID 맞춰야 함
        val regionText = findViewById<TextView>(R.id.regionText)

        // 프로필 이미지 클릭 시 갤러리 열기
        profileImageView.setOnClickListener {
            pickImage.launch("image/*")
        }

        // ✅ 앱 실행 시 서버에서 프로필(닉네임 + 이미지) 가져오기
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.memberApi.getProfile()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val profile = response.body()?.result
                    nicknameInput.setText(profile?.nickname ?: "")

                    // ✅ 프로필 이미지가 있으면 Glide로 표시
                    if (!profile?.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this@AccountSettingsActivity)
                            .load(profile?.profileImageUrl)
                            .placeholder(R.drawable.avatar) // 기본 이미지
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.avatar)
                    }
                    val dong = profile?.verifiedRegionNames?.firstOrNull()
                    if (!dong.isNullOrBlank()) {
                        regionText.text = "$dong 인증 완료"
                        regionText.setTextColor(getColor(R.color.main_green))
                        regionText.visibility = View.VISIBLE
                    } else {
                        regionText.text = "인증된 지역 없음"
                        // regionText.visibility = View.GONE // 숨기려면
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AccountSettingsActivity, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val logoutButton = findViewById<TextView>(R.id.logoutButton)
        logoutButton.setOnClickListener { showLogoutDialog() }

        val withdrawButton = findViewById<TextView>(R.id.withdrawButton)
        withdrawButton.setOnClickListener { showWithdrawDialog() }

        // ✅ 비밀번호 변경
        val btnConfirmPassword = findViewById<ImageButton>(R.id.btnConfirmPassword)
        val passwordMasked = findViewById<EditText>(R.id.passwordMasked) // 첫 입력
        val newPasswordInput = findViewById<EditText>(R.id.newPasswordInput) // 두 번째 입력

        btnConfirmPassword.setOnClickListener {
            val firstPassword = passwordMasked.text.toString().trim()
            val confirmPassword = newPasswordInput.text.toString().trim()

            if (firstPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (firstPassword != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ API 호출
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.memberApi.changePassword(
                        mapOf("password" to firstPassword)
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Toast.makeText(
                            this@AccountSettingsActivity,
                            "비밀번호가 변경되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        passwordMasked.text.clear()
                        newPasswordInput.text.clear()
                    } else {
                        Toast.makeText(
                            this@AccountSettingsActivity,
                            response.body()?.message ?: "비밀번호 변경 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AccountSettingsActivity,
                        "오류 발생: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val btnNicknameEdit = findViewById<ImageButton>(R.id.btnNicknameEdit)

        btnNicknameEdit.setOnClickListener {
            val newNickname = nicknameInput.text.toString().trim()

            if (newNickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.memberApi.changeNickname(
                        mapOf("nickname" to newNickname)
                    )

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val changedName = response.body()?.result?.nickname ?: newNickname
                        Toast.makeText(
                            this@AccountSettingsActivity,
                            "닉네임이 변경되었습니다: $changedName",
                            Toast.LENGTH_SHORT
                        ).show()
                        nicknameInput.setText(changedName) // 변경된 닉네임으로 입력창 업데이트
                    } else {
                        Toast.makeText(
                            this@AccountSettingsActivity,
                            response.body()?.message ?: "닉네임 변경 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AccountSettingsActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun uploadProfileImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "profile_image.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val response = RetrofitInstance.memberApi.changeProfileImage(body)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val newUrl = response.body()?.result?.profileImageUrl

                    Toast.makeText(this@AccountSettingsActivity, "프로필 이미지가 변경되었습니다.", Toast.LENGTH_SHORT).show()

                    Glide.with(this@AccountSettingsActivity)
                        .load(newUrl)
                        .placeholder(R.drawable.avatar)
                        .circleCrop() // ✅ 원형 크롭
                        .into(findViewById(R.id.profileImage))
                } else {
                    Toast.makeText(this@AccountSettingsActivity, response.body()?.message ?: "변경 실패", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@AccountSettingsActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.mypage_account_settings_logout_popup, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // ✅ 팝업 내 로그아웃 버튼
        val confirmButton = dialogView.findViewById<ImageButton>(R.id.btnLogoutConfirm)
        confirmButton.setOnClickListener {
            TokenManager.clearAll()// 토큰 삭제
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            // ✅ SplashActivity로 이동
            startActivity(Intent(this, SplashActivity::class.java))
            finishAffinity()

            alertDialog.dismiss()
        }

        // 취소 버튼
        val cancelButton = dialogView.findViewById<ImageButton>(R.id.btnLogoutCancel)
        cancelButton.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }


    private fun showWithdrawDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.mypage_account_settings_withdraw_popup, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val confirmButton = dialogView.findViewById<ImageButton>(R.id.btnWithdrawConfirm)
        confirmButton.setOnClickListener {
            val viewModel = ViewModelProvider(this).get(MemberViewModel::class.java)
            viewModel.withdraw()

            viewModel.withdrawStatus.observe(this) { status ->
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                if (status.contains("성공")) {
                    TokenManager.clearToken()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finishAffinity()
                }
            }
            alertDialog.dismiss()
        }

        val cancelButton = dialogView.findViewById<ImageButton>(R.id.btnWithdrawCancel)
        cancelButton.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun extractDong(full: String): String? {
        // 구분자 통일 후 토큰화
        val tokens = full.replace(",", " ")
            .replace("·", " ")
            .split(" ")
            .filter { it.isNotBlank() }

        // 보통 가장 뒤에 위치 → 뒤에서부터 탐색
        return tokens.asReversed().firstOrNull { it.endsWith("동") }
            ?: tokens.asReversed().firstOrNull { it.endsWith("가") } // (옵션) 종로1가 같은 경우 대비
    }

}



