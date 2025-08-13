package com.example.onenthapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.data.userset.UserSetRepository
import com.example.onenthapp.databinding.ActivityAlertSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertSettingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AlertSettingsActivity"
    }
    private lateinit var binding: ActivityAlertSettingsBinding
    private val repository = UserSetRepository()

    private val keywordIdsToDelete = mutableSetOf<Pair<Int, String>>() // ID와 타입을 함께 저장
    private var isEditMode = false

    private lateinit var keywordAdapter: KeywordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        try {
            binding = ActivityAlertSettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "Binding successful")

            setupViews()
            setupRecyclerView()
            loadUserSettings()

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "화면 초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViews() {
        Log.d(TAG, "setupViews started")

        try {
            // 뒤로가기
            binding.backButton.setOnClickListener {
                Log.d(TAG, "Back button clicked")
                finish()
            }

            // 수정 버튼
            binding.modifyButton.setOnClickListener {
                Log.d(TAG, "Modify button clicked")
                toggleEditMode(true)
            }

            // 확인 버튼 (삭제 실행)
            binding.btnConfirmDelete.setOnClickListener {
                Log.d(TAG, "Confirm delete button clicked")
                deleteSelectedKeywords()
            }

            // 지역 키워드 등록 버튼
            binding.btnRegisterLocation.setOnClickListener {
                Log.d(TAG, "Register location button clicked")
                registerRegionKeyword()
            }

            // 상품 키워드 등록 버튼
            binding.btnRegisterLocation2.setOnClickListener {
                Log.d(TAG, "Register product button clicked")
                registerProductKeyword()
            }

            // 스크랩 알림 스위치
            binding.alertSwitch1.setOnCheckedChangeListener { _, isChecked ->
                Log.d(TAG, "Scrap alert switch changed to: $isChecked")
                updateScrapAlert(isChecked)
            }

            // 리뷰 알림 스위치
            binding.alertSwitch2.setOnCheckedChangeListener { _, isChecked ->
                Log.d(TAG, "Review alert switch changed to: $isChecked")
                updateReviewAlert(isChecked)
            }

            Log.d(TAG, "setupViews completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupViews: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView started")

        try {
            keywordAdapter = KeywordAdapter(
                onDeleteClick = { id, type ->
                    Log.d(TAG, "Delete clicked for keyword id: $id, type: $type")
                    if (isEditMode) {
                        keywordIdsToDelete.add(Pair(id, type))
                        Log.d(TAG, "Added to delete list. Total items to delete: ${keywordIdsToDelete.size}")
                        // 시각적 피드백을 위해 아이템을 회색으로 표시하거나 체크박스 추가 가능
                    }
                },
                onToggleClick = { id, type, newState ->
                    Log.d(TAG, "Toggle clicked for keyword id: $id, type: $type, newState: $newState")
                    toggleKeywordAlert(id, type, newState)
                },
                isEditModeProvider = { isEditMode }
            )

            binding.keywordRecyclerView.adapter = keywordAdapter
            binding.keywordRecyclerView.layoutManager = LinearLayoutManager(this)

            Log.d(TAG, "RecyclerView setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupRecyclerView: ${e.message}", e)
        }
    }

    private fun loadUserSettings() {
        Log.d(TAG, "loadUserSettings started")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Making API call to getUserSettings")
                val response = repository.getUserSettings()
                Log.d(TAG, "API response received. IsSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Response body isSuccess: ${body?.isSuccess}")
                    Log.d(TAG, "Response body message: ${body?.message}")
                    Log.d(TAG, "Response body code: ${body?.code}")

                    if (body?.isSuccess == true) {
                        val userSettings = body.result
                        Log.d(TAG, "UserSettings received:")
                        Log.d(TAG, "- Scrap alert enabled: ${userSettings.scrapAlertSummary?.enabled}")
                        Log.d(TAG, "- Review alert enabled: ${userSettings.reviewAlertSummary?.enabled}")
                        Log.d(TAG, "- Keyword alerts count: ${userSettings.keywordAlertSummaryList.size}")

                        userSettings.keywordAlertSummaryList.forEachIndexed { index, keyword ->
                            Log.d(TAG, "Keyword $index: ${keyword.keyword} (${keyword.keywordAlertType}) - enabled: ${keyword.enabled}")
                        }

                        withContext(Dispatchers.Main) {
                            binding.alertSwitch1.isChecked = userSettings.scrapAlertSummary?.enabled ?: false
                            binding.alertSwitch2.isChecked = userSettings.reviewAlertSummary?.enabled ?: false

                            keywordAdapter.submitList(userSettings.keywordAlertSummaryList)
                            Log.d(TAG, "UI updated successfully")
                        }

                    } else {
                        Log.e(TAG, "API returned success=false: ${body?.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AlertSettingsActivity, "설정을 불러오는데 실패했습니다: ${body?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AlertSettingsActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadUserSettings: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun registerRegionKeyword() {
        Log.d(TAG, "registerRegionKeyword started")

        val keyword = binding.alertLocationText.text.toString().trim()
        Log.d(TAG, "Input keyword: '$keyword'")

        if (keyword.isEmpty()) {
            Log.d(TAG, "Keyword is empty")
            Toast.makeText(this, "키워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 현재 API에서는 지역 ID로 등록하는 방식이므로,
        // 실제 구현에서는 지역명을 ID로 변환하는 로직이 필요합니다.
        // 여기서는 예시로 1번 지역으로 등록
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Making API call to register region keyword")
                val response = repository.registerRegionKeyword(1) // 임시 지역 ID
                Log.d(TAG, "Region keyword API response: isSuccessful=${response.isSuccessful}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d(TAG, "Region keyword response body: isSuccess=${body?.isSuccess}, message=${body?.message}")

                        if (body?.isSuccess == true) {
                            binding.alertLocationText.text.clear()
                            Toast.makeText(this@AlertSettingsActivity, "지역 키워드가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            loadUserSettings() // 목록 새로고침
                        } else {
                            Toast.makeText(this@AlertSettingsActivity, "등록에 실패했습니다: ${body?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "HTTP error in region keyword registration: ${response.code()}")
                        Toast.makeText(this@AlertSettingsActivity, "등록에 실패했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in registerRegionKeyword: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun registerProductKeyword() {
        Log.d(TAG, "registerProductKeyword started")

        val keyword = binding.alertLocationText2.text.toString().trim()
        Log.d(TAG, "Input product keyword: '$keyword'")

        if (keyword.isEmpty()) {
            Log.d(TAG, "Product keyword is empty")
            Toast.makeText(this, "키워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Making API call to register product keyword")
                val response = repository.registerKeyword(keyword)
                Log.d(TAG, "Product keyword API response: isSuccessful=${response.isSuccessful}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d(TAG, "Product keyword response body: isSuccess=${body?.isSuccess}, message=${body?.message}")

                        if (body?.isSuccess == true) {
                            binding.alertLocationText2.text.clear()
                            Toast.makeText(this@AlertSettingsActivity, "상품 키워드가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            loadUserSettings() // 목록 새로고침
                        } else {
                            Toast.makeText(this@AlertSettingsActivity, "등록에 실패했습니다: ${body?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "HTTP error in product keyword registration: ${response.code()}")
                        Toast.makeText(this@AlertSettingsActivity, "등록에 실패했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in registerProductKeyword: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateScrapAlert(isEnabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.updateScrapAlert(isEnabled)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Toast.makeText(this@AlertSettingsActivity,
                            if (isEnabled) "스크랩 알림이 활성화되었습니다." else "스크랩 알림이 비활성화되었습니다.",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        // 실패 시 스위치 상태 되돌리기
                        binding.alertSwitch1.isChecked = !isEnabled
                        Toast.makeText(this@AlertSettingsActivity, "설정 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.alertSwitch1.isChecked = !isEnabled
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateReviewAlert(isEnabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.updateReviewAlert(isEnabled)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Toast.makeText(this@AlertSettingsActivity,
                            if (isEnabled) "리뷰 알림이 활성화되었습니다." else "리뷰 알림이 비활성화되었습니다.",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        binding.alertSwitch2.isChecked = !isEnabled
                        Toast.makeText(this@AlertSettingsActivity, "설정 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.alertSwitch2.isChecked = !isEnabled
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleKeywordAlert(keywordAlertId: Int, alertType: String, isEnabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = when (alertType) {
                    "REGION" -> repository.toggleRegionKeyword(keywordAlertId, isEnabled)
                    "PRODUCT" -> repository.toggleKeyword(keywordAlertId, isEnabled)
                    else -> return@launch
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Toast.makeText(this@AlertSettingsActivity,
                            if (isEnabled) "알림이 활성화되었습니다." else "알림이 비활성화되었습니다.",
                            Toast.LENGTH_SHORT).show()
                        loadUserSettings() // 목록 새로고침
                    } else {
                        Toast.makeText(this@AlertSettingsActivity, "설정 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable
        binding.btnConfirmDelete.visibility = if (enable) View.VISIBLE else View.GONE
        keywordAdapter.notifyDataSetChanged() // 모드 변경 시 UI 갱신
        if (!enable) {
            keywordIdsToDelete.clear()
        }
    }

    private fun deleteSelectedKeywords() {
        if (keywordIdsToDelete.isEmpty()) {
            Toast.makeText(this, "삭제할 키워드를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val productKeywords = keywordIdsToDelete.filter { it.second == "PRODUCT" }.map { it.first }
                val regionKeywords = keywordIdsToDelete.filter { it.second == "REGION" }.map { it.first }

                val response = repository.deleteKeywords(productKeywords, regionKeywords)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        Toast.makeText(this@AlertSettingsActivity, "선택한 키워드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        toggleEditMode(false)
                        loadUserSettings()
                    } else {
                        Toast.makeText(this@AlertSettingsActivity, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}