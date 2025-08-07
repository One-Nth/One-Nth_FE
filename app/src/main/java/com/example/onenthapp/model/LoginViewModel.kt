import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

//    fun login(email: String, password: String, onResult: (Boolean, String?, String?) -> Unit) {
//        viewModelScope.launch {
//            try {
//                val response = repository.login(email, password)
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    if (body?.isSuccess == true) {
//                        val token = body.result.accessToken
//                        TokenManager.saveToken(token)
//                        onResult(true, null, token)
//                    } else {
//                        onResult(false, body?.message ?: "로그인 실패", null)
//                    }
//                } else {
//                    onResult(false, "오류 코드: ${response.code()}", null)
//                }
//            } catch (e: Exception) {
//                onResult(false, e.message, null)
//            }
//        }
//    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?, String?, Int?) -> Unit  // ✅ memberId(Int?) 추가
    ) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        val token = body.result.accessToken
                        val memberId = body.result.memberId  // ✅ memberId 꺼내기
                        TokenManager.saveToken(token)
                        TokenManager.saveMemberId(memberId.toLong())  // 저장까지 할 수도 있음
                        onResult(true, null, token, memberId)  // ✅ 콜백에 전달
                    } else {
                        onResult(false, body?.message ?: "로그인 실패", null, null)
                    }
                } else {
                    onResult(false, "오류 코드: ${response.code()}", null, null)
                }
            } catch (e: Exception) {
                onResult(false, e.message, null, null)
            }
        }
    }


}

