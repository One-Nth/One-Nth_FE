package com.example.onenthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

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

        alertDialog.show()
    }

}



