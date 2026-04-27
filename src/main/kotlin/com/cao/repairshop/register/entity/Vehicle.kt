package com.cao.repairshop.register.entity

import com.cao.repairshop.register.domain.Plate

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import com.cao.repairshop.register.repository.converter.PlateConverter
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.CascadeType
import jakarta.persistence.Table
import com.cao.repairshop.serviceorder.entity.ServiceOrder
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_vehicle")
class Vehicle(
    @Id
    @Column(name = "id_tb_vehicle")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer,

    @Convert(converter = PlateConverter::class)
    @Column(name = "plate", nullable = false, unique = true, length = 7)
    var plate: Plate,

    @Column(nullable = false, length = 50)
    var brand: String,

    @Column(nullable = false, length = 80)
    var model: String,

    @Column(length = 30)
    var color: String? = null,

    @Column(name = "manufacturing_date")
    var manufacturingDate: LocalDate? = null,

    @Column(name = "last_maintenance")
    var lastMaintenance: LocalDateTime? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    var serviceOrders: MutableSet<ServiceOrder> = mutableSetOf(),

    @Column(nullable = false)
    var created: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updated: LocalDateTime = LocalDateTime.now()
) {
    fun updateDetails(plate: Plate, brand: String, model: String, color: String?, manufacturingDate: LocalDate?) {
        this.plate = plate
        this.brand = brand
        this.model = model
        this.color = color
        this.manufacturingDate = manufacturingDate
        this.updated = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean = other is Vehicle && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Vehicle(id=$id, plate=${plate.normalized})"
}
