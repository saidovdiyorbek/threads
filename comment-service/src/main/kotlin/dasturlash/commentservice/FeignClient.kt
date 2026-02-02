package dasturlash.commentservice

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "user-service", url = "\${services.hosts.user}/internal/api/v1/users", configuration = [FeignOAuth2TokenConfig::class])
interface UserClient {
    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean

    @GetMapping("/{id}/get-user-short-info")
    fun getUserShortInfo(@PathVariable id: Long): UserShortInfo?
}

@FeignClient(name = "post-service", url = "\${services.hosts.post}/internal/api/v1/posts", configuration = [FeignOAuth2TokenConfig::class])
interface PostClient {
    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean

    @PutMapping("/increment-post-comment-count/{id}")
    fun incrementPostComment(@PathVariable id: Long)

    @PutMapping("/decrement-post-comment-count/{id}")
    fun decrementPostComment(@PathVariable id: Long)
}

@FeignClient(name = "attach-service", url = "\${services.hosts.attach}/internal/api/v1/attaches", configuration = [FeignOAuth2TokenConfig::class])
interface AttachClient{
    @PostMapping("/exists")
    fun exists(@RequestBody hash: InternalHashCheckRequest): Boolean

    @PostMapping("/hashes/exists")
    fun listExists(@RequestBody hashes: InternalHashesCheckRequest): Boolean

    @DeleteMapping("/deleteList")
    fun deleteList(@RequestBody hashes: List<String>)
}