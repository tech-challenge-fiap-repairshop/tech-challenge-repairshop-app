package com.cao.repairshop.serviceorder.infra.persistence.repositories

import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceOrderHistoryRepository : JpaRepository<ServiceOrderHistoryEntity, UUID>
