package uz.zero.auth.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.services.UserService

@RestController
@RequestMapping("user")
class UserController(
    private val userService: UserService
){
    @PostMapping("/register")
    fun registerUser(@RequestBody request: UserCreateRequest) = userService.registerUser(request)

    @GetMapping("/me")
    fun userMe() = userService.userMe()

    @GetMapping("/test")
    fun testAdd() = "test adding "
}