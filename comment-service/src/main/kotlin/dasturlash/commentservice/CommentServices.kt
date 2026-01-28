package dasturlash.commentservice

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

interface CommentService{
    fun create(request: CommentCreateRequest)
    fun getOne(id: Long): CommentResponse
    fun update(id: Long, request: CommentUpdateRequest)
    fun getAll(): List<CommentResponse>
    fun delete(id: Long)
    fun getCommentsByUserId(userId: Long): UserCommentsResponse
    fun getCommentsByPostId(postId: Long): PostCommentsResponse
    fun getCommentsByParent(parentId: Long): ParentCommentsResponse
    fun likeComment(request: LikeRequest)
    fun dislikeComment(request: LikeRequest)
}

@Service
class CommentServiceImpl(
    private val repository: CommentRepository,
    private val commentAttachRepo: CommentAttachRepository,
    private val commentLikeRepo: CommentLikeRepository,

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
                    parentComment?.let {
                        repository.incrementCommentReplyCount(parentComment.id!!)
                    }
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
                comment.likeCount,
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
                comment.likeCount,
                comment.deleted,
            ))
        }
        return responseComments
    }

    override fun delete(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { comment ->
            val commentAttaches = commentAttachRepo.findCommentAttaches(comment.id!!)
            repository.trash(comment.id!!)
            repository.deleteCommentByParentId(id)
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

    override fun getCommentsByUserId(userId: Long): UserCommentsResponse {
        try{
            val shortInfo = userClient.getUserShortInfo(userId)?.let { userShortInfo ->
                val responseCommentShortInfo: MutableList<CommentShortInfo> = mutableListOf()
                repository.findCommentsByUserId(userId).forEach { comment ->
                    responseCommentShortInfo.add(
                        CommentShortInfo(
                            comment.text,
                            comment.id!!,
                            comment.parentId,
                            comment.replyCommentCount,
                            comment.likeCount,
                        )
                    )
                }
                return UserCommentsResponse(
                    userShortInfo,
                    responseCommentShortInfo
                )
            }
        }catch (e: FeignClientException){
            throw e
        }
        return UserCommentsResponse()
    }
    @Transactional
    override fun getCommentsByPostId(postId: Long): PostCommentsResponse {
        try{
            postClient.exists(postId).takeIf { it }?.let {
                val responseCommentShortInfo: MutableList<CommentShortInfo> = mutableListOf()
                repository.findCommentsByPostId(postId).forEach { comment ->
                    responseCommentShortInfo.add(
                        CommentShortInfo(
                            comment.text,
                            comment.id!!,
                            comment.parentId,
                            comment.replyCommentCount,
                            comment.likeCount,
                        )
                    )
                }
                return PostCommentsResponse(
                    postId,
                    responseCommentShortInfo
                )
            }
        }catch (e: FeignClientException){
            throw e
        }
          return PostCommentsResponse()
    }

    override fun getCommentsByParent(parentId: Long): ParentCommentsResponse {
        repository.findByIdAndDeletedFalse(parentId)?.let { pComment ->
            val responseCommentShortInfo: MutableList<ParentCommentShortInfo> = mutableListOf()
            repository.findCommentsByParentId(parentId).forEach { comment ->
                responseCommentShortInfo.add(ParentCommentShortInfo(
                    comment.text,
                    comment.id!!,
                    comment.replyCommentCount,
                    comment.likeCount,
                ))
            }
            return ParentCommentsResponse(
                pComment.id,
                pComment.text,
                responseCommentShortInfo
            )
        }
        throw CommentNotFoundException()
    }
    @Transactional
    override fun likeComment(request: LikeRequest) {
        try {
            userClient.exists(request.userId).takeIf { it }?.let {
                repository.findByIdAndDeletedFalse(request.commentId)?.let { comment ->
                    commentLikeRepo.findCommentLikeByCommentIdAndUserIdAndDeletedFalseOrDeletedTrue(comment.id!!, request.userId)?.let {
                        if (it.deleted){
                            it.deleted = false
                            commentLikeRepo.save(it)
                            repository.incrementCommentLike(request.commentId)
                            return
                        }
                    }
                    commentLikeRepo.save(CommentLike(comment, request.userId))
                    repository.incrementCommentLike(comment.id!!)
                }
            }
        }catch (e: FeignClientException){
            throw e
        }
    }

    @Transactional
    override fun dislikeComment(request: LikeRequest) {
        try {
            userClient.exists(request.userId).takeIf { it }?.let {
                repository.findByIdAndDeletedFalse(request.commentId)?.let { comment ->
                    commentLikeRepo.findCommentLikeByCommentIdAndUserIdAndDeletedFalseOrDeletedTrue(comment.id!!, request.userId)?.let {
                        if (!it.deleted){
                            it.deleted = true
                            commentLikeRepo.save(it)
                            repository.decrementCommentLike(request.commentId)
                            return
                        }
                    }
                    repository.decrementCommentReplyCount(comment.id!!)
                }
            }
        }catch (e: FeignClientException){
            throw e
        }
    }
}