package dasturlash.postservice

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "user-service", url = "http://localhost:8081/internal/api/v1/users")
interface UserClient{
    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean
}

@FeignClient(name = "attach-service", url = "http://localhost:8083/internal/api/v1/attaches")
interface AttachClient{
    @GetMapping("/{hash}/exists")
    fun exists(@PathVariable hash: String): Boolean
}