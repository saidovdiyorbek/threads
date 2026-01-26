package dasturlash.postservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableFeignClients
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
@EnableJpaAuditing
@SpringBootApplication
class PostServiceApplication

fun main(args: Array<String>) {
    runApplication<PostServiceApplication>(*args)
}
