package com.cao.repairshop.serviceorder.service

import mu.KotlinLogging
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.dto.CreateExecutionBatchRequest
import com.cao.repairshop.execution.dto.CreateExecutionRequest
import com.cao.repairshop.execution.dto.ExecutionResponse
import com.cao.repairshop.execution.dto.UpdateExecutionRequest
import com.cao.repairshop.execution.entity.Execution
import com.cao.repairshop.execution.mapper.toResponse
import com.cao.repairshop.inventory.service.InsumeService
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.domain.Plate
import com.cao.repairshop.register.service.CustomerService
import com.cao.repairshop.register.service.VehicleService
import com.cao.repairshop.serviceorder.domain.ApprovalDomainService
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.dto.ApprovalRequest
import com.cao.repairshop.serviceorder.dto.CreateServiceOrderRequest
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
import org.hibernate.Hibernate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class ServiceOrderService(
    private val serviceOrderRepository: ServiceOrderRepository,
    private val customerService: CustomerService,
    private val vehicleService: VehicleService,
    private val insumeService: InsumeService,
    private val approvalDomainService: ApprovalDomainService
) {

    // ---- Service Order lifecycle ----

    @Transactional
    fun createServiceOrder(request: CreateServiceOrderRequest): ServiceOrder {
        logger.info { "Creating service order for customer=${request.customerEmail}, vehicle=${request.vehiclePlate}, services=${request.services.size}" }
        val customer = customerService.findByEmailOrThrow(Email(request.customerEmail))
        val vehicle = vehicleService.verifyAndTakeByPlate(Plate(request.vehiclePlate))

        val order = ServiceOrder(
            customer = customer,
            vehicle = vehicle,
            enterTime = LocalDateTime.now()
        )

        request.services.forEach { sd ->
            val resolvedInsumes = sd.insumes.map { insumeService.getEntityById(it.insumeId) to it.quantity }
            order.addExecution(
                basicDescription = sd.basicDescription,
                fullDescription = sd.fullDescription,
                price = sd.price ?: BigDecimal.ZERO,
                estimatedTime = sd.estimatedTime,
                insumes = resolvedInsumes
            )
        }

        order.recordHistory(ServiceOrderStatus.RECEIVED)

        val saved = serviceOrderRepository.save(order)
        logger.info { "Service order created: id=${saved.id}, status=${saved.status}, totalPrice=${saved.totalPrice}" }
        return saved
    }

    @Transactional(readOnly = true)
    fun findServiceOrder(id: UUID): ServiceOrder = findServiceOrderById(id)

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<ServiceOrder> = serviceOrderRepository.findAll(pageable)

    @Transactional
    fun advanceStatus(id: UUID, newStatus: ServiceOrderStatus): ServiceOrder {
        val order = findServiceOrderById(id)
        order.advanceStatus(newStatus)
        return serviceOrderRepository.save(order)
    }

    @Transactional
    fun approve(id: UUID, request: ApprovalRequest): ServiceOrder {
        logger.info { "Processing approval for service order $id, approved=${request.approved}" }
        val order = findServiceOrderById(id)

        if (request.approved) {
            val stockRequirements = approvalDomainService.approve(order)
            stockRequirements.forEach { req ->
                insumeService.deductStock(req.insumeId, req.quantity)
            }
        } else {
            approvalDomainService.refuse(order)
        }

        return serviceOrderRepository.save(order)
    }

    // ---- Execution operations (child entity within aggregate) ----

    @Transactional
    fun addExecution(serviceOrderId: UUID, request: CreateExecutionRequest): ExecutionResponse {
        val order = findServiceOrderById(serviceOrderId)

        val resolvedInsumes = request.insumes.map { insumeService.getEntityById(it.insumeId) to it.quantity }
        val execution = order.addExecution(
            basicDescription = request.basicDescription,
            fullDescription = request.fullDescription,
            price = request.price,
            estimatedTime = request.estimatedTime,
            insumes = resolvedInsumes
        )
        execution.recordHistory(execution.status)

        serviceOrderRepository.save(order)
        return execution.toResponse()
    }

    @Transactional
    fun addExecutionBatch(serviceOrderId: UUID, request: CreateExecutionBatchRequest): List<ExecutionResponse> {
        val order = findServiceOrderById(serviceOrderId)

        val executions = request.executions.map { execDef ->
            val resolvedInsumes = execDef.insumes.map { insumeService.getEntityById(it.insumeId) to it.quantity }
            val execution = order.addExecution(
                basicDescription = execDef.basicDescription,
                fullDescription = execDef.fullDescription,
                price = execDef.price ?: BigDecimal.ZERO,
                estimatedTime = execDef.estimatedTime,
                insumes = resolvedInsumes
            )
            execution.recordHistory(execution.status)
            execution
        }

        serviceOrderRepository.save(order)
        return executions.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findExecution(serviceOrderId: UUID, executionId: UUID): ExecutionResponse {
        val order = findServiceOrderById(serviceOrderId)
        return findExecutionInOrder(order, executionId).toResponse()
    }

    @Transactional
    fun updateExecution(serviceOrderId: UUID, executionId: UUID, request: UpdateExecutionRequest): ExecutionResponse {
        val order = findServiceOrderById(serviceOrderId)
        val execution = findExecutionInOrder(order, executionId)

        execution.apply {
            basicDescription = request.basicDescription
            fullDescription = request.fullDescription
            price = request.price
            estimatedTime = request.estimatedTime
            updated = LocalDateTime.now()
        }

        order.recalculateTotalPrice()
        serviceOrderRepository.save(order)
        return execution.toResponse()
    }

    @Transactional
    fun removeExecution(serviceOrderId: UUID, executionId: UUID) {
        val order = findServiceOrderById(serviceOrderId)
        val execution = findExecutionInOrder(order, executionId)

        order.executions.remove(execution)
        order.recalculateTotalPrice()
        serviceOrderRepository.save(order)
    }

    @Transactional
    fun advanceExecutionStatus(serviceOrderId: UUID, executionId: UUID, newStatus: ExecutionStatus): ExecutionResponse {
        val order = findServiceOrderById(serviceOrderId)
        val execution = findExecutionInOrder(order, executionId)

        execution.advanceStatus(newStatus)
        serviceOrderRepository.save(order)
        return execution.toResponse()
    }

    // ---- Internal helpers ----

    fun findServiceOrderById(id: UUID): ServiceOrder {
        val order = serviceOrderRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND) }
        Hibernate.initialize(order.executions)
        Hibernate.initialize(order.histories)
        order.executions.forEach { Hibernate.initialize(it.insumes) }
        return order
    }

    private fun findExecutionInOrder(order: ServiceOrder, executionId: UUID): Execution =
        order.executions.find { it.id == executionId }
            ?: throw EntityNotFoundException(ErrorMessages.Execution.NOT_FOUND)
}
