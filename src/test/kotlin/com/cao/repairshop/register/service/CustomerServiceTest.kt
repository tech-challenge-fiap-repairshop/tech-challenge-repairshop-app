package com.cao.repairshop.register.service

import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.dto.*
import com.cao.repairshop.register.repository.CustomerRepository

import com.cao.repairshop.core.exception.BusinessRuleViolationException
import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.InvalidDocumentException
import com.cao.repairshop.register.domain.ServiceOrderExistenceChecker
import com.cao.repairshop.user.domain.UserRole
import com.cao.repairshop.user.entity.User
import com.cao.repairshop.user.service.UserService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var serviceOrderExistenceChecker: ServiceOrderExistenceChecker

    @MockK
    private lateinit var userService: UserService

    @InjectMockKs
    private lateinit var customerService: CustomerService

    private val defaultEmail = "test@example.com"

    private fun buildCustomer(name: String = "Test", cpf: String = "529.982.247-25") =
        Customer(name = name, document = Document(cpf))

    private fun buildUser(email: String = defaultEmail, role: UserRole = UserRole.CUSTOMER) =
        User(name = "Test User", function = role, email = email, password = "encoded")

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    fun `create success - returns CustomerResponse`() {
        val request = CreateCustomerRequest(name = "Test", document = "529.982.247-25", email = defaultEmail)
        val customer = buildCustomer()
        val user = buildUser()

        every { userService.verifyRegisteredCustomer(defaultEmail) } returns user
        every { customerRepository.findByDocument(any()) } returns null
        every { customerRepository.findByEmail(any<Email>()) } returns null
        every { customerRepository.save(any()) } returns customer

        val result = customerService.create(request)

        assertThat(result.name).isEqualTo("Test")
        verify(exactly = 1) { customerRepository.save(any()) }
    }

    @Test
    fun `create duplicate document - throws DuplicateEntityException`() {
        val request = CreateCustomerRequest(name = "Test", document = "529.982.247-25", email = defaultEmail)
        val existing = buildCustomer()
        val user = buildUser()

        every { userService.verifyRegisteredCustomer(defaultEmail) } returns user
        every { customerRepository.findByDocument(any()) } returns existing

        assertThatThrownBy { customerService.create(request) }
            .isInstanceOf(DuplicateEntityException::class.java)

        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `create invalid CPF - throws InvalidDocumentException`() {
        val request = CreateCustomerRequest(name = "Test", document = "111.111.111-11", email = defaultEmail)

        assertThatThrownBy { customerService.create(request) }
            .isInstanceOf(InvalidDocumentException::class.java)
    }

    @Test
    fun `create duplicate email - throws DuplicateEntityException`() {
        val request = CreateCustomerRequest(name = "Test", document = "529.982.247-25", email = defaultEmail)
        val user = buildUser()
        val existing = buildCustomer()

        every { userService.verifyRegisteredCustomer(defaultEmail) } returns user
        every { customerRepository.findByDocument(any()) } returns null
        every { customerRepository.findByEmail(any<Email>()) } returns existing

        assertThatThrownBy { customerService.create(request) }
            .isInstanceOf(DuplicateEntityException::class.java)

        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `create user not found for email - throws EntityNotFoundException`() {
        val request = CreateCustomerRequest(name = "Test", document = "529.982.247-25", email = defaultEmail)

        every { userService.verifyRegisteredCustomer(defaultEmail) } throws
            EntityNotFoundException("No registered user found for the provided email. The customer must have an active user account.")

        assertThatThrownBy { customerService.create(request) }
            .isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining("No registered user found")

        verify(exactly = 0) { customerRepository.save(any()) }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    fun `findById success - returns CustomerResponse`() {
        val id = UUID.randomUUID()
        val customer = buildCustomer()

        every { customerRepository.findById(id) } returns Optional.of(customer)

        val result = customerService.findById(id)

        assertThat(result.name).isEqualTo("Test")
    }

    @Test
    fun `findById not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { customerRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { customerService.findById(id) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    fun `findAll paginated - returns Page of CustomerResponse`() {
        val pageable = PageRequest.of(0, 10)
        val customer = buildCustomer()
        val page = PageImpl(listOf(customer), pageable, 1)

        every { customerRepository.findAll(pageable) } returns page

        val result = customerService.findAll(pageable)

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content).hasSize(1)
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    fun `update success - returns updated CustomerResponse`() {
        val id = UUID.randomUUID()
        val customer = buildCustomer()
        val request = UpdateCustomerRequest(name = "Updated")

        every { customerRepository.findById(id) } returns Optional.of(customer)
        every { customerRepository.findByEmail(any<Email>()) } returns null
        every { customerRepository.save(any()) } returns customer

        val result = customerService.update(id, request)

        assertThat(result).isNotNull
        verify(exactly = 1) { customerRepository.save(any()) }
    }

    @Test
    fun `update duplicate email - throws DuplicateEntityException`() {
        val id = UUID.randomUUID()
        val customer = buildCustomer()
        val otherCustomer = buildCustomer() // Different instance, different id internally
        val request = UpdateCustomerRequest(name = "Updated", email = "test@example.com")

        every { customerRepository.findById(id) } returns Optional.of(customer)
        every { customerRepository.findByEmail(any<Email>()) } returns otherCustomer

        assertThatThrownBy { customerService.update(id, request) }
            .isInstanceOf(DuplicateEntityException::class.java)

        verify(exactly = 0) { customerRepository.save(any()) }
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    fun `delete success - deletes customer when no service orders exist`() {
        val customer = buildCustomer()

        every { customerRepository.findById(customer.id) } returns Optional.of(customer)
        every { serviceOrderExistenceChecker.existsByCustomerId(customer.id) } returns false
        every { customerRepository.delete(customer) } just runs

        customerService.delete(customer.id)

        verify(exactly = 1) { customerRepository.delete(customer) }
    }

    @Test
    fun `delete blocked by OS - throws DuplicateEntityException`() {
        val customer = buildCustomer()

        every { customerRepository.findById(customer.id) } returns Optional.of(customer)
        every { serviceOrderExistenceChecker.existsByCustomerId(customer.id) } returns true
        
        assertThatThrownBy { customerService.delete(customer.id) }
            .isInstanceOf(BusinessRuleViolationException::class.java)

        verify(exactly = 0) { customerRepository.delete(any()) }
    }
}
