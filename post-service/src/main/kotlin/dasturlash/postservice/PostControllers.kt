package dasturlash.postservice

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/posts")
class PostController(
    private val service: PostService
) {
    @PostMapping
    fun create(@Valid @RequestBody request: PostCreateRequest) = service.create(request)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): PostResponse = service.getOne(id)

    @GetMapping
    fun getAll(): List<PostResponse> = service.getAll()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PostUpdateRequest) = service.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PostMapping("/post-like")
    fun postLike(@RequestBody request: PostLikeRequest) = service.postLike(request)

    @DeleteMapping("/post-dislike")
    fun postDislike(@RequestBody request: PostDislikeRequest) = service.postDislike(request)

    @GetMapping("/get-user-posts/{userId}")
    fun getUserPosts(@PathVariable userId: Long): UserPostsResponse = service.getUserPosts(userId)

    @GetMapping("/get-user-liked-posts/{userId}")
    fun getUserLikedPosts(@PathVariable userId: Long): List<PostResponse> = service.getUserLikedPosts(userId)
}
@RestController
@RequestMapping("/internal/api/v1/posts")
class PostInternalController(
    private val service: PostService
){

    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean = service.exists(id)

    @PutMapping("/increment-post-comment-count/{id}")
    fun incrementPostComment(@PathVariable id: Long) = service.incrementPostComment(id)

    @PutMapping("/decrement-post-comment-count/{id}")
    fun decrementPostComment(@PathVariable id: Long) = service.decrementPostComment(id)

    @DeleteMapping("/delete-comments-by-deleted-post/{postId}")
    fun deleteCommentsByPost(@PathVariable postId: Long) = service.deleteCommentsByPost(postId)

}