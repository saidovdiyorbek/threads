package uz.davrbank.auth.models.requests

import jakarta.validation.constraints.Size
import uz.zero.auth.utils.NotSpace


data class UserUpdateRequest(
    @field:Size(min = 3, max = 32)
    @field:NotSpace
    val username: String,
    val roleId: Long,
    @field:Size(max = 255) val fullName: String
)