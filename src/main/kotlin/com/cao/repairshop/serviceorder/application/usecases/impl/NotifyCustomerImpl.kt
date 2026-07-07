package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.notification.EmailService
import com.cao.repairshop.core.notification.dto.EmailRequest
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.serviceorder.application.usecases.NotifyCustomer
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Service

@Service
class NotifyCustomerImpl(
    private val customerGateway: CustomerGateway,
    private val vehicleGateway: VehicleGateway,
    private val emailService: EmailService
) : NotifyCustomer {

    override fun execute(order: ServiceOrder, status: ServiceOrderStatus) {
        val strategy = status.strategy
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
