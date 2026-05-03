package com.cao.repairshop.register.entity

import com.cao.repairshop.payment.entity.Invoice
import com.cao.repairshop.register.domain.Document
import com.cao.repairshop.register.domain.Email
import com.cao.repairshop.register.repository.converter.DocumentConverter
import com.cao.repairshop.register.repository.converter.EmailConverter
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_customer")
data class Customer(
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

    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var invoices: MutableSet<Invoice> = mutableSetOf(),

    @CreationTimestamp
    @Column(nullable = false)
    var created: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updated: LocalDateTime? = null
) {
    fun updateDetails(name: String, email: Email?, phone: String?, birthDate: LocalDate?) {
        this.name = name
        this.email = email
        this.phone = phone
        this.birthDate = birthDate
    }

    override fun equals(other: Any?): Boolean = other is Customer && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Customer(id=$id, name=$name)"
}

