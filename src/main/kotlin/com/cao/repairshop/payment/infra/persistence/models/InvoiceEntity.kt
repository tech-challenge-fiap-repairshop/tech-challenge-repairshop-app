package com.cao.repairshop.payment.infra.persistence.models

import com.cao.repairshop.register.infra.persistence.models.CustomerEntity
import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_invoice")
class InvoiceEntity(
    @Id
    @Column(name = "id_tb_invoice")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: CustomerEntity,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false, unique = true)
    var serviceOrder: ServiceOrderEntity,

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal,

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    var invoiceNumber: String,

    @Column(name = "emission_date", nullable = false)
    var emissionDate: LocalDateTime = LocalDateTime.now(),

    @CreationTimestamp
    @Column(nullable = false)
    var created: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updated: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean = other is InvoiceEntity && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "InvoiceEntity(id=$id, number=$invoiceNumber)"
}
