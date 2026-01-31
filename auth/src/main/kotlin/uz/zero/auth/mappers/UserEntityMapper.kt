package uz.zero.auth.mappers

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.model.responses.UserResponse
import uz.zero.auth.entities.User
import uz.zero.auth.enums.Role
import uz.zero.auth.enums.UserStatus

@Component
class UserEntityMapper(
    private val passwordEncoder: PasswordEncoder,
) {

    fun toUserInfo(user: User): UserInfoResponse {
        return UserInfoResponse(
            id = user.id!!,
            fullName = user.fullName,
            username = user.username,
            role = user.role.name,
        )
    }

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            username = user.username,
            fullName = user.fullName,
            role = user.role.name,
            status = user.status
        )
    }

    fun toEntity(request: UserCreateRequest, role: Role) = request.run {
        User(fullName = fullName,
            username = username,
            password = passwordEncoder.encode(password),
            role = role,
            email = email,
            status = UserStatus.ACTIVE)
    }
}