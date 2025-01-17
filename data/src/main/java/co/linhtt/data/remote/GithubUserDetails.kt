package co.linhtt.data.remote

data class GithubUserDetails(
    val login: String,
    val name:String?,
    val avatar_url:String,
    val html_url:String,
    val location:String,
    val followers:Int,
    val following:Int
)