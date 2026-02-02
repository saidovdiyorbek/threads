package dasturlash.postservice

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): PostResponse = service.getOne(id)

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER')")
    @GetMapping
    fun getAll(): List<PostResponse> = service.getAll()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PostUpdateRequest) = service.update(id, request)

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/post-like")
    fun postLike(@RequestBody request: PostLikeRequest) = service.postLike(request)

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @DeleteMapping("/post-dislike")
    fun postDislike(@RequestBody request: PostDislikeRequest) = service.postDislike(request)

    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_DEVELOPER')")
    @GetMapping("/get-user-posts/{userId}")
    fun getUserPosts(@PathVariable userId: Long): UserPostsResponse = service.getUserPosts(userId)

    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_DEVELOPER')")
    @GetMapping("/get-user-liked-posts/{userId}")
    fun getUserLikedPosts(@PathVariable userId: Long): List<PostResponse> = service.getUserLikedPosts(userId)
}
@RestController
@RequestMapping("/internal/api/v1/posts")
class PostInternalController(
    private val service: PostService
){
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean = service.exists(id)

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/increment-post-comment-count/{id}")
    fun incrementPostComment(@PathVariable id: Long) = service.incrementPostComment(id)

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/decrement-post-comment-count/{id}")
    fun decrementPostComment(@PathVariable id: Long) = service.decrementPostComment(id)

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete-comments-by-deleted-post/{postId}")
    fun deleteCommentsByPost(@PathVariable postId: Long) = service.deleteCommentsByPost(postId)

}