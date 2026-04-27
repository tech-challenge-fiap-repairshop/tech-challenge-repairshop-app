package com.cao.repairshop.register.mapper

import com.cao.repairshop.register.dto.CustomerResponse
import com.cao.repairshop.register.entity.Customer


fun Customer.toResponse() = CustomerResponse(
    id = id,
    name = name,
    document = document.normalized,
    email = email?.value,
    phone = phone,
    birthDate = birthDate,
    created = created,
    updated = updated
)

fun List<Customer>.toResponse() = map { it.toResponse() }