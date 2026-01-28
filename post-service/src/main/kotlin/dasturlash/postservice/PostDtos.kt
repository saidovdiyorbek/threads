package dasturlash.postservice

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class PostCreateRequest(
    @field:Size(min = 1, max = 255, message = "Text must be between 1 and 255")
    val text: String? = null,
    @field:NotNull
    val userId: Long,
    val parentId: Long? = null,
    @field:Size(max = 20, message = "Image max 20")
    val hashIds: List<String>? = null,
)

data class PostResponse(
    val id: Long,
    val text: String? = null,
    val userId: Long,
    val postAttaches: List<String>,
    val postLikes: Int,
    val postComment: Int
)

data class PostUpdateRequest(
    val text: String? = null,
    @field:Size(max = 20, message = "Image max 20")
    val hashIds: List<String>? = null,
)

data class PostLikeRequest(
    val userId: Long,
    val postId: Long,
)

data class PostDislikeRequest(
    val userId: Long,
    val postId: Long,
)

data class UserShortInfo(
    val id: Long,
    val username: String,
)

data class PostShortResponse(
    val id: Long,
    val text: String? = null,
    val postAttaches: List<String>,
    val postLikes: Int,
    val postComment: Int
)

data class UserPostsResponse(
    val user: UserShortInfo? = null,
    val posts: List<PostShortResponse>? = null
)