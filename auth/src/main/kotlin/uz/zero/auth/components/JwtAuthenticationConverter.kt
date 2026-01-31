package uz.zero.auth.components

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import uz.davrbank.auth.constants.USER_DETAILS_HEADER_KEY
import uz.zero.auth.extensions.decompress
import uz.zero.auth.extensions.getUsername
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.utils.getHeader

@Component
class JwtAuthenticationConverter(
    private val objectMapper: ObjectMapper,
) : Converter<Jwt, JwtAuthenticationToken> {
    override fun convert(source: Jwt): JwtAuthenticationToken {
        val userDetailsJson = getHeader(USER_DETAILS_HEADER_KEY)?.decompress()
        val userDetails = userDetailsJson?.run { objectMapper.readValue(this, UserInfoResponse::class.java) }
        val username = userDetails?.username ?: source.getUsername()
        val authorities = mutableListOf<SimpleGrantedAuthority>()
        if (userDetails != null) {
            authorities.add(SimpleGrantedAuthority("ROLE_${userDetails.role}"))
        }
        return JwtAuthenticationToken(source, authorities, username).apply {
            details = userDetailsJson
        }
    }
}