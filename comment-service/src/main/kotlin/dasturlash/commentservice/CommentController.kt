package dasturlash.commentservice

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
@RequestMapping("/comments")
class CommentController(
    private val service: CommentService
){
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    fun create(@Valid @RequestBody request: CommentCreateRequest) = service.create(request)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): CommentResponse = service.getOne(id)

    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @GetMapping
    fun getAll(): List<CommentResponse> = service.getAll()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: CommentUpdateRequest) = service.update(id, request)

    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long)  = service.delete(id)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-comments-by-userId/{userId}")
    fun getCommentsByUserId(@PathVariable userId: Long): UserCommentsResponse = service.getCommentsByUserId(userId)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-comments-by-postId/{postId}")
    fun getCommentsByPostId(@PathVariable postId: Long): PostCommentsResponse = service.getCommentsByPostId(postId)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-comments-by-parent/{parentId}")
    fun getCommentsByParent(@PathVariable parentId: Long): ParentCommentsResponse = service.getCommentsByParent(parentId)

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/like-comment")
    fun likeComment(@RequestBody likeBody: LikeRequest) = service.likeComment(likeBody)

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/dislike-comment")
    fun dislikeComment(@RequestBody dislikeBody: LikeRequest) = service.dislikeComment(dislikeBody)
}