package com.cao.repairshop.user.mapper

import com.cao.repairshop.user.dto.UserResponse
import com.cao.repairshop.user.entity.User

fun User.toResponse() = UserResponse(
    id = id,
    name = name,
    function = function,
    email = email,
    phone = phone
)
