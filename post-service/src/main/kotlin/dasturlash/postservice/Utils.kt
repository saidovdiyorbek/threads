package dasturlash.postservice

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

}