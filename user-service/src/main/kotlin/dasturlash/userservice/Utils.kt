package dasturlash.userservice

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class SecurityUtil{

    fun getCurrentUserId(): Long{
        val authentication = SecurityContextHolder.getContext().authentication

        if(authentication is JwtAuthenticationToken){
            val userId = authentication.tokenAttributes["uid"].toString()
            return userId.toLong()
        }
        throw Exception("Invalid token")
    }

    fun getCurrentUserRole(): UserRole{
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is JwtAuthenticationToken){
            val userRole = authentication.tokenAttributes[JWT_ROLE_KEY] as UserRole
            return userRole
        }
        throw Exception("Invalid token")
    }

}