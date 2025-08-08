import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject


class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

//    fun login(
//        email: String,
//        password: String,
//        onResult: (Boolean, String?, String?, String?, Int?) -> Unit  // ✅ refreshToken 추가됨
//    ) {
//        viewModelScope.launch {
//            try {
//                val response = repository.login(email, password)

//                if (response.isSuccessful) {
//                    val body = response.body()
//                    if (body?.isSuccess == true) {
//                        val accessToken = body.result.accessToken
//                        val refreshToken = body.result.refreshToken  // ✅ 여기서 refreshToken 가져오기
//                        val memberId = body.result.memberId
//
//                        TokenManager.saveToken(accessToken)
//                        TokenManager.saveRefreshToken(refreshToken) // 저장도 같이 가능
//
//                        onResult(true, null, accessToken, refreshToken, memberId)
//                    } else {
//                        onResult(false, body?.message ?: "로그인 실패", null, null, null)
//                    }
//                } else {
//                    onResult(false, "오류 코드: ${response.code()}", null, null, null)
//                }
//            } catch (e: Exception) {
//                onResult(false, e.message, null, null, null)
//            }
//        }
//    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?, String?, String?, Int?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                val errorBodyString = response.errorBody()?.string()
                val body = response.body()

                if (response.isSuccessful && body?.isSuccess == true) {
                    val accessToken = body.result.accessToken
                    val refreshToken = body.result.refreshToken
                    val memberId = body.result.memberId

                    TokenManager.saveToken(accessToken)
                    TokenManager.saveRefreshToken(refreshToken)

                    onResult(true, null, accessToken, refreshToken, memberId)
                } else {
                    // 실패 시 result 값을 꺼냄
                    val resultFromServer = body?.result?.toString()
                        ?: try {
                            val json = JSONObject(errorBodyString ?: "")
                            json.optString("result", "로그인 실패")
                        } catch (e: Exception) {
                            "로그인 실패"
                        }

                    onResult(false, resultFromServer, null, null, null)
                }

            } catch (e: Exception) {
                onResult(false, e.message, null, null, null)
            }
        }
    }



}