package com.example.onenthapp.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.onenthapp.R

import android.content.Intent
import android.net.http.HttpException
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.onenthapp.MyReviewActivity
import com.example.onenthapp.NwonSavedActivity
import com.example.onenthapp.RetrofitInstance
import com.example.onenthapp.data.transaction.CancelTransactionRequest
import kotlinx.coroutines.launch

class CancelDealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_deal)

        val roomName = intent.getStringExtra("roomName")
        val isWriter = intent.getBooleanExtra("isWriter", false)  // 추가: 작성자 여부 받기
        val dealConfirmationId = intent.getIntExtra("dealConfirmationId", -1)

        // 작성자면 "판매자 측 과실" 레이아웃 숨기기
        val dealWriteLayout = findViewById<LinearLayout>(R.id.deal_write)
        if (isWriter) {
            dealWriteLayout.visibility = View.GONE
        } else {
            dealWriteLayout.visibility = View.VISIBLE
        }

        if (dealConfirmationId == -1) {
            Toast.makeText(this, "거래 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btn_review).setOnClickListener {
            val intent = Intent(this, MyReviewActivity::class.java)
            intent.putExtra("roomName", roomName)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_savings).setOnClickListener {
            val intent = Intent(this, NwonSavedActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_left).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.cancelbtn).setOnClickListener {
            val roomName = intent.getStringExtra("roomName") ?: return@setOnClickListener

            val selectedReason = when {
                findViewById<RadioButton>(R.id.radioOption1).isChecked -> "FRAUD_SUSPECTED"
                findViewById<RadioButton>(R.id.radioOption2).isChecked -> "BAD_LANGUAGE"
                findViewById<RadioButton>(R.id.radioOption3).isChecked -> "CHANGE_OF_MIND"
                findViewById<RadioButton>(R.id.radioOption4).isChecked -> "DEFECTIVE_PRODUCT"
                else -> null
            }

            if (selectedReason == null) {
                Toast.makeText(this, "취소 사유를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CancelTransactionRequest(
                dealConfirmationId = dealConfirmationId,
                cancelReason = selectedReason
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.transactionApi.cancelTransaction(roomName, request)

                    if (response.isSuccessful) {
                        Toast.makeText(this@CancelDealActivity, "거래가 취소되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = response.errorBody()?.string()
                        Toast.makeText(this@CancelDealActivity, "취소 실패: $error", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@CancelDealActivity, "에러: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}


