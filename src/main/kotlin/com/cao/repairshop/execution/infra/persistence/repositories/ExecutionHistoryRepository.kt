package com.cao.repairshop.execution.infra.persistence.repositories

import com.cao.repairshop.execution.infra.persistence.models.ExecutionHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExecutionHistoryRepository : JpaRepository<ExecutionHistoryEntity, UUID>
