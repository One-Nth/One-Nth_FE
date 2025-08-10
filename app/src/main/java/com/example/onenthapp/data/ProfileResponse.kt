data class ProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ProfileResult
)

data class ProfileResult(
    val profileImageUrl: String?,
    val nickname: String,
    val verifiedRegionNames: List<String> = emptyList()
)

data class NicknameResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NicknameResult
)

data class NicknameResult(
    val nickname: String
)

data class ProfileImageResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ProfileImageResult
)

data class ProfileImageResult(
    val profileImageUrl: String
)

