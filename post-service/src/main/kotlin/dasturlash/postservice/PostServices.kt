package dasturlash.postservice

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

interface PostService {
    fun create(request: PostCreateRequest)
    fun getOne(id: Long): PostResponse
    fun getAll(): List<PostResponse>
    fun update(id: Long, request: PostUpdateRequest)
    fun delete(id: Long)
    fun getUserPosts(userId: Long): UserPostsResponse
    fun postLike(request: PostLikeRequest)
    fun postDislike(request: PostDislikeRequest)
    fun getUserLikedPosts(userId: Long): List<PostResponse>
    fun exists(id: Long): Boolean
    fun incrementPostComment(id: Long)
    fun decrementPostComment(id: Long)
    fun deleteCommentsByPost(postId: Long)
}

@Service
class PostServicesImpl(
   private val repository: PostRepository,
   private val postAttachRepo: PostAttachRepository,
   private val userClient: UserClient,
   private val attachClient: AttachClient,
   private val postLikeRepo: PostLikeRepository,
   private val securityUtil: SecurityUtil
) : PostService{
    @Transactional
    override fun create(request: PostCreateRequest) {
        try {
            val post = Post(userId = 0).apply {
                val currentUserId = securityUtil.getCurrentUserId()
                println("In session user: $currentUserId")
                userClient.exists(currentUserId).takeIf { it }?.let {
                    userId = currentUserId
                    //check parent post
                    request.parentId?.let {
                        val findPost = repository.findByIdAndDeletedFalse(it) ?:
                            throw PostNotFoundException()
                        post = findPost
                    }

                    //check text
                    request.text?.let { text = it }


                }
            }
            val postAttaches: MutableList<PostAttach> = mutableListOf()
            request.hashIds?.forEach { hash ->
                println("Create postda 54")
                attachClient.exists(InternalHashCheckRequest(securityUtil.getCurrentUserId(), hash)).takeIf { it }?.let {
                    postAttaches.add(PostAttach(hash, post))
                }
            }
            repository.save(post)
            userClient.incrementUserPostCount(securityUtil.getCurrentUserId())
            postAttachRepo.saveAll(postAttaches)
        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun getOne(id: Long): PostResponse {
        repository.findByIdAndDeletedFalse(id).let { post ->
            val postAttachHash = postAttachRepo.getPostAttachHash(id)
            return PostResponse(
                id = post!!.id!!,
                text = post.text,
                userId = post.userId,
                postAttachHash,
                post.postLikeCount,
                post.postCommentCount
            )
        }
    }

    override fun getAll(): List<PostResponse> {
        val responsePosts: MutableList<PostResponse> = mutableListOf()
        repository.findAll().forEach { post ->
            val postAttachHash = postAttachRepo.getPostAttachHash(post.id!!)
            responsePosts.add(PostResponse(
                id = post!!.id!!,
                text = post.text,
                userId = post.userId,
                postAttachHash,
                post.postLikeCount,
                post.postCommentCount
            ))
        }
        return responsePosts
    }
    @Transactional
    override fun update(id: Long, request: PostUpdateRequest) {
        try{
            repository.findByIdAndDeletedFalse(id)?.let { post ->
                val currentUserId = securityUtil.getCurrentUserId()
                if (post.userId != currentUserId)
                    throw PostNotYoursException()
                request.text?.let { post.text = it
                                    repository.save(post)}


                request.hashIds?.let { newHashes ->
                    val postAttachHashes = postAttachRepo.getPostAttachHash(id)
                    val hashesToAdd = newHashes.filter { !postAttachHashes.contains(it) }
                    val hashesToRemove = postAttachHashes.filter { !newHashes.contains(it) }

                    //ochirilgan attachlarnini bazadan ham ochirish
                    if (hashesToRemove.isNotEmpty()) {
                        postAttachRepo.removeByHashesList(hashesToRemove)
                        attachClient.deleteList(hashesToRemove)
                    }

                    // yangi rasmlarni bazaga qoshish
                    if (hashesToAdd.isNotEmpty()) {
                        attachClient.listExists(InternalHashesCheckRequest(currentUserId, hashesToAdd))
                        val postAttachesToAdd: MutableList<PostAttach> = mutableListOf()
                        hashesToAdd.forEach { hash ->
                            postAttachesToAdd.add(PostAttach(hash, post))
                        }
                        postAttachRepo.saveAll(postAttachesToAdd)
                    }
                }
                return
            }
            throw PostNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }

    }
    @Transactional
    override fun delete(id: Long) {
        try{
            val currentUserId = securityUtil.getCurrentUserId()
            repository.findByIdAndDeletedFalse(id)?.let { post ->
                if (currentUserId != post.userId) {
                    throw PostNotYoursException()
                }
                val postAttachHash = postAttachRepo.getPostAttachHash(id)
                repository.trash(post.id!!)
                //child postlarni ochirish
                repository.findPostByParentId(id)
                userClient.decrementUserPostCount(post.userId)
                if (postAttachHash.isNotEmpty()) {
                    attachClient.deleteList(postAttachHash)
                }
                return
            }
            throw PostNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun getUserPosts(userId: Long): UserPostsResponse {
        try{
            userClient.getUserShortInfo(userId)?.let { shortInfo ->
                val responseUserPosts: MutableList<PostShortResponse> = mutableListOf()
                repository.findPostByUserIdAndDeletedFalse(shortInfo.id)?.forEach { post ->
                    responseUserPosts.add(PostShortResponse(
                        id = post.id!!,
                        text = post.text,
                        postAttachRepo.getPostAttachHash(post.id!!),
                        post.postLikeCount,
                        post.postCommentCount
                    ))
                }
                return UserPostsResponse(
                    shortInfo,
                    responseUserPosts
                )
            }
        }catch (e: FeignClientException){
            throw e
        }
        return UserPostsResponse()
    }

    @Transactional
    override fun postLike(request: PostLikeRequest) {
        try{
            repository.findByIdAndDeletedFalse(request.postId)?.let { post ->
                val currentUserId = securityUtil.getCurrentUserId()
                userClient.exists(currentUserId).takeIf { it }?.let {
                    postLikeRepo.findPostLikeByPostIdAndUserIdAndDeletedTrue(post.id!!, currentUserId)?.let { postLike ->
                        repository.incrementLike(post.id!!)
                        postLike.deleted = false
                        postLikeRepo.save(postLike)
                        return
                    }
                    postLikeRepo.findPostLikeByPostIdAndUserIdAndDeletedFalse(post.id!!, currentUserId)?.let {throw UserAlreadyLikedException() }

                    postLikeRepo.save(PostLike(currentUserId, post))
                    repository.incrementLike(post.id!!)
                    return
                }
            }
            throw PostNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }
    }
    @Transactional
    override fun postDislike(request: PostDislikeRequest) {
        try {
            repository.findByIdAndDeletedFalse(request.postId)?.let { post ->
                val currentUserId = securityUtil.getCurrentUserId()
                userClient.exists(currentUserId).takeIf { it }?.let {
                    postLikeRepo.findPostLikeByPostIdAndUserIdAndDeletedFalse(post.id!!, currentUserId)?.let { postLike ->
                        repository.decrementLike(post.id!!)
                        postLike.deleted = true
                        postLikeRepo.save(postLike)
                        return
                    }
                    throw UserAlreadyDislikedException()
                }
            }
            throw PostNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun getUserLikedPosts(userId: Long): List<PostResponse> {
        val responsePosts: MutableList<PostResponse> = mutableListOf()
        repository.findUserLikedPostsAndDeletedFalse(userId)?.forEach { post ->
            responsePosts.add(PostResponse(
                id = post.id!!,
                text = post.text,
                userId = post.userId,
                postAttachRepo.getPostAttachHash(post.id!!),
                post.postLikeCount,
                post.postCommentCount
            ))
        }
        return responsePosts
    }

    override fun exists(id: Long): Boolean {
        repository.existsPostByIdAndDeletedFalse(id).takeIf { it }?.let {
            println("Post topildi")
            return true
        }
        println("Post topilmadi")
        throw PostNotFoundException()
    }
    @Transactional
    override fun incrementPostComment(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { post ->
            repository.incrementComment(post.id!!)
            return
        }
        throw PostNotFoundException()
    }
    @Transactional
    override fun decrementPostComment(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { post ->
            repository.decrementComment(post.id!!)
            return
        }
        throw PostNotFoundException()
    }

    override fun deleteCommentsByPost(postId: Long) {

    }
}