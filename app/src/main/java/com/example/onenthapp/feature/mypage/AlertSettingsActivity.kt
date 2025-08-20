package com.example.onenthapp.feature.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onenthapp.KeywordAdapter
import com.example.onenthapp.RegionSuggestionAdapter
import com.example.onenthapp.data.map.MyRegionRepository
import com.example.onenthapp.data.map.SimpleRegion
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
    private val regionRepository = MyRegionRepository()

    private val keywordIdsToDelete = mutableSetOf<Pair<Int, String>>() // ID와 타입을 함께 저장
    private var isEditMode = false
    private var isSaving = false

    private lateinit var keywordAdapter: KeywordAdapter
    private lateinit var regionSuggestionAdapter: RegionSuggestionAdapter
    private var oneToast: Toast? = null
    // Activity 상단 멤버로: 삭제 후보(복구용 데이터 포함)
    private val pendingRemovals = mutableListOf<RemovedItem>()
    
    // 지역 검색 관련 변수
    private var selectedRegion: SimpleRegion? = null
    private var currentSuggestions = mutableListOf<SimpleRegion>()
    private var currentPage = 0
    private var isLastPage = true
    private var currentKeyword = ""
    private val pageSize = 10

    data class RemovedItem(
        val id: Int,
        val type: String, // "PRODUCT" | "REGION"
        val position: Int,
        val item: com.example.onenthapp.data.userset.KeywordAlertSummary
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        try {
            binding = ActivityAlertSettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "Binding successful")

            setupViews()
            setupRecyclerView()
            setupRegionSearch()
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
//            binding.btnConfirmDelete.setOnClickListener {
//                Log.d(TAG, "Confirm delete button clicked")
//                deleteSelectedKeywords()
//            }

            binding.btnConfirmDelete.setOnClickListener {
                if (isSaving) return@setOnClickListener
                if (pendingRemovals.isEmpty()) {
                    showToast("변경 사항이 없습니다.")
                    toggleEditMode(false)
                    return@setOnClickListener
                }

                isSaving = true
                binding.btnConfirmDelete.isEnabled = false

                // ✨ 현재 화면에 남아있는 아이템으로 '유지할' 목록 만들기
                val current = keywordAdapter.currentItems()  // 어댑터에 헬퍼 추가 (아래 3번 참고)
                val keepProducts = current.filter { it.keywordAlertType == "PRODUCT" }.map { it.keywordAlertId }
                val keepRegions  = current.filter { it.keywordAlertType == "REGION"  }.map { it.keywordAlertId }

                lifecycleScope.launch {
                    try {
                        val res = repository.deleteKeywords(keepProducts, keepRegions) // 서버 스펙: 유지목록
                        if (res.isSuccessful && res.body()?.isSuccess == true) {
                            showToast("삭제가 완료되었습니다.")
                            toggleEditMode(false)
                            loadUserSettings() // 서버 최신값으로 동기화
                        } else {
                            val msg = parseErrorMessage(res.errorBody()?.string()) ?: "삭제에 실패했습니다. (${res.code()})"
                            showToast(msg)
                            rollbackRemovals()
                            loadUserSettings()
                        }
                    } catch (e: Exception) {
                        showToast("네트워크 오류: ${e.message}")
                        rollbackRemovals()
                        loadUserSettings()
                    } finally {
                        isSaving = false
                        binding.btnConfirmDelete.isEnabled = true
                    }
                }
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

            // 게시글 댓글 알림 스위치
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

    private fun rollbackRemovals() {
        if (pendingRemovals.isEmpty()) return
        // 원래 순서에 가까워지도록 뒤에서부터 삽입
        val toRestore = pendingRemovals.sortedByDescending { it.position }
        toRestore.forEach { r ->
            keywordAdapter.insertAt(r.position, r.item)
        }
        pendingRemovals.clear()
    }

    private fun setupRecyclerView() {
        keywordAdapter = KeywordAdapter(
            onDeleteClick = { id, type, position ->
                // (이건 기존대로) UI에서만 제거하고 완료 버튼에서 일괄 PATCH
                val snapshot = keywordAdapter.getItemAt(position)
                keywordAdapter.removeAt(position)
                snapshot?.let { pendingRemovals += RemovedItem(id, type, position, it) }
            },
            onToggleClick = { id, type, newState ->
                toggleKeywordAlert(id, type, newState) // ✅ 3개 인자
            },
            isEditModeProvider = { isEditMode }
        )


        binding.keywordRecyclerView.apply {
            adapter = keywordAdapter
            layoutManager = LinearLayoutManager(this@AlertSettingsActivity)
        }
    }

    private fun setupRegionSearch() {
        // 지역 검색 결과 어댑터 설정
        regionSuggestionAdapter = RegionSuggestionAdapter(
            onClick = { region ->
                selectedRegion = region
                binding.alertLocationText.setText(region.regionName)
                binding.cardRegionSuggestions.visibility = View.GONE
            },
            onEndReached = { loadMoreRegions() }
        )
        
        binding.rvRegionSuggestions.apply {
            adapter = regionSuggestionAdapter
            layoutManager = LinearLayoutManager(this@AlertSettingsActivity)
        }
        
        // 지역 검색 입력 텍스트 변경 리스너
        binding.alertLocationText.addTextChangedListener { s ->
            val query = s?.toString()?.trim().orEmpty()
            if (query.isEmpty()) {
                currentSuggestions.clear()
                regionSuggestionAdapter.submitList(emptyList())
                binding.cardRegionSuggestions.visibility = View.GONE
                selectedRegion = null
            } else {
                startSearch(query)
            }
        }
    }

    private fun startSearch(newKeyword: String) {
        currentKeyword = newKeyword.trim()
        if (currentKeyword.isEmpty()) {
            currentSuggestions.clear()
            regionSuggestionAdapter.submitList(emptyList())
            binding.cardRegionSuggestions.visibility = View.GONE
            return
        }
        currentPage = 0
        searchRegions(currentKeyword, currentPage)
    }

    private fun searchRegions(keyword: String, page: Int) {
        lifecycleScope.launch {
            try {
                val (regions, pagination) = regionRepository.searchRegions(keyword, page, pageSize)
                isLastPage = pagination?.last ?: true
                
                if (page == 0) {
                    // 새로운 검색
                    currentSuggestions.clear()
                    currentSuggestions.addAll(regions)
                } else {
                    // 페이징: 기존 리스트에 추가
                    currentSuggestions.addAll(regions)
                }
                
                regionSuggestionAdapter.updateKeyword(keyword)
                regionSuggestionAdapter.submitList(currentSuggestions.toList())
                binding.cardRegionSuggestions.visibility = if (currentSuggestions.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Log.e(TAG, "지역 검색 실패: ${e.message}", e)
                if (page == 0) {
                    currentSuggestions.clear()
                    regionSuggestionAdapter.submitList(emptyList())
                    binding.cardRegionSuggestions.visibility = View.GONE
                }
            }
        }
    }

    private fun loadMoreRegions() {
        if (isLastPage || currentKeyword.isEmpty()) return
        currentPage++
        searchRegions(currentKeyword, currentPage)
    }


    private fun loadUserSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getUserSettings()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        val userSettings = body.result
                        withContext(Dispatchers.Main) {
                            binding.alertSwitch1.isChecked = userSettings.scrapAlertSummary?.enabled ?: false
                            binding.alertSwitch2.isChecked = userSettings.reviewAlertSummary?.enabled ?: false

                            keywordAdapter.submitList(response.body()?.result?.keywordAlertSummaryList?.toList())

                            // 추가: 빈 리스트일 때 강제로 notifyDataSetChanged()
                            if (userSettings.keywordAlertSummaryList.isEmpty()) {
                                keywordAdapter.notifyDataSetChanged()
                            }

                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AlertSettingsActivity, "설정을 불러오는데 실패했습니다: ${body?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AlertSettingsActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun registerRegionKeyword() {
        Log.d(TAG, "registerRegionKeyword started")

        val region = selectedRegion
        if (region == null) {
            Log.d(TAG, "No region selected")
            Toast.makeText(this, "지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Selected region: ${region.regionName} (ID: ${region.regionId})")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Making API call to register region keyword")
                val response = repository.registerRegionKeyword(region.regionId.toInt())
                Log.d(TAG, "Region keyword API response: isSuccessful=${response.isSuccessful}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d(TAG, "Region keyword response body: isSuccess=${body?.isSuccess}, message=${body?.message}")

                        if (body?.isSuccess == true) {
                            binding.alertLocationText.text.clear()
                            selectedRegion = null
                            binding.cardRegionSuggestions.visibility = View.GONE
                            showToast("지역 알림이 등록되었습니다.")
                            loadUserSettings() // 목록 새로고침
                        } else {
                            val errorMsg = when (body?.code) {
                                "REGION_KEYWORD_LIMIT_EXCEEDED" -> "등록 가능한 지역 알림은 최대 3개입니다."
                                "REGION_KEYWORD_ALREADY_EXISTS" -> "이미 알림으로 등록한 지역입니다."
                                "REGION_NOT_FOUND" -> "존재하지 않는 지역입니다."
                                else -> "등록에 실패했습니다: ${body?.message}"
                            }
                            showToast(errorMsg)
                        }
                    } else {
                        Log.e(TAG, "HTTP error in region keyword registration: ${response.code()}")
                        showToast("등록에 실패했습니다. (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in registerRegionKeyword: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showToast("네트워크 오류가 발생했습니다: ${e.message}")
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
                            if (isEnabled) "게시글 댓글 알림이 활성화되었습니다." else "게시글 댓글 알림이 비활성화되었습니다.",
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

//    private fun toggleKeywordAlert(keywordAlertId: Int, alertType: String, isEnabled: Boolean) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = when (alertType) {
//                    "REGION" -> repository.toggleRegionKeyword(keywordAlertId, isEnabled)
//                    "PRODUCT" -> repository.toggleKeyword(keywordAlertId, isEnabled)
//                    else -> return@launch
//                }
//
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful && response.body()?.isSuccess == true) {
//                        Toast.makeText(this@AlertSettingsActivity,
//                            if (isEnabled) "알림이 활성화되었습니다." else "알림이 비활성화되었습니다.",
//                            Toast.LENGTH_SHORT).show()
//                        loadUserSettings() // 목록 새로고침
//                    } else {
//                        Toast.makeText(this@AlertSettingsActivity, "설정 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@AlertSettingsActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
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
                    // ✅ 네트워크 성공 → 즉시 아이콘 상태 업데이트
                    keywordAdapter.updateEnabledById(keywordAlertId, alertType, isEnabled)
                    showToast(if (isEnabled) "알림이 활성화되었습니다." else "알림이 비활성화되었습니다.")
                    // (선택) 서버 재동기화가 꼭 필요하면 아래 한 줄 유지
                    // loadUserSettings()
                } else {
                    showToast("설정 변경에 실패했습니다.")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showToast("네트워크 오류가 발생했습니다.")
            }
        }
    }
}



    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable
        binding.btnConfirmDelete.visibility = if (enable) View.VISIBLE else View.GONE
        keywordAdapter.notifyDataSetChanged()
        if (!enable) {
            // 편집 모드 종료 시(성공/취소 시점) 후보 비우기
            pendingRemovals.clear()
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
                // 삭제 후 서버 성공 응답을 받았을 때
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlertSettingsActivity, "선택한 키워드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    toggleEditMode(false)

                    // 서버에서 최신 데이터 다시 불러오기
                    loadUserSettings() // 여기서 데이터를 다시 받아서 어댑터에 submitList() 호출 필요
                }
            }
        }
    }

    private fun showToast(msg: String) {
        oneToast?.cancel()
        oneToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        oneToast?.show()
    }

    data class ApiError(
        val isSuccess: Boolean,
        val code: String?,
        val message: String?
    )

    /** 서버 에러바디에서 message만 꺼내기 */
    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            if (errorBody.isNullOrBlank()) return null
            // Gson 쓰는 버전 (Gson 이미 의존성/임포트 되어 있을 확률 높음)
            com.google.gson.Gson().fromJson(errorBody, ApiError::class.java)?.message
                ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}