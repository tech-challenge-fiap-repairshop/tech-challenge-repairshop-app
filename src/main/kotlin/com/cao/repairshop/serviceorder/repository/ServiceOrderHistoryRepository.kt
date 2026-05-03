package com.cao.repairshop.serviceorder.repository

import com.cao.repairshop.serviceorder.entity.ServiceOrderHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceOrderHistoryRepository : JpaRepository<ServiceOrderHistory, UUID> {
    fun findByServiceOrderIdOrderByRegisterTimeAsc(serviceOrderId: UUID): List<ServiceOrderHistory>
}
