package com.cao.repairshop.serviceorder.domain.entities

import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import java.time.LocalDateTime
import java.util.UUID

class ServiceOrderHistory(
    val id: UUID = UUID.randomUUID(),
    val serviceOrderId: UUID,
    var status: ServiceOrderStatus,
    var description: String? = null,
    var registerTime: LocalDateTime = LocalDateTime.now(),
    var intervalTime: Long? = null
) {
    override fun equals(other: Any?): Boolean = other is ServiceOrderHistory && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
