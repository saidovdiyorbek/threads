package dasturlash.commentservice

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

interface CommentService{
    fun create(request: CommentCreateRequest)
    fun getOne(id: Long): CommentResponse
    fun update(id: Long, request: CommentUpdateRequest)
    fun getAll(): List<CommentResponse>
    fun delete(id: Long)
}

@Service
class CommentServiceImpl(
    private val repository: CommentRepository,
    private val commentAttachRepo: CommentAttachRepository,

    private val userClient: UserClient,
    private val postClient: PostClient,
    private val attachClient: AttachClient,
) : CommentService{
    @Transactional
    override fun create(request: CommentCreateRequest) {
        try{
            postClient.exists(request.postId).takeIf {it}?.let {
                userClient.getUserShortInfo(request.userId)?.let { userShortInfo ->
                    val parentComment = request.parentId?.let { parentId ->
                        repository.findByIdAndDeletedFalse(parentId) ?: throw CommentNotFoundException()
                    }
                    val saveComment = repository.save(Comment(
                        request.text,
                        request.postId,
                        request.userId,
                        userShortInfo.username,
                        parentComment
                    ))
                    val commentAttaches: MutableList<CommentAttach> = mutableListOf()
                    request.hashes?.forEach { hash ->
                        attachClient.exists(hash).takeIf {it}?.let {
                            commentAttaches.add(CommentAttach(hash, saveComment))
                        }
                    }
                    postClient.incrementPostComment(saveComment.postId)
                    repository.incrementCommentReplyCount(parentComment?.id!!)
                    commentAttachRepo.saveAll(commentAttaches)
                    return
                }
            }
        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun getOne(id: Long): CommentResponse {
        repository.findByIdAndDeletedFalse(id)?.let { comment ->
            return CommentResponse(
                comment.id!!,
                comment.text,
                commentAttachRepo.findCommentAttaches(comment.id!!),
                UserShortInfo(comment.userId, comment.username),
                comment.postId,
                comment.parentId?.id,
                comment.deleted,
            )
        }
        throw CommentNotFoundException()
    }

    override fun update(id: Long, request: CommentUpdateRequest) {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<CommentResponse> {
        val responseComments: MutableList<CommentResponse> = mutableListOf()
        repository.findAll().forEach { comment ->
            responseComments.add(CommentResponse(
                comment.id!!,
                comment.text,
                commentAttachRepo.findCommentAttaches(comment.id!!),
                UserShortInfo(comment.userId, comment.username),
                comment.postId,
                comment.parentId?.id,
                comment.deleted,
            ))
        }
        return responseComments
    }

    override fun delete(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { comment ->
            val commentAttaches = commentAttachRepo.findCommentAttaches(comment.id!!)
            repository.trash(comment.id!!)
            //parent commentni reply countini kamaytirish
            comment.parentId?.let { parentId ->
                repository.decrementCommentReplyCount(parentId.id!!)
            }
            //postni comment countini kamaytirish
            postClient.decrementPostComment(comment.postId)
            //attachlarni ochirish
            if (commentAttaches.isNotEmpty()) {
                attachClient.deleteList(commentAttaches)
            }
            return
        }
        throw CommentNotFoundException()
    }
}