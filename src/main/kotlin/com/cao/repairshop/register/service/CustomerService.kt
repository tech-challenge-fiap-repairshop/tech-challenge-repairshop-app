package com.cao.repairshop.register.service

import com.cao.repairshop.core.exception.BusinessRuleViolationException
import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.dto.CreateCustomerRequest
import com.cao.repairshop.register.dto.UpdateCustomerRequest
import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.repository.CustomerRepository
import com.cao.repairshop.register.domain.ServiceOrderExistenceChecker
import com.cao.repairshop.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val serviceOrderExistenceChecker: ServiceOrderExistenceChecker,
    private val userService: UserService
) {

    @Transactional
    fun create(request: CreateCustomerRequest): Customer {
        val document = Document(request.document)
        val email = Email(request.email)

        userService.verifyRegisteredCustomer(request.email)

        customerRepository.findByDocument(document)?.let {
            throw DuplicateEntityException(ErrorMessages.Customer.DUPLICATE_DOCUMENT)
        }

        customerRepository.findByEmail(email)?.let {
            throw DuplicateEntityException(ErrorMessages.Customer.DUPLICATE_EMAIL)
        }

        val customer = Customer(
            name = request.name,
            document = document,
            email = email,
            phone = request.phone,
            birthDate = request.birthDate
        )
        return customerRepository.save(customer)
    }

    @Transactional(readOnly = true)
    fun findByEmailOrThrow(email: Email): Customer =
        customerRepository.findByEmail(email)
            ?: throw EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND)

    @Transactional(readOnly = true)
    fun findById(id: UUID): Customer {
        val customer = customerRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND) }
        return customer
    }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<Customer> =
        customerRepository.findAll(pageable)

    @Transactional
    fun update(id: UUID, request: UpdateCustomerRequest): Customer {
        val customer = customerRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND) }

        val email = request.email?.let { Email(it) }
        email?.let { e ->
            customerRepository.findByEmail(e)?.let { existingCustomer ->
                if (existingCustomer.id != id) {
                    throw DuplicateEntityException(ErrorMessages.Customer.DUPLICATE_EMAIL)
                }
            }
        }

        customer.updateDetails(
            name = request.name,
            email = email,
            phone = request.phone,
            birthDate = request.birthDate
        )

        return customerRepository.save(customer)
    }

    @Transactional
    fun delete(id: UUID) {
        val customer = customerRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND) }

        if (serviceOrderExistenceChecker.existsByCustomerId(customer.id))
            throw BusinessRuleViolationException(ErrorMessages.Customer.HAS_SERVICE_ORDERS)

        customerRepository.delete(customer)
    }
}
