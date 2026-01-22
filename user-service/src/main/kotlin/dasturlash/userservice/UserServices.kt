package dasturlash.userservice

import org.springframework.stereotype.Service

interface UserServices {
    fun create(request: UserCreateRequest)
    fun getOne(id: Long): UserResponse
    fun getAll(): List<UserResponse>
    fun update(id: Long, request: UserUpdateRequest)
}

@Service
class UserServiceImpl(
    private val repository: UserRepository

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
}