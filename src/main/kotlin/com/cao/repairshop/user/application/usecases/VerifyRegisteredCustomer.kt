package com.cao.repairshop.user.application.usecases

import com.cao.repairshop.user.domain.entities.User

interface VerifyRegisteredCustomer {
    fun execute(email: String): User
}
