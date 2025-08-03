package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.model.MemberViewModel
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch


class AccountSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

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
        val nicknameInput = findViewById<EditText>(R.id.nicknameInput)

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

    private fun showLogoutDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.mypage_account_settings_logout_popup, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
}






//        btnConfirmPassword.setOnClickListener {
//            val newPassword = newPasswordInput.text.toString().trim()
//
//            if (newPassword.isEmpty()) {
//                Toast.makeText(this, "새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            lifecycleScope.launch {
//                try {
//                    val response = RetrofitInstance.memberApi.changePassword(
//                        mapOf("password" to newPassword)
//                    )
//
//                    if (response.isSuccessful && response.body()?.isSuccess == true) {
//                        Toast.makeText(this@AccountSettingsActivity, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
//                        newPasswordInput.text.clear() // 입력창 초기화
//                    } else {
//                        Toast.makeText(
//                            this@AccountSettingsActivity,
//                            response.body()?.message ?: "비밀번호 변경 실패",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(this@AccountSettingsActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
