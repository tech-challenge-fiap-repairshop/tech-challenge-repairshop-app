package com.cao.repairshop.serviceorder.entity

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_service_order_history")
class ServiceOrderHistory(
    @Id
    @Column(name = "id_tb_service_order_history")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    var serviceOrder: ServiceOrder,

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED,

    @Column(name = "interval_time")
    var intervalTime: Long? = null,

    @Column(name = "register_time", nullable = false)
    var registerTime: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean = other is ServiceOrderHistory && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
