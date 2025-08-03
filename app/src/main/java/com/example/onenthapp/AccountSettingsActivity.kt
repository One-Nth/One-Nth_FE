package com.example.onenthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.onenthapp.model.MemberViewModel
import com.example.onenthapp.util.TokenManager

class AccountSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // ✅ 로그아웃 TextView 클릭 시 팝업 띄우기
        val logoutButton = findViewById<TextView>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        // ✅ 탈퇴하기 TextView 클릭 시 팝업 띄우기
        val withdrawButton = findViewById<TextView>(R.id.withdrawButton)
        withdrawButton.setOnClickListener {
            showWithdrawDialog()
        }
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.mypage_account_settings_logout_popup, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        alertDialog.show()
    }

    private fun showWithdrawDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.mypage_account_settings_withdraw_popup, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // ✅ 탈퇴하기 버튼
        val confirmButton = dialogView.findViewById<ImageButton>(R.id.btnWithdrawConfirm)
        confirmButton.setOnClickListener {
            val viewModel = ViewModelProvider(this).get(MemberViewModel::class.java)
            viewModel.withdraw()

            viewModel.withdrawStatus.observe(this) { status ->
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                if (status.contains("성공")) {
                    // ✅ 토큰 삭제 & 로그인 화면으로 이동
                    TokenManager.clearToken()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finishAffinity()
                }
            }

            alertDialog.dismiss()
        }

        // 취소 버튼
        val cancelButton = dialogView.findViewById<ImageButton>(R.id.btnWithdrawCancel)
        cancelButton.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }


}



