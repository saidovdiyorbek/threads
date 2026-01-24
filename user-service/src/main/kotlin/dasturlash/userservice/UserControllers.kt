package dasturlash.userservice

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
@RequestMapping("/users")
class UserController(
    private val service: UserServices
) {

    @PostMapping
    fun create(@Valid @RequestBody request: UserCreateRequest) = service.create(request)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): UserResponse = service.getOne(id)

    @GetMapping
    fun getAll(): List<UserResponse> = service.getAll()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UserUpdateRequest) = service.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long)  = service.delete(id)

    @PostMapping("/follow")
    fun follow(@RequestBody followRequest: FollowRequest) = service.follow(followRequest)

    @DeleteMapping("/unfollow")
    fun unfollow(@RequestBody unfollowRequest: UnfollowRequest) = service.unfollow(unfollowRequest)

    @GetMapping("/view-profile/{id}")
    fun viewProfile(@PathVariable id: Long): ProfileResponse = service.viewProfile(id)
}