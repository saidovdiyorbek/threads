package dasturlash.userservice

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
@RequestMapping("/users")
class UserController(
    private val service: UserServices
) {

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER')")
    @PostMapping
    fun create(@Valid @RequestBody request: UserCreateRequest) = service.create(request)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER', 'ROLE_USER')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): UserResponse = service.getOne(id)

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER')")
    @GetMapping
    fun getAll(): List<UserResponse> = service.getAll()

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER', 'ROLE_USER')")
    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UserUpdateRequest) = service.update(id, request)

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long)  = service.delete(id)

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/follow")
    fun follow(@RequestBody followRequest: FollowRequest) = service.follow(followRequest)

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @DeleteMapping("/unfollow")
    fun unfollow(@RequestBody unfollowRequest: UnfollowRequest) = service.unfollow(unfollowRequest)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view-profile/{id}")
    fun viewProfile(@PathVariable id: Long): ProfileResponse = service.viewProfile(id)

}

@RestController
@RequestMapping("/internal/api/v1/users")
class UserInternalController(
    private val service: UserServices
){
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/exists")
    fun exists(@PathVariable id: Long): Boolean = service.exists(id)

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/increment-user-post-count/{id}")
    fun incrementUserPostCount(@PathVariable id: Long) = service.incrementUserPostCount(id)

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/decrement-user-post-count/{id}")
    fun decrementUserPostCount(@PathVariable id: Long) = service.decrementUserPostCount(id)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/get-user-short-info")
    fun getUserShortInfo(@PathVariable id: Long): UserShortInfo? = service.getUserShortInfo(id)


}