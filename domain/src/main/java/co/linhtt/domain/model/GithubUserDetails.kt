package co.linhtt.domain.model

data class GithubUserDetails(
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val name: String?,
    val followers: Int,
    val location: String?,
    val following: Int
)

sealed class LoadResourceResult<out T> {
    data class Success<T>(val data: T) : LoadResourceResult<T>()
    data class Error(val message: String) : LoadResourceResult<Nothing>()
    data object Loading : LoadResourceResult<Nothing>()
}