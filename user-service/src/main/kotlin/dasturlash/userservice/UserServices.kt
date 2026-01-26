package dasturlash.userservice

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

interface UserServices {
    fun create(request: UserCreateRequest)
    fun getOne(id: Long): UserResponse
    fun getAll(): List<UserResponse>
    fun update(id: Long, request: UserUpdateRequest)
    fun delete(id: Long)
    fun follow(followRequest: FollowRequest)
    fun unfollow(unfollowRequest: UnfollowRequest)
    fun viewProfile(id: Long): ProfileResponse
    fun exists(id: Long): Boolean
    fun userPosts(id: Long): List<UserPostResponse>
}

@Service
class UserServiceImpl(
    private val repository: UserRepository,
    private val userFollow: UserFollowRepository,
    private val postClient: PostClient

) : UserServices {
    override fun create(request: UserCreateRequest) {
        repository.existsByUsername(request.username).takeIf { it }?.let {
            throw UsernameAlreadyExistsException()
        }
        repository.existsByEmail(request.email).takeIf { it }?.let {
            throw EmailAlreadyExistsException()
        }

        repository.save(User(
            fullname = request.fullName?.let { it } as String,
            username = request.username,
            email = request.email,
            password = request.password,
            bio = request.bio?.let { it } as String,
            status = UserStatus.ACTIVE,
            role = request.role,
        ))
    }

    override fun getOne(id: Long): UserResponse {
        repository.findByIdFull(id)?.let { user ->
            return UserResponse(
                user.id!!,
                user.fullname?.let { it },
                user.username,
                user.email,
                user.bio?.let { it } as String,
                user.deleted,
                user.status,
            )
        }
        throw UserNotFoundException()
    }

    override fun getAll(): List<UserResponse> {
        val findUser: MutableList<UserResponse> = mutableListOf()
        repository.findAll().forEach { user ->
            findUser.add(UserResponse(
                id = user.id!!,
                fullName = user.fullname?.let { it } as String,
                username = user.username,
                email = user.email,
                bio = user.bio?.let { it } as String,
                deleted = user.deleted,
                status = user.status,
            ))
        }
        return findUser
    }

    override fun update(id: Long, request: UserUpdateRequest) {
        repository.findByIdAndDeletedFalse(id)?.let { user ->
            request.username?.let { repository.existsByUsername(it).takeIf { it }?.let {
                throw UsernameAlreadyExistsException()
                }
                user.fullname = it
            }
            request.email?.let { repository.existsByEmail(it).takeIf { it }?.let {
                throw EmailAlreadyExistsException()
                }
                user.email = it
            }
            request.password?.let { user.password = it }
            request.bio?.let { user.bio = it }
            repository.save(user)
        }
    }

    override fun delete(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { user ->
            repository.trash(id)
        }
        throw UserNotFoundException()
    }
    @Transactional
    override fun follow(followRequest: FollowRequest) {
        repository.findByIdAndDeletedFalse(followRequest.profileId)?.let { profile ->
            // follow bosilayotgan profile user ekanligini tekshirayapmiz
            repository.findByIdAndRoleUser(followRequest.followId)?.let { follow ->
                val checkFollowing = userFollow.checkFollowing(followRequest.profileId, followRequest.followId)
                if (!checkFollowing && followRequest.profileId != followRequest.followId) {
                    userFollow.save(UserFollow(
                        // follow qilayotgan user
                        profile = profile,
                        // follow bosilayotgan user
                        follow = follow
                    ))

                    repository.incrementFollowers(followRequest.followId)
                    repository.incrementFollowing(followRequest.profileId)
                    return
                }
                throw UserAlreadyFollowedOrSelfFollowException()
            }
            throw FollowingOrUnfollowingUserNotFoundException()
        }
        throw UserNotFoundException()
    }
    @Transactional
    override fun unfollow(unfollowRequest: UnfollowRequest) {
        repository.findByIdAndDeletedFalse(unfollowRequest.profileId)?.let { profile ->
            // follow bosilayotgan profile user ekanligini tekshirayapmiz
            repository.findByIdAndRoleUser(unfollowRequest.followId)?.let { follow ->
                userFollow.checkUnFollowing(unfollowRequest.profileId, unfollowRequest.followId)?.let {
                    if (unfollowRequest.profileId != unfollowRequest.followId) {
                        userFollow.delete(it)
                        repository.decrementFollowers(unfollowRequest.followId)
                        repository.decrementFollowing(unfollowRequest.profileId)
                        return
                    }
                }
                throw UserAlreadyUnfollowedOrSelfUnfollowException()
            }
            throw FollowingOrUnfollowingUserNotFoundException()
        }
        throw UserNotFoundException()
    }

    override fun viewProfile(id: Long): ProfileResponse {
        repository.getUserForProfile(id)?.let { user ->
            val followers = userFollow.getFollowers(user.id!!)
            val following = userFollow.getFollowing(user.id!!)
            val posts = user.postCount

            return ProfileResponse(
                id = user.id!!,
                fullName = user.fullname?.let { it } as String,
                username = user.username,
                postCount = posts,
                followersCount = followers,
                followingCount = following,
            )
        }
        throw UserNotFoundException()
    }

    override fun exists(id: Long): Boolean {
        repository.existsUserById(id).takeIf { it }?.let { user ->
            print("User bor ekan")
            return true
        }
        print("User yoq ekan")
        throw UserNotFoundException()
    }

    override fun userPosts(id: Long): List<UserPostResponse> {
        try {
            repository.findByIdAndDeletedFalse(id)?.let { user ->
                val userPosts = postClient.getUserPosts(user.id!!)
                val responseUserPost: MutableList<UserPostResponse> = mutableListOf()
                if (userPosts.isNotEmpty()){
                    userPosts.forEach { postResponse ->
                        responseUserPost.add(UserPostResponse(
                            user.username,
                            postResponse.id,
                            postResponse.text,
                            postResponse.userId,
                            postResponse.postAttaches,
                            postResponse.postLikes,
                            postResponse.postComment
                        ))
                    }
                    return responseUserPost
                }
                return responseUserPost
            }
            throw UserNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }
    }
}