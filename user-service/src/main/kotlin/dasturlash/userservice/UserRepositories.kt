package dasturlash.userservice

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun findByIdFull(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    entityManager: EntityManager
): SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {
    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), true) }
    override fun findByIdAndDeletedFalse(id: Long): T? = findByIdOrNull(id)?.run { if (deleted) null else this }
    override fun findByIdFull(id: Long): T? = findByIdOrNull(id)

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)

    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)

}
@Repository
interface UserRepository : BaseRepository<User>{
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
    @Query("""
        select u from User u
        where u.id = ?1 and u.role = 'ROLE_USER' and u.deleted = false
    """)
    fun findByIdAndRoleUser(id: Long): User?

    @Modifying // Bu o'zgartirish kiritadi degani
    @Query("UPDATE User u SET u.followersCount = u.followersCount + 1 WHERE u.id = :userId")
    fun incrementFollowers(userId: Long)

    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
    fun incrementFollowing(userId: Long)

    // Unfollow uchun teskarisi (decrement)
    @Modifying
    @Query("UPDATE User u SET u.followersCount = u.followersCount - 1 WHERE u.id = :userId")
    fun decrementFollowers(userId: Long)

    @Modifying
    @Query("UPDATE User u SET u.followingCount = u.followingCount - 1 WHERE u.id = :userId")
    fun decrementFollowing(userId: Long)
}

interface UserFollowRepository : BaseRepository<UserFollow> {
    @Query("""
        select case 
            when count(uf) > 0 then true else false
        end
        from UserFollow uf
        where uf.profile.id = ?1 and uf.follow.id = ?2
    """)
    fun checkFollowing(profileId: Long,  follow: Long): Boolean

    @Query("""
        select u from UserFollow u
        where u.profile.id = ?1 and u.follow.id = ?2
    """)
    fun checkUnFollowing(profileId: Long, follow: Long): UserFollow?

    @Query("""
        select count(u)
        from UserFollow u
        where u.follow.id = ?1
    """)
    fun getFollowers(userId: Long): Int

    @Query("""
        select count(u) from UserFollow u
        where u.profile.id = ?1
    """)
    fun getFollowing(userId: Long): Int
}