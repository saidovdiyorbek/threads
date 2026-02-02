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
    fun incrementUserPostCount(id: Long)
    fun decrementUserPostCount(id: Long)
    fun getUserShortInfo(id: Long): UserShortInfo?
}

@Service
class UserServiceImpl(
    private val repository: UserRepository,
    private val userFollow: UserFollowRepository,
    private val securityUtil: SecurityUtil,

) : UserServices {
    @Transactional
    override fun create(request: UserCreateRequest) {
        repository.existsByUsername(request.username).takeIf { it }?.let {
            throw UsernameAlreadyExistsException()
        }
        repository.existsByEmail(request.email).takeIf { it }?.let {
            throw EmailAlreadyExistsException()
        }

        repository.save(User(
            fullName = request.fullName?.let { it } as String,
            username = request.username,
            email = request.email,
            password = request.password,
            bio = request.bio?.let { it } as String,
            status = UserStatus.ACTIVE,
            role = request.role,
        ))
    }
    @Transactional
    override fun getOne(id: Long): UserResponse {
        repository.findByIdFull(id)?.let { user ->
            return UserResponse(
                user.id!!,
                user.fullName?.let { it },
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
                fullName = user.fullName?.let { it } as String,
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
                user.fullName = it
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
        val currentUserId = securityUtil.getCurrentUserId()
        repository.findByIdAndDeletedFalse(currentUserId)?.let { profile ->
            // follow bosilayotgan profile user ekanligini tekshirayapmiz
            repository.findByIdAndRoleUser(followRequest.followId)?.let { follow ->
                val checkFollowing = userFollow.checkFollowing(currentUserId, followRequest.followId)
                if (!checkFollowing && currentUserId != followRequest.followId) {
                    userFollow.save(UserFollow(
                        // follow qilayotgan user
                        profile = profile,
                        // follow bosilayotgan user
                        follow = follow
                    ))

                    repository.incrementFollowers(followRequest.followId)
                    repository.incrementFollowing(currentUserId)
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
        val currentUserId = securityUtil.getCurrentUserId()
        repository.findByIdAndDeletedFalse(currentUserId)?.let { profile ->
            // follow bosilayotgan profile user ekanligini tekshirayapmiz
            repository.findByIdAndRoleUser(unfollowRequest.unFollowId)?.let { follow ->
                userFollow.checkUnFollowing(currentUserId, unfollowRequest.unFollowId)?.let {
                    if (currentUserId != unfollowRequest.unFollowId) {
                        userFollow.delete(it)
                        repository.decrementFollowers(unfollowRequest.unFollowId)
                        repository.decrementFollowing(currentUserId)
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
                fullName = user.fullName?.let { it } as String,
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

    @Transactional
    override fun incrementUserPostCount(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { user ->
            repository.incrementUserPost(id)
            return
        }
        throw UserNotFoundException()
    }

    @Transactional
    override fun decrementUserPostCount(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.let { user ->
            repository.decrementUserPost(id)
            return
        }
        throw UserNotFoundException()
    }

    override fun getUserShortInfo(id: Long): UserShortInfo? {
        repository.findByIdAndDeletedFalse(id)?.let { user ->
            return UserShortInfo(
                id = user.id!!,
                username = user.username,
            )
        }
        throw UserNotFoundException()
    }
}