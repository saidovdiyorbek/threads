package uz.zero.auth.entities

import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import uz.zero.auth.enums.Role
import uz.zero.auth.enums.UserStatus

//
@Entity(name = "users")
class User(
    var fullName: String? = null,
    @Column(nullable = false, unique = true) var username: String,
    @Column(nullable = false, unique = true) var email: String,
    @Column(nullable = false)var password: String,
    var bio: String? = null,
    @Enumerated(EnumType.STRING) var status: UserStatus,
    @Enumerated(EnumType.STRING) var role: Role,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    val postCount: Int = 0,
) : BaseEntity()