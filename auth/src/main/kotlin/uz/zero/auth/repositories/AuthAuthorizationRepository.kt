package uz.zero.auth.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import uz.zero.auth.entities.AuthAuthorization


interface AuthAuthorizationRepository : MongoRepository<AuthAuthorization, String> {
    fun findByAccessTokenValue(accessTokenValue: String): AuthAuthorization?
    fun findByRefreshTokenValue(refreshTokenValue: String): AuthAuthorization?
    fun findByRegisteredClientIdAndPrincipalName(registeredClientId: String, principalName: String): AuthAuthorization?
}
