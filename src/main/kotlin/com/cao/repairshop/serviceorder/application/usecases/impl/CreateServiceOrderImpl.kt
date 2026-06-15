package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.inventory.application.gateways.InsumeGateway
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.register.domain.entities.Email
import com.cao.repairshop.register.domain.entities.Plate
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.CreateServiceOrder
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.CreateServiceOrderRequest
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class CreateServiceOrderImpl(
    private val serviceOrderGateway: ServiceOrderGateway,
    private val customerGateway: CustomerGateway,
    private val vehicleGateway: VehicleGateway,
    private val insumeGateway: InsumeGateway
) : CreateServiceOrder {

    @Transactional
    override fun execute(request: CreateServiceOrderRequest): ServiceOrderResponse {
        val customer = customerGateway.findByEmail(Email(request.customerEmail))
            ?: throw EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND)
        val vehicle = vehicleGateway.findByPlate(Plate(request.vehiclePlate))
            ?: throw EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND)

        val order = ServiceOrder(
            customerId = customer.id,
            vehicleId = vehicle.id,
            enterTime = LocalDateTime.now()
        )

        request.services.forEach { sd ->
            val resolvedInsumes = sd.insumes.map {
                val insume = insumeGateway.findById(it.insumeId)
                    ?: throw EntityNotFoundException(ErrorMessages.Insume.notFoundById(it.insumeId))
                insume to it.quantity
            }
            order.addExecution(
                basicDescription = BasicExecution.valueOf(sd.basicDescription.uppercase()),
                fullDescription = sd.fullDescription,
                price = sd.price ?: BigDecimal.ZERO,
                estimatedTime = sd.estimatedTime,
                insumes = resolvedInsumes
            )
        }

        order.recordHistory(ServiceOrderStatus.RECEIVED)

        val saved = serviceOrderGateway.save(order)
        return saved.toResponse()
    }
}
