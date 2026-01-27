package dasturlash.postservice

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "user-service", url = "http://localhost:8081/internal/api/v1/users")
interface UserClient{
    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean

    @PutMapping("/increment-user-post-count/{id}")
    fun incrementUserPostCount(@PathVariable id: Long)

    @PutMapping("/decrement-user-post-count/{id}")
    fun decrementUserPostCount(@PathVariable id: Long)
}

@FeignClient(name = "attach-service", url = "http://localhost:8083/internal/api/v1/attaches")
interface AttachClient{
    @GetMapping("/{hash}/exists")
    fun exists(@PathVariable hash: String): Boolean

    @PostMapping("/hashes/exists")
    fun listExists(@RequestBody hashes: List<String>): Boolean

    @DeleteMapping("/deleteList")
    fun deleteList(@RequestBody hashes: List<String>)
}