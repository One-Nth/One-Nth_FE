import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onenthapp.data.AuthRepository
import com.example.onenthapp.util.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        TokenManager.saveToken(body.result.accessToken)
                        onResult(true, null)
                    } else {
                        onResult(false, body?.message ?: "로그인 실패")
                    }
                } else {
                    onResult(false, "오류 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

}

