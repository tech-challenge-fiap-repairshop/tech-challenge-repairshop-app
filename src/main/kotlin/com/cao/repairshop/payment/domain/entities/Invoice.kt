package com.cao.repairshop.payment.domain.entities

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class Invoice(
    val id: UUID = UUID.randomUUID(),
    var customer: Customer,
    var serviceOrder: ServiceOrder,
    var price: BigDecimal,
    var invoiceNumber: String,
    var emissionDate: LocalDateTime = LocalDateTime.now(),
    var vehiclePlate: String? = null,
    var created: LocalDateTime? = null,
    var updated: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean = other is Invoice && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Invoice(id=$id, number=$invoiceNumber)"
}
