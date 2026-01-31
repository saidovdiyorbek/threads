package uz.zero.auth.model.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val id: Long,
    private val username: String,
    private val password: String,
    private val role: String,
    private val enabled: Boolean,

) : UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(role))

    override fun getPassword() = password

    override fun getUsername() = username

    override fun isEnabled() = enabled
    fun getUserId() = id
    fun getRole(): String = role
}