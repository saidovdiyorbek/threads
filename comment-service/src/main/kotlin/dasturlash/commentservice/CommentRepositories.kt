package dasturlash.commentservice

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
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

interface CommentRepository : BaseRepository<Comment>{
    @Modifying
    @Query("""
        update Comment c set c.replyCommentCount = c.replyCommentCount + 1 where c.id = :commentId
    """)
    fun incrementCommentReplyCount(commentId: Long)

    @Modifying
    @Query("""
        update Comment c set c.replyCommentCount = c.replyCommentCount - 1 where c.id = :commentId
    """)
    fun decrementCommentReplyCount(commentId: Long)
}

interface CommentLikeRepository : BaseRepository<CommentLike>{}
@Repository
interface CommentAttachRepository : BaseRepository<CommentAttach>{
    @Query("""
        select ca.attachHash from CommentAttach ca
        where ca.comment.id = :commentId
    """)
    fun findCommentAttaches(commentId: Long): List<String>
}