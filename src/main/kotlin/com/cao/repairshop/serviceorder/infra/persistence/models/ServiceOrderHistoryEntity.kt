package com.cao.repairshop.serviceorder.infra.persistence.models

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_service_order_history")
class ServiceOrderHistoryEntity(
    @Id
    @Column(name = "id_tb_service_order_history")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_order_id", nullable = false)
    var serviceOrder: ServiceOrderEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ServiceOrderStatus,

    @Column(length = 255)
    var description: String? = null,

    @Column(name = "register_time", nullable = false)
    var registerTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "interval_time")
    var intervalTime: Long? = null
) {
    override fun equals(other: Any?): Boolean = other is ServiceOrderHistoryEntity && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
