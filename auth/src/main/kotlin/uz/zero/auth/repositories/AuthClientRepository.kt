package uz.zero.auth.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import uz.zero.auth.entities.AuthClient


interface AuthClientRepository : MongoRepository<AuthClient, String> {
    fun findByClientIdAndActiveTrue(clientId: String): AuthClient?
    fun existsByClientId(clientId: String): Boolean
    fun findByIdAndActiveTrue(id: String): AuthClient?
}