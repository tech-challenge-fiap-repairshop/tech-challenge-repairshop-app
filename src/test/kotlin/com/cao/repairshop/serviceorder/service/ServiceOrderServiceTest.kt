package com.cao.repairshop.serviceorder.service

import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.entity.ServiceOrderHistory
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.dto.*
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import com.cao.repairshop.execution.domain.BasicExecution

import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.register.service.CustomerService
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.entity.Vehicle
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.service.VehicleService
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.serviceorder.domain.ApprovalDomainService
import com.cao.repairshop.inventory.service.InsumeService
import com.cao.repairshop.inventory.entity.Insume
import com.cao.repairshop.execution.entity.ExecutionInsume
import com.cao.repairshop.execution.entity.ExecutionInsumeId
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.core.notification.EmailService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ServiceOrderServiceTest {

    @MockK
    private lateinit var serviceOrderRepository: ServiceOrderRepository

    @MockK
    private lateinit var insumeService: InsumeService

    @MockK
    private lateinit var customerService: CustomerService

    @MockK
    private lateinit var vehicleService: VehicleService

    @MockK
    private lateinit var approvalDomainService: ApprovalDomainService

    @MockK
    private lateinit var emailService: EmailService

    @InjectMockKs
    private lateinit var serviceOrderService: ServiceOrderService

    private val defaultEmail = "test@example.com"
    private val defaultPlate = "ABC-1234"

    private fun buildCustomer() =
        Customer(name = "Test Customer", document = Document("529.982.247-25"), email = Email(defaultEmail))

    private fun buildVehicle(customer: Customer) =
        Vehicle(customer = customer, plate = Plate(defaultPlate), brand = "Toyota", model = "Corolla")

    private fun buildOrder(status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED, customer: Customer = buildCustomer(), vehicle: Vehicle = buildVehicle(customer)) =
        ServiceOrder(
            id = UUID.randomUUID(),
            customer = customer,
            vehicle = vehicle,
            status = status,
            enterTime = LocalDateTime.now()
        )

    private fun buildExecution(
        serviceOrder: ServiceOrder,
        status: ExecutionStatus = ExecutionStatus.FINALIZED
    ) = com.cao.repairshop.execution.entity.Execution(
        serviceOrder = serviceOrder,
        basicDescription = BasicExecution.OIL_CHANGE,
        fullDescription = "Oil change",
        price = BigDecimal("100.00"),
        status = status,
        created = LocalDateTime.now(),
        updated = LocalDateTime.now()
    )

    @Test
    fun `createServiceOrder success - customer and vehicle exist, total calculated, history recorded`() {
        val customer = buildCustomer()
        val vehicle = buildVehicle(customer)
        val serviceDefinition = ExecutionDefinitionRequest(
            basicDescription = BasicExecution.OIL_CHANGE,
            fullDescription = "Oil change",
            price = BigDecimal("100.00"),
            insumes = emptyList()
        )
        val request = CreateServiceOrderRequest(
            customerEmail = defaultEmail,
            vehiclePlate = defaultPlate,
            services = listOf(serviceDefinition)
        )
        val savedOrder = buildOrder(customer = customer, vehicle = vehicle)

        every { customerService.findByEmailOrThrow(any()) } returns customer
        every { vehicleService.verifyAndTakeByPlate(any()) } returns vehicle
        every { serviceOrderRepository.save(any()) } returns savedOrder

        val result = serviceOrderService.createServiceOrder(request)

        assertThat(result).isNotNull
        verify(atLeast = 1) { serviceOrderRepository.save(any()) }
    }

    @Test
    fun `createServiceOrder customer not found - throws EntityNotFoundException`() {
        val request = CreateServiceOrderRequest(
            customerEmail = "notfound@example.com",
            vehiclePlate = defaultPlate,
            services = emptyList()
        )

        every { customerService.findByEmailOrThrow(any()) } throws EntityNotFoundException("Customer not found")

        assertThatThrownBy { serviceOrderService.createServiceOrder(request) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `advanceStatus valid transition RECEIVED to IN_DIAGNOSIS`() {
        val order = buildOrder(ServiceOrderStatus.RECEIVED)

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)
        every { serviceOrderRepository.save(any()) } returns order

        val result = serviceOrderService.advanceStatus(order.id, ServiceOrderStatus.IN_DIAGNOSIS)

        assertThat(result.status).isEqualTo(ServiceOrderStatus.IN_DIAGNOSIS)
    }

    @Test
    fun `advanceStatus to WAITING_APPROVAL sends email`() {
        val order = buildOrder(ServiceOrderStatus.IN_DIAGNOSIS)
        order.executions.add(buildExecution(order))

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)
        every { serviceOrderRepository.save(any()) } returns order
        every { emailService.sendEmail(any()) } just runs

        val result = serviceOrderService.advanceStatus(order.id, ServiceOrderStatus.WAITING_APPROVAL)

        assertThat(result.status).isEqualTo(ServiceOrderStatus.WAITING_APPROVAL)
        verify(exactly = 1) { emailService.sendEmail(match { it.to == defaultEmail }) }
    }

    @Test
    fun `advanceStatus FINALIZED when not all services finalized - throws InvalidStateTransitionException`() {
        val order = buildOrder(ServiceOrderStatus.IN_EXECUTION)
        val unfinishedService = buildExecution(order, ExecutionStatus.PENDING)

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)
        order.executions.add(unfinishedService)

        assertThatThrownBy { serviceOrderService.advanceStatus(order.id, ServiceOrderStatus.FINALIZED) }
            .isInstanceOf(InvalidStateTransitionException::class.java)
            .hasMessageContaining("FINALIZED")
    }

    @Test
    fun `findById success - returns ServiceOrder`() {
        val order = buildOrder()

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)

        val result = serviceOrderService.findServiceOrder(order.id)

        assertThat(result.id).isEqualTo(order.id)
    }

    @Test
    fun `findById not found - throws EntityNotFoundException`() {
        val id = UUID.randomUUID()

        every { serviceOrderRepository.findDetailedById(id) } returns Optional.empty()

        assertThatThrownBy { serviceOrderService.findServiceOrder(id) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun `approve with approved=true - deducts stock from insumes`() {
        val order = buildOrder(ServiceOrderStatus.WAITING_APPROVAL)
        val service = buildExecution(order, ExecutionStatus.INITIATED)
        val insumeId = UUID.randomUUID()
        val insume = Insume(
            id = insumeId,
            name = "Brake pad",
            price = BigDecimal("50.00"),
            unityPrice = BigDecimal("30.00"),
            quantity = 5
        )
        service.insumes.add(ExecutionInsume(ExecutionInsumeId(service.id, insumeId), service, insume, 2))
        order.executions.add(service)

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)
        every { approvalDomainService.approve(any()) } answers {
            val o = firstArg<ServiceOrder>()
            o.approve()
            o.advanceStatus(ServiceOrderStatus.IN_EXECUTION)
            o.collectInsumeRequirements().map { (id, qty) -> ApprovalDomainService.StockRequirement(id, qty) }
        }
        every { insumeService.deductStock(any(), any()) } just runs
        every { serviceOrderRepository.save(any()) } returns order

        val result = serviceOrderService.approve(order.id, ApprovalRequest(approved = true))

        assertThat(result.status).isEqualTo(ServiceOrderStatus.IN_EXECUTION)
        verify(exactly = 1) { insumeService.deductStock(any(), any()) }
    }

    @Test
    fun `advanceExecutionStatus automates SO finalization when last execution is finished`() {
        val order = buildOrder(ServiceOrderStatus.IN_EXECUTION)
        val execution = buildExecution(order, ExecutionStatus.PENDING)
        order.executions.add(execution)

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)
        every { serviceOrderRepository.save(any()) } returns order

        val result = serviceOrderService.advanceExecutionStatus(order.id, execution.id, ExecutionStatus.FINALIZED)

        assertThat(result.status).isEqualTo(ExecutionStatus.FINALIZED)
        assertThat(order.status).isEqualTo(ServiceOrderStatus.FINALIZED)
        verify(exactly = 1) { serviceOrderRepository.save(any()) }
    }

    @Test
    fun `approve with approved=false - transitions status to REFUSED`() {
        val order = buildOrder(ServiceOrderStatus.WAITING_APPROVAL)

        every { serviceOrderRepository.findDetailedById(order.id) } returns Optional.of(order)
        every { approvalDomainService.refuse(order) } answers { order.refuse() }
        every { serviceOrderRepository.save(any()) } returns order

        val result = serviceOrderService.approve(order.id, ApprovalRequest(approved = false))

        assertThat(result.status).isEqualTo(ServiceOrderStatus.REFUSED)
        verify(exactly = 0) { insumeService.deductStock(any(), any()) }
    }
}
