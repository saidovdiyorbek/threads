package uz.zero.auth.repositories


import uz.zero.auth.entities.User

interface UserRepository : BaseRepository<User> {
    fun findByUsernameAndDeletedFalse(username: String): User?
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}