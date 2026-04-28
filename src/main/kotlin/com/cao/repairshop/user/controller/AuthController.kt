package com.cao.repairshop.user.controller

import com.cao.repairshop.user.controller.interfaces.AuthApi
import com.cao.repairshop.user.service.UserService
import com.cao.repairshop.user.dto.CreateUserRequest
import com.cao.repairshop.user.dto.LoginRequest
import com.cao.repairshop.user.dto.TokenResponse
import com.cao.repairshop.user.dto.UserResponse

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) : AuthApi {

    @PostMapping("/login")
    override fun login(@Valid @RequestBody request: LoginRequest): TokenResponse =
        userService.authenticate(request)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    override fun register(@Valid @RequestBody request: CreateUserRequest): UserResponse =
        userService.createUser(request)
}
