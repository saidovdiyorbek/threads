package dasturlash.postservice

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

interface PostService {
    fun create(request: PostCreateRequest)
    fun getOne(id: Long): PostResponse
    fun getAll(): List<PostResponse>
    fun update(id: Long, request: PostUpdateRequest)
    fun delete(id: Long)
    fun getUserPosts(userId: Long): List<PostResponse>
    fun postLike(request: PostLikeRequest)
    fun postDislike(request: PostDislikeRequest)
    fun getUserLikedPosts(userId: Long): List<PostResponse>
}

@Service
class PostServicesImpl(
   private val repository: PostRepository,
   private val postAttachRepo: PostAttachRepository,
   private val userClient: UserClient,
   private val attachClient: AttachClient,
   private val postLikeRepo: PostLikeRepository,
) : PostService{
    @Transactional
    override fun create(request: PostCreateRequest) {
        try {

            val post = Post(userId = 0).apply {
                userClient.exists(request.userId).takeIf { it }?.let {
                    userId = request.userId
                    //check parent post
                    request.parentId?.let {
                        val findPost = repository.findByIdAndDeletedFalse(it) ?:
                            throw PostNotFoundException()
                        post = findPost
                    }

                    //check text
                    request.text?.let { text = it }
                    repository.save(this)
                    userClient.incrementUserPostCount(request.userId)
                }
            }
            val postAttaches: MutableList<PostAttach> = mutableListOf()
            request.hashIds?.forEach { hash ->
                attachClient.exists(hash).takeIf { it }?.let {
                    postAttaches.add(PostAttach(hash, post))
                }
            }

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
                        attachClient.listExists(hashesToAdd)
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
        repository.findByIdAndDeletedFalse(id)?.let { post ->
            val postAttachHash = postAttachRepo.getPostAttachHash(id)
            repository.trash(post.id!!)
            userClient.decrementUserPostCount(post.userId)
            if (postAttachHash.isNotEmpty()){
                attachClient.deleteList(postAttachHash)
            }
            return
        }
        throw PostNotFoundException()
    }

    override fun getUserPosts(userId: Long): List<PostResponse> {
        val responsePosts: MutableList<PostResponse> = mutableListOf()
        repository.findPostByUserId(userId)?.forEach { post ->
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

    @Transactional
    override fun postLike(request: PostLikeRequest) {
        try{
            repository.findByIdAndDeletedFalse(request.postId)?.let { post ->
                userClient.exists(request.userId).takeIf { it }?.let {
                    postLikeRepo.findPostLikeByPostIdAndUserIdAndDeletedTrue(post.id!!, request.userId)?.let { postLike ->
                        repository.incrementLike(post.id!!)
                        postLike.deleted = false
                        postLikeRepo.save(postLike)
                        return
                    }
                    postLikeRepo.findPostLikeByPostIdAndUserIdAndDeletedFalse(post.id!!, request.userId)?.let {throw UserAlreadyLikedException() }

                    postLikeRepo.save(PostLike(request.userId, post))
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
                userClient.exists(request.userId).takeIf { it }?.let {
                    postLikeRepo.findPostLikeByPostIdAndUserIdAndDeletedFalse(post.id!!, request.userId)?.let { postLike ->
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
        repository.findUserLikedPosts(userId)?.forEach { post ->
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
}