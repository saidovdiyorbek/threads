package uz.zero.auth.services.security

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import uz.zero.auth.enums.AuthServerGrantType
import uz.zero.auth.model.security.AuthServerAuthenticationToken

interface AuthServerProvider {
    fun provide(client: RegisteredClient, authenticationToken: AuthServerAuthenticationToken): OAuth2Authorization
    fun grantType(): AuthServerGrantType
}
