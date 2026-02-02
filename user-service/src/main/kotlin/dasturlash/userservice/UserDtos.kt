package dasturlash.userservice

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class UserCreateRequest(
    @field:Size(min = 1, max = 60, message = "Name must be between 1 and 60")
    val fullName: String? = null,

    @field:Size(min = 3, max = 60, message = "Username must be between 1 and 60")
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,

    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 6, max = 60, message = "Password must be between 1 and 60")
    val password: String,

    @field:Size(min = 6, max = 60, message = "Username must be between 1 and 60")
    val bio: String? = null,

    @field:NotNull(message = "Role cannot be null")
    val role: UserRole,
)

data class UserResponse(
    val id: Long,
    val fullName: String? = null,
    val username: String,
    val email: String,
    val bio: String? = null,
    val deleted: Boolean,
    val status: UserStatus
)

data class UserUpdateRequest(
    @field:Size(min = 1, max = 60, message = "Name must be between 1 and 60")
    val fullName: String? = null,

    @field:Size(min = 3, max = 60, message = "Username must be between 1 and 60")
    val username: String? = null,

    @field:Email(message = "Email must be valid")
    val email: String? = null,

    @field:Size(min = 6, max = 60, message = "Password must be between 1 and 60")
    val password: String? = null,

    @field:Size(min = 6, max = 60, message = "Username must be between 1 and 60")
    val bio: String? = null,
)

data class FollowRequest(
    @field:NotNull
    val followId: Long
)

data class UnfollowRequest(
    @field:NotNull
    val unFollowId: Long
)

data class ProfileResponse(
    val id: Long,
    val fullName: String? = null,
    val username: String,
    val postCount: Int,
    val followersCount: Int,
    val followingCount: Int,
)

data class PostResponse(
    val id: Long,
    val text: String? = null,
    val userId: Long,
    val postAttaches: List<String>,
    val postLikes: Int,
    val postComment: Int
)

data class UserPostResponse(
    val username: String,
    val postId: Long,
    val text: String? = null,
    val userId: Long,
    val postAttaches: List<String>,
    val postLikes: Int,
    val postComment: Int
)

data class UserShortInfo(
    val id: Long,
    val username: String,
)

data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)