package com.cao.repairshop.payment.service

import com.cao.repairshop.payment.entity.Invoice
import com.cao.repairshop.payment.dto.CreateInvoiceRequest
import com.cao.repairshop.payment.repository.InvoiceRepository

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.core.exception.DuplicateEntityException
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.exception.InvalidStateTransitionException
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.serviceorder.service.ServiceOrderService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val serviceOrderService: ServiceOrderService
) {

    @Transactional
    fun create(request: CreateInvoiceRequest): Invoice =
        serviceOrderService.findServiceOrderById(request.serviceOrderId).run {
            validateInvoiceEligibility(this, request.invoiceNumber)

            Invoice(
                customer = customer,
                serviceOrder = this,
                price = totalPrice,
                invoiceNumber = request.invoiceNumber
            ).also {
                advanceStatus(ServiceOrderStatus.PAID)
                invoiceRepository.save(it)
            }
        }

    private fun validateInvoiceEligibility(serviceOrder: ServiceOrder, invoiceNumber: String) {
        if (serviceOrder.status != ServiceOrderStatus.FINALIZED)
            throw InvalidStateTransitionException(
                "Cannot create invoice: service order status must be FINALIZED, but was ${serviceOrder.status}."
            )

        invoiceRepository.findByServiceOrderId(serviceOrder.id)?.let {
            throw DuplicateEntityException(ErrorMessages.Invoice.DUPLICATE_SERVICE_ORDER)
        }

        invoiceRepository.findByInvoiceNumber(invoiceNumber)?.let {
            throw DuplicateEntityException(ErrorMessages.Invoice.DUPLICATE_NUMBER)
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Invoice =
        invoiceRepository.findDetailedById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Invoice.NOT_FOUND) }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<Invoice> =
        invoiceRepository.findAll(pageable)
}
