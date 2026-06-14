package com.cao.repairshop.payment.infra.gateways

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.payment.application.gateways.InvoiceGateway
import com.cao.repairshop.payment.domain.entities.Invoice
import com.cao.repairshop.payment.domain.entities.mapper.toDomain
import com.cao.repairshop.payment.infra.persistence.models.InvoiceEntity
import com.cao.repairshop.payment.infra.persistence.repositories.InvoiceRepository
import com.cao.repairshop.register.infra.persistence.repositories.CustomerRepository
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InvoiceGatewayImplJPA(
    private val invoiceRepository: InvoiceRepository,
    private val serviceOrderRepository: ServiceOrderRepository,
    private val customerRepository: CustomerRepository
) : InvoiceGateway {

    override fun save(invoice: Invoice): Invoice {
        val serviceOrderEntity = serviceOrderRepository.findById(invoice.serviceOrder.id)
            .orElseThrow { EntityNotFoundException("ServiceOrder not found with ID ${invoice.serviceOrder.id}") }
        val customerEntity = customerRepository.findById(invoice.customer.id)
            .orElseThrow { EntityNotFoundException("Customer not found with ID ${invoice.customer.id}") }

        val entity = InvoiceEntity(
            id = invoice.id,
            customer = customerEntity,
            serviceOrder = serviceOrderEntity,
            price = invoice.price,
            invoiceNumber = invoice.invoiceNumber,
            emissionDate = invoice.emissionDate,
            created = invoice.created,
            updated = invoice.updated
        )
        val saved = invoiceRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): Invoice? {
        return invoiceRepository.findDetailedById(id).orElse(null)?.toDomain()
    }

    override fun findAll(pageable: Pageable): Page<Invoice> {
        return invoiceRepository.findAll(pageable).map { it.toDomain() }
    }

    override fun findByServiceOrderId(serviceOrderId: UUID): Invoice? {
        return invoiceRepository.findByServiceOrderId(serviceOrderId)?.toDomain()
    }

    override fun findByInvoiceNumber(invoiceNumber: String): Invoice? {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)?.toDomain()
    }
}
