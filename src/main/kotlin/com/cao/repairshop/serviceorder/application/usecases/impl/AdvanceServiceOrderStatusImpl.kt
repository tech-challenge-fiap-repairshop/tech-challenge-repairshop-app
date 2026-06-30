package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.notification.EmailService
import com.cao.repairshop.core.notification.dto.EmailRequest
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.AdvanceServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.EmailStrategy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdvanceServiceOrderStatusImpl(
    private val serviceOrderGateway: ServiceOrderGateway,
    private val customerGateway: CustomerGateway,
    private val vehicleGateway: VehicleGateway,
    private val emailService: EmailService,
    emailStrategies: List<EmailStrategy>
) : AdvanceServiceOrderStatus {

    private val strategyMap = emailStrategies.associateBy { it.status }

    @Transactional
    override fun execute(id: UUID, newStatus: ServiceOrderStatus): ServiceOrderResponse {
        val order = serviceOrderGateway.findDetailedById(id)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)

        order.advanceStatus(newStatus)
        val saved = serviceOrderGateway.save(order)

        strategyMap[newStatus]?.let { strategy ->
            notifyCustomer(saved, strategy)
        }

        return saved.toResponse()
    }

    private fun notifyCustomer(order: ServiceOrder, strategy: EmailStrategy) {
        val customer = customerGateway.findById(order.customerId)
            ?: throw EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND)

        customer.email?.let { customerEmail ->
            val vehicle = vehicleGateway.findById(order.vehicleId)
                ?: throw EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND)

            val request = EmailRequest(
                to = customerEmail.value,
                subject = strategy.formatSubject(order),
                body = strategy.formatBody(customer, order, vehicle)
            )
            emailService.sendEmail(request)
        }
    }
}