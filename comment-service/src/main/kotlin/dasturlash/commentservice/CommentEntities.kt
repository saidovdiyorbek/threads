package dasturlash.commentservice

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP)var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP)var lastModifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
class Comment(
    var text: String? = null,
    var postId: Long,
    var userId: Long,
    var username: String,
    @ManyToOne(fetch = FetchType.LAZY)
    var parentId: Comment? = null,
    var replyCommentCount: Int = 0
) : BaseEntity()

@Entity
class CommentAttach(
    val attachHash: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val comment: Comment,
) : BaseEntity()

@Entity
class CommentLike(
    @ManyToOne(fetch = FetchType.LAZY)
    val comment: Comment,
    val userId: Long,
) : BaseEntity()