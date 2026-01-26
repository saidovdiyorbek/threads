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
){
    @PostMapping
    fun create(@Valid @RequestBody request: PostCreateRequest) = service.create(request)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): PostResponse = service.getOne(id)

    @GetMapping
    fun getAll(): List<PostResponse> = service.getAll()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PostUpdateRequest) = service.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long)  = service.delete(id)

}