package com.cao.repairshop.payment.entity

import com.cao.repairshop.register.entity.Customer
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_invoice")
class Invoice(
    @Id
    @Column(name = "id_tb_invoice")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false, unique = true)
    var serviceOrder: ServiceOrder,

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
    override fun equals(other: Any?): Boolean = other is Invoice && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Invoice(id=$id, number=$invoiceNumber)"
}

