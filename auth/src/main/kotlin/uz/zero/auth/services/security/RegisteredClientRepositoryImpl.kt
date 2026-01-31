package uz.zero.auth.services.security

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Service
import uz.zero.auth.mappers.AuthClientMapper
import uz.zero.auth.repositories.AuthClientRepository


@Service
class RegisteredClientRepositoryImpl(
    private val authClientMapper: AuthClientMapper,
    private val clientRepository: AuthClientRepository
) : RegisteredClientRepository {
    override fun save(registeredClient: RegisteredClient) {
        clientRepository.save(authClientMapper.toEntity(registeredClient))
    }

    override fun findById(id: String): RegisteredClient? {
        return clientRepository.findByIdAndActiveTrue(id)?.let { authClientMapper.toClient(it) }
    }

    override fun findByClientId(clientId: String): RegisteredClient? {
        return clientRepository.findByClientIdAndActiveTrue(clientId)?.let { authClientMapper.toClient(it) }
    }
}