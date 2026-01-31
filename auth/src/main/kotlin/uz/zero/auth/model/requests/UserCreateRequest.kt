package uz.zero.auth.model.requests

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import uz.zero.auth.utils.NotSpace


data class UserCreateRequest(
    @field:Size(min = 3, max = 32)
    @field:NotSpace
    val username: String,
    @field:Size(min = 8)
    @field:Pattern.List(
        value = [
            Pattern(regexp = ".*[a-z].*", message = "PASSWORD_LOWER_CASE_ERROR"),
            Pattern(regexp = ".*[A-Z].*", message = "PASSWORD_UPPER_CASE_ERROR"),
            Pattern(regexp = ".*\\d.*", message = "PASSWORD_DIGIT_ERROR"),
            Pattern(
                regexp = ".*[~!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*",
                message = "PASSWORD_EXTRA_CHARACTER_ERROR"
            )
        ]
    )
    val password: String,
    @field:Size(max = 255) val fullName: String,
    @field:Email
    val email: String,
    @field:Size(min = 8, max = 32, message = "BIO_SIZE_ERROR")
    val bio: String? = null,
)
