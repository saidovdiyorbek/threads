package dasturlash.commentservice

import jakarta.validation.constraints.Size

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class CommentCreateRequest(
    val text: String? = null,
    val hashes: List<String>? = null,
    val userId: Long,
    val postId: Long,
    val parentId: Long? = null,
)

data class CommentResponse(
    val id: Long,
    @field:Size(max = 200, message = "Text max 200")
    val text: String? = null,
    @field:Size(max = 20, message = "Image max 20")
    val hashes: List<String>? = null,
    val userShortInfo: UserShortInfo? = null,
    val postId: Long? = null,
    val parentId: Long? = null,
    val deleted: Boolean,
)

data class CommentUpdateRequest(
    val text: String? = null,
)

data class UserShortInfo(
    val id: Long,
    val username: String,
)