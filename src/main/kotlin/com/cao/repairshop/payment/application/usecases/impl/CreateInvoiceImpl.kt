package com.cao.repairshop.payment.application.usecases.impl

import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.payment.application.gateways.InvoiceGateway
import com.cao.repairshop.payment.application.usecases.CreateInvoice
import com.cao.repairshop.payment.domain.entities.Invoice
import com.cao.repairshop.payment.domain.entities.mapper.toResponse
import com.cao.repairshop.payment.infra.controller.dtos.CreateInvoiceRequest
import com.cao.repairshop.payment.infra.controller.dtos.InvoiceResponse
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateInvoiceImpl(
    private val invoiceGateway: InvoiceGateway,
    private val serviceOrderGateway: ServiceOrderGateway,
    private val customerGateway: CustomerGateway,
    private val vehicleGateway: VehicleGateway
) : CreateInvoice {

    @Transactional
    override fun execute(request: CreateInvoiceRequest): InvoiceResponse {
        val serviceOrder = serviceOrderGateway.findDetailedById(request.serviceOrderId)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)
            
        validateInvoiceEligibility(serviceOrder, request.invoiceNumber)

        val customer = customerGateway.findById(serviceOrder.customerId)
            ?: throw EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND)

        val vehicle = vehicleGateway.findById(serviceOrder.vehicleId)
            ?: throw EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND)

        val invoice = Invoice(
            customer = customer,
            serviceOrder = serviceOrder,
            price = serviceOrder.totalPrice,
            invoiceNumber = request.invoiceNumber,
            vehiclePlate = vehicle.plate.value
        )

        serviceOrder.advanceStatus(ServiceOrderStatus.PAID)
        serviceOrderGateway.save(serviceOrder)
        
        return invoiceGateway.save(invoice).toResponse()
    }

    private fun validateInvoiceEligibility(serviceOrder: ServiceOrder, invoiceNumber: String) {
        if (serviceOrder.status != ServiceOrderStatus.FINALIZED)
            throw InvalidStateTransitionException(
                "Cannot create invoice: service order status must be FINALIZED, but was ${serviceOrder.status}."
            )

        invoiceGateway.findByServiceOrderId(serviceOrder.id)?.let {
            throw DuplicateEntityException(ErrorMessages.Invoice.DUPLICATE_SERVICE_ORDER)
        }

        invoiceGateway.findByInvoiceNumber(invoiceNumber)?.let {
            throw DuplicateEntityException(ErrorMessages.Invoice.DUPLICATE_NUMBER)
        }
    }
}
