package com.example.onenthapp

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.nwonsaved.NwonSavedRepository
import com.example.onenthapp.databinding.ActivityNwonSavedBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class NwonSavedActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NwonSavedActivity"
    }

    private lateinit var binding: ActivityNwonSavedBinding
    private lateinit var adapter: NwonSavedItemAdapter
    private val repository = NwonSavedRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "onCreate 시작")
            binding = ActivityNwonSavedBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupToolbar()
            setupRecyclerView()
            setupListeners() // 💡 이벤트 리스너 설정
            fetchNwonSavedData()

            Log.d(TAG, "onCreate 정상 종료")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate 오류: ${e.message}", e)
            Toast.makeText(this, "화면 초기화 중 오류 발생", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            adapter = NwonSavedItemAdapter()
            binding.rvItems.layoutManager = LinearLayoutManager(this)
            binding.rvItems.adapter = adapter

            Log.d(TAG, "RecyclerView 초기화 완료")
        } catch (e: Exception) {
            Log.e(TAG, "RecyclerView 설정 오류: ${e.message}", e)
        }
    }

    private fun setupListeners() {
        // 💡 거래 요약 영역 클릭 시 MyReviewActivity 이동
        binding.transactionLayout.setOnClickListener {
            startActivity(Intent(this, MyReviewActivity::class.java))
        }

        // 💡 공유 버튼 클릭 시 팝업 띄우기
        binding.btnShare.setOnClickListener {
            showShareDialog()
        }
    }

    private fun showShareDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.share_nwon_popup, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // 링크 입력창 & 복사 버튼
        val linkEditText = dialogView.findViewById<EditText>(R.id.shareLinkEditText)
        val copyButton = dialogView.findViewById<ImageButton>(R.id.copyButton)

        copyButton.setOnClickListener {
            val text = linkEditText.text.toString()
            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("공유링크", text))
            Toast.makeText(this, "링크가 복사되었습니다", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

        // 팝업 크기 강제 조정
        val widthInPx = (347 * resources.displayMetrics.density).toInt()
        val heightInPx = (202 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(widthInPx, heightInPx)

        // 배경 투명화 (둥근 테두리 유지)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun fetchNwonSavedData() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "API 요청 시작")

                val nwonSavedResponse = repository.lookNwonSaved()
                Log.d(TAG, "거래 요약 응답: 성공 여부 = ${nwonSavedResponse.isSuccessful}")
                Log.d(TAG, "응답 코드: ${nwonSavedResponse.code()}")
                Log.d(TAG, "응답 바디: ${nwonSavedResponse.body()}")

                if (nwonSavedResponse.isSuccessful && nwonSavedResponse.body()?.isSuccess == true) {
                    val result = nwonSavedResponse.body()?.result

                    result?.let {
                        Log.d(TAG, "거래 요약 데이터 수신 성공")
                        binding.dealReviewCount.text = it.totalReviewCount.toString()
                        binding.dealReviewStar.text = "★ ${String.format("%.1f", it.totalReviewRating)}"
                        binding.dealCount.text = formatNumber(it.totalDealHistory.totalDealCount)
                        binding.dealCashCount.text = formatNumber(it.totalDealHistory.totalDealAmount)
                        binding.dealNwon.text = "${formatNumber(it.savedAmount)}"
                    }
                } else {
                    val errorMsg = nwonSavedResponse.body()?.message ?: "알 수 없는 오류"
                    Log.e(TAG, "거래 요약 정보 응답 실패: $errorMsg")
                    Toast.makeText(this@NwonSavedActivity, "거래 정보 불러오기 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                }

                val itemsResponse = repository.lookMyHistoryItem("all")
                Log.d(TAG, "상품 리스트 응답: 성공 여부 = ${itemsResponse.isSuccessful}")
                Log.d(TAG, "상품 리스트 응답 코드: ${itemsResponse.code()}")
                Log.d(TAG, "상품 리스트 응답 바디: ${itemsResponse.body()}")

                if (itemsResponse.isSuccessful && itemsResponse.body()?.isSuccess == true) {
                    val items = itemsResponse.body()?.result ?: emptyList()
                    Log.d(TAG, "상품 리스트 ${items.size}개 수신")

                    items.forEachIndexed { index, item ->
                        Log.d(TAG, "상품 $index: ID=${item.itemId}, 이름=${item.itemName}, 타입=${item.itemType}")
                    }

                    if (items.isEmpty()) {
                        Log.d(TAG, "상품 리스트가 비어있음")
                        Toast.makeText(this@NwonSavedActivity, "거래한 상품이 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter.submitList(items)
                        Log.d(TAG, "어댑터에 데이터 전달 완료")
                    }
                } else {
                    val errorMsg = itemsResponse.body()?.message ?: "알 수 없는 오류"
                    Log.e(TAG, "상품 리스트 응답 실패: $errorMsg")
                    Toast.makeText(this@NwonSavedActivity, "거래한 상품 불러오기 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "fetchNwonSavedData 예외 발생", e)
                Toast.makeText(this@NwonSavedActivity, "서버 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun formatNumber(number: Int): String {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(number)
    }
}
