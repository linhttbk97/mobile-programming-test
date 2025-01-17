package co.linhtt.domain.repository

import androidx.paging.PagingData
import co.linhtt.domain.model.GithubUser
import co.linhtt.domain.model.GithubUserDetails
import co.linhtt.domain.model.LoadResourceResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<PagingData<GithubUser>>
    fun getUserByLogin(login: String): Flow<LoadResourceResult<GithubUserDetails>>
}