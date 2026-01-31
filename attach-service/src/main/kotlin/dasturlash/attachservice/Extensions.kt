package dasturlash.attachservice

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uz.zero.user.USERNAME_KEY
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.util.Base64
import java.util.zip.GZIPInputStream


fun getHeader(headerKey: String): String? {
    return try {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        request.getHeader(headerKey)
    } catch (e: Exception) {
        null
    }
}


fun getUserJwtPrincipal(): Jwt? {
    val principal = SecurityContextHolder.getContext().authentication?.principal

    if (principal is Jwt) {
        return principal
    }

    return null
}


fun String.decompress(): String {
    val bytes = Base64.getDecoder().decode(this)
    return GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
}

fun username(): String {
    return getUserJwtPrincipal()?.claims?.get(USERNAME_KEY) as String
}
