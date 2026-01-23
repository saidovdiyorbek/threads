package dasturlash.userservice

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
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
@Table(name = "users")
class User(
    var fullname: String? = null,
    @Column(nullable = false, unique = true) var username: String,
    @Column(nullable = false, unique = true) var email: String,
    @Column(nullable = false)var password: String,
    var bio: String? = null,
    @Enumerated(EnumType.STRING) var status: UserStatus,
    @Enumerated(EnumType.STRING) var role: UserRole,
    ) : BaseEntity()

@Entity
class UserFollow(
    @ManyToOne(fetch = FetchType.LAZY)
    var profile: User,

    @ManyToOne(fetch = FetchType.LAZY)
    var follow: User
) : BaseEntity()