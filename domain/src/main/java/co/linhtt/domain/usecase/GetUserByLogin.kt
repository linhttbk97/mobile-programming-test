package co.linhtt.domain.usecase

import co.linhtt.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByLogin @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(login: String) = userRepository.getUserByLogin(login)
}