package com.cao.repairshop.register.repository

import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID> {
    fun findByDocument(document: Document): Customer?
    fun findByEmail(email: Email): Customer?
}
