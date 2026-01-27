package dasturlash.userservice

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "post-service", url = "http://localhost:8082/internal/api/v1/posts")
interface PostClient{
    @GetMapping("/get-user-posts/{userId}")
    fun getUserPosts(@PathVariable userId: Long): List<PostResponse>

    @GetMapping("/get-user-liked-posts/{userId}")
    fun getUserLikedPosts(@PathVariable userId: Long): List<PostResponse>
}