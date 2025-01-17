package co.linhtt.githubuser.ui.user_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import co.linhtt.domain.model.LoadResourceResult
import co.linhtt.domain.usecase.GetUserByLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserByLogin: GetUserByLogin
) : ViewModel() {
    val userDetailsResourceState =
        getUserByLogin.invoke(getUserLoginBundle()).mapLatest {
            when (it) {
                is LoadResourceResult.Error -> UserDetailsScreenState.Error(it.message)
                LoadResourceResult.Loading -> UserDetailsScreenState.Loading
                is LoadResourceResult.Success -> UserDetailsScreenState.Success(it.data)
            }
        }.flowOn(Dispatchers.IO)

    private fun getUserLoginBundle() = savedStateHandle.get<String>("login")
        ?: throw IllegalArgumentException("login cannot be null")
}