package com.cao.repairshop.register.service

import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.domain.ServiceOrderExistenceChecker
import com.cao.repairshop.register.dto.*
import com.cao.repairshop.register.repository.VehicleRepository

import com.cao.repairshop.core.exception.BusinessRuleViolationException
import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.InvalidPlateException
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
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class VehicleServiceTest {

    @MockK
    private lateinit var vehicleRepository: VehicleRepository

    @MockK
    private lateinit var customerService: CustomerService

    @MockK
    private lateinit var serviceOrderExistenceChecker: ServiceOrderExistenceChecker

    @InjectMockKs
    private lateinit var vehicleService: VehicleService

    private val defaultEmail = "test@example.com"

    private fun buildCustomer() =
        Customer(name = "Test Customer", document = Document("529.982.247-25"))

    private fun buildVehicle(customer: Customer) =
        Vehicle(
            customer = customer,
            plate = Plate("ABC-1234"),
            brand = "Toyota",
            model = "Corolla"
        )

    private fun buildCreateRequest(
        customerEmail: String = defaultEmail,
        plate: String = "ABC-1234",
        manufacturingDate: LocalDate? = null
    ) = CreateVehicleRequest(
        customerEmail = customerEmail,
        plate = plate,
        brand = "Toyota",
        model = "Corolla",
        manufacturingDate = manufacturingDate
    )

    @Test
    fun `create success - customer exists, plate valid`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)
        val request = buildCreateRequest(customerEmail = defaultEmail)

        every { vehicleRepository.findByPlate(any()) } returns null
        every { customerService.findByEmailOrThrow(any()) } returns customer
        every { vehicleRepository.save(any()) } returns vehicle

        val result = vehicleService.create(request)

        assertThat(result.brand).isEqualTo("Toyota")
        verify(exactly = 1) { vehicleRepository.save(any()) }
    }

    @Test
    fun `create duplicate plate - throws DuplicateEntityException`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)
        val request = buildCreateRequest(customerEmail = defaultEmail)

        every { vehicleRepository.findByPlate(any()) } returns vehicle

        assertThatThrownBy { vehicleService.create(request) }
            .isInstanceOf(DuplicateEntityException::class.java)

        verify(exactly = 0) { vehicleRepository.save(any()) }
    }

    @Test
    fun `create customer not found - throws EntityNotFoundException`() {
        val request = buildCreateRequest(customerEmail = defaultEmail)

        every { vehicleRepository.findByPlate(any()) } returns null
        every { customerService.findByEmailOrThrow(any()) } throws EntityNotFoundException("Customer not found")

        assertThatThrownBy { vehicleService.create(request) }
            .isInstanceOf(EntityNotFoundException::class.java)

        verify(exactly = 0) { vehicleRepository.save(any()) }
    }

    @Test
    fun `create invalid plate - throws InvalidPlateException`() {
        val request = buildCreateRequest(customerEmail = defaultEmail, plate = "INVALID")

        assertThatThrownBy { vehicleService.create(request) }
            .isInstanceOf(InvalidPlateException::class.java)
    }

    @Test
    fun `findAll paginated - returns Page of VehicleResponse`() {
        val pageable = PageRequest.of(0, 10)
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)
        val page = PageImpl(listOf(vehicle), pageable, 1)

        every { vehicleRepository.findAll(pageable) } returns page

        val result = vehicleService.findAll(pageable)

        assertThat(result.totalElements).isEqualTo(1)
    }

    @Test
    fun `findById success - returns VehicleResponse`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)

        every { vehicleRepository.findById(vehicle.id) } returns Optional.of(vehicle)

        val result = vehicleService.findById(vehicle.id)

        assertThat(result.brand).isEqualTo("Toyota")
    }

    @Test
    fun `findById not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { vehicleRepository.findById(id) } returns Optional.empty()

        assertThatThrownBy { vehicleService.findById(id) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `update success - returns updated VehicleResponse`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)
        val request = UpdateVehicleRequest(plate = "ABC-1234", brand = "Honda", model = "Civic")

        every { vehicleRepository.findById(vehicle.id) } returns Optional.of(vehicle)
        every { vehicleRepository.findByPlate(any()) } returns null
        every { vehicleRepository.save(any()) } returns vehicle

        val result = vehicleService.update(vehicle.id, request)

        assertThat(result).isNotNull
        verify(exactly = 1) { vehicleRepository.save(any()) }
    }

    @Test
    fun `update duplicate plate - throws DuplicateEntityException`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)
        val otherVehicle = buildVehicle(customer)
        val request = UpdateVehicleRequest(plate = "ABC-1234", brand = "Honda", model = "Civic")

        every { vehicleRepository.findById(vehicle.id) } returns Optional.of(vehicle)
        every { vehicleRepository.findByPlate(any()) } returns otherVehicle

        assertThatThrownBy { vehicleService.update(vehicle.id, request) }
            .isInstanceOf(DuplicateEntityException::class.java)

        verify(exactly = 0) { vehicleRepository.save(any()) }
    }

    @Test
    fun `delete success - deletes vehicle when no service orders exist`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)

        every { vehicleRepository.findById(vehicle.id) } returns Optional.of(vehicle)
        every { serviceOrderExistenceChecker.existsByVehicleId(vehicle.id) } returns false
        every { vehicleRepository.delete(vehicle) } just runs

        vehicleService.delete(vehicle.id)

        verify(exactly = 1) { vehicleRepository.delete(vehicle) }
    }

    @Test
    fun `delete blocked by OS - throws BusinessRuleViolationException`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)

        every { vehicleRepository.findById(vehicle.id) } returns Optional.of(vehicle)
        every { serviceOrderExistenceChecker.existsByVehicleId(vehicle.id) } returns true

        assertThatThrownBy { vehicleService.delete(vehicle.id) }
            .isInstanceOf(BusinessRuleViolationException::class.java)

        verify(exactly = 0) { vehicleRepository.delete(any()) }
    }
}
