package uz.zero.auth.components.loaders

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uz.davrbank.auth.constants.DEV_ROLE
import uz.zero.auth.entities.User
import uz.zero.auth.enums.Role
import uz.zero.auth.enums.UserStatus
import uz.zero.auth.repositories.UserRepository


@Component
class DevUserLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(this::class.java)


    @Transactional
    override fun run(vararg args: String?) {
        createDevUser()
    }


    private fun createDevUser() {
        try {
            val username = "dev"
            val email = "example@gmail.com"
            if (userRepository.findByUsernameAndDeletedFalse(username) != null) return
            logger.info("Creating dev user...")

            val devUser = User(
                fullName = username,
                username = username,
                email = email,
                password = passwordEncoder.encode("DeV#2025"),
                role = Role.DEVELOPER,
                status = UserStatus.ACTIVE,
            )
            userRepository.save(devUser)
            logger.info("Created dev user...")
        } catch (e: Exception) {
            logger.warn("Couldn't create dev user. Stacktrace: ${e.stackTraceToString()}")
        }
    }

}