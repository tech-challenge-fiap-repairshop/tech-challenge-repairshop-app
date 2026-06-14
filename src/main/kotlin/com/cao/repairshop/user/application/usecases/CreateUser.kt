package com.cao.repairshop.user.application.usecases

import com.cao.repairshop.user.infra.controller.dtos.CreateUserRequest
import com.cao.repairshop.user.infra.controller.dtos.UserResponse

interface CreateUser {
    fun execute(request: CreateUserRequest): UserResponse
}
