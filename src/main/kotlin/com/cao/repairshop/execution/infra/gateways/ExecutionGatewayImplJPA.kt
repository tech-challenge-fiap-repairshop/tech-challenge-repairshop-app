package com.cao.repairshop.execution.infra.gateways

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.execution.application.gateways.ExecutionGateway
import com.cao.repairshop.execution.domain.entities.Execution
import com.cao.repairshop.execution.domain.entities.mapper.toDomain
import com.cao.repairshop.execution.domain.entities.mapper.toEntity
import com.cao.repairshop.execution.infra.persistence.repositories.ExecutionRepository
import com.cao.repairshop.serviceorder.infra.persistence.repositories.ServiceOrderRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ExecutionGatewayImplJPA(
    private val executionRepository: ExecutionRepository,
    private val serviceOrderRepository: ServiceOrderRepository
) : ExecutionGateway {

    override fun save(execution: Execution): Execution {
        val serviceOrderEntity = serviceOrderRepository.findById(execution.serviceOrderId)
            .orElseThrow { EntityNotFoundException("ServiceOrder not found with ID ${execution.serviceOrderId}") }
        val entity = execution.toEntity(serviceOrderEntity)
        val saved = executionRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): Execution? {
        return executionRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByServiceOrderId(serviceOrderId: UUID): List<Execution> {
        return executionRepository.findByServiceOrderId(serviceOrderId).map { it.toDomain() }
    }

    override fun delete(id: UUID) {
        executionRepository.deleteById(id)
    }

    override fun getAverageExecutionTimeMinutes(): Double? {
        return executionRepository.getAverageExecutionTimeMinutes()
    }
}
