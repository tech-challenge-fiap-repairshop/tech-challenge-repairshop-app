package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.persistence.models.ServiceOrderEntity
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

interface FindServiceOrder {
    fun findById(id: UUID): ServiceOrderResponse
    fun findAll(spec: Specification<ServiceOrderEntity>?, pageable: Pageable): Page<ServiceOrderResponse>
}

