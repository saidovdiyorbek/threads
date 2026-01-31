package uz.zero.auth.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.zero.auth.enums.Role
import uz.zero.auth.mappers.UserEntityMapper
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.repositories.UserRepository
import uz.zero.auth.utils.userId

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserEntityMapper
) {
    fun userMe(): UserInfoResponse {
        return userMapper
            .toUserInfo(userRepository.findByIdAndDeletedFalse(userId())!!)
    }

    @Transactional
    fun registerUser(request: UserCreateRequest) {
        if (userRepository.existsByUsername(request.username))
            throw IllegalArgumentException("Username ${request.username} is already registered")

        if(userRepository.existsByEmail(request.email))
            throw IllegalArgumentException("Email ${request.email} is already registered")

        userRepository.save(userMapper.toEntity(request, Role.USER))
    }
}