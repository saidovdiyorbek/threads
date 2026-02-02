package dasturlash.commentservice

import jakarta.validation.constraints.Size

data class BaseMessage(val code: Int?, val message: String? = null){
    companion object{
        var OK = BaseMessage(code = 0, message = "OK")
    }
}

data class CommentCreateRequest(
    @field:Size(max = 200, message = "Text max 200")
    val text: String? = null,
    @field:Size(max = 20, message = "Image max 20")
    val hashes: List<String>? = null,
    val postId: Long,
    val parentId: Long? = null,
)

data class CommentResponse(
    val id: Long,
    val text: String? = null,
    val hashes: List<String>? = null,
    val userShortInfo: UserShortInfo? = null,
    val postId: Long? = null,
    val parentId: Long? = null,
    var likeCount: Int = 0,
    val deleted: Boolean,
)

data class CommentUpdateRequest(
    val text: String? = null,
)

data class UserShortInfo(
    val id: Long,
    val username: String,
)

data class CommentShortInfo(
    val text: String? = null,
    val id: Long,
    val parent: Comment? = null,
    val replyCommentCount: Int = 0,
    val likeCount: Int = 0,
)

data class ParentCommentShortInfo(
    val text: String? = null,
    val postId: Long,
    val replyCommentCount: Int = 0,
    val likeCount: Int = 0,
)

data class UserCommentsResponse(
    val user: UserShortInfo? = null,
    val comments: List<CommentShortInfo>? = null
)

data class PostCommentsResponse(
    val postId: Long? = null,
    val comments: List<CommentShortInfo>? = null
)

data class ParentCommentsResponse(
    val parentId: Long? = null,
    val parentText: String? = null,
    val comments: List<ParentCommentShortInfo>? = null
)

data class LikeRequest(
    val commentId: Long,
)

data class UserInfoResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val role: String,
)

data class InternalHashCheckRequest(
    val userId: Long,
    val hash: String,
)

data class InternalHashesCheckRequest(
    val userId: Long,
    val hashes: List<String>
)
