package dasturlash.commentservice

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
@RequestMapping("/comments")
class CommentController(
    private val service: CommentService
){
    @PostMapping
    fun create(@Valid @RequestBody request: CommentCreateRequest) = service.create(request)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): CommentResponse = service.getOne(id)

    @GetMapping
    fun getAll(): List<CommentResponse> = service.getAll()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: CommentUpdateRequest) = service.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long)  = service.delete(id)

    @GetMapping("/get-comments-by-userId/{userId}")
    fun getCommentsByUserId(@PathVariable userId: Long): UserCommentsResponse = service.getCommentsByUserId(userId)

    @GetMapping("/get-comments-by-postId/{postId}")
    fun getCommentsByPostId(@PathVariable postId: Long): PostCommentsResponse = service.getCommentsByPostId(postId)

    @GetMapping("/get-comments-by-parent/{parentId}")
    fun getCommentsByParent(@PathVariable parentId: Long): ParentCommentsResponse = service.getCommentsByParent(parentId)

    @PostMapping("/like-comment")
    fun likeComment(@RequestBody likeBody: LikeRequest) = service.likeComment(likeBody)

    @DeleteMapping("/dislike-comment")
    fun dislikeComment(@RequestBody dislikeBody: LikeRequest) = service.dislikeComment(dislikeBody)
}