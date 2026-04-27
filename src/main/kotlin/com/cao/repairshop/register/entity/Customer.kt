package com.cao.repairshop.register.entity

import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import com.cao.repairshop.register.repository.converter.DocumentConverter
import com.cao.repairshop.register.repository.converter.EmailConverter
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import com.cao.repairshop.payment.entity.Invoice
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_customer")
class Customer(
    @Id
    @Column(name = "id_tb_customer")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 150)
    var name: String,

    @Convert(converter = DocumentConverter::class)
    @Column(name = "document", nullable = false, unique = true, length = 14)
    var document: Document,

    @Convert(converter = EmailConverter::class)
    @Column(name = "email", unique = true, length = 255)
    var email: Email? = null,

    @Column(length = 20)
    var phone: String? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], orphanRemoval = true)
    var vehicles: MutableSet<Vehicle> = mutableSetOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    var serviceOrders: MutableSet<ServiceOrder> = mutableSetOf(),

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], orphanRemoval = true)
    var invoices: MutableSet<Invoice> = mutableSetOf(),

    @Column(nullable = false)
    var created: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updated: LocalDateTime = LocalDateTime.now()
) {
    fun updateDetails(name: String, email: Email?, phone: String?, birthDate: LocalDate?) {
        this.name = name
        this.email = email
        this.phone = phone
        this.birthDate = birthDate
        this.updated = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean = other is Customer && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Customer(id=$id, name=$name)"
}
