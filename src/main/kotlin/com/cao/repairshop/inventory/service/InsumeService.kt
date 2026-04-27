package com.cao.repairshop.inventory.service

import mu.KotlinLogging
import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.inventory.dto.CreateInsumeRequest
import com.cao.repairshop.inventory.dto.InsumeResponse
import com.cao.repairshop.inventory.dto.UpdateInsumeRequest
import com.cao.repairshop.inventory.entity.Insume
import com.cao.repairshop.inventory.mapper.toResponse
import com.cao.repairshop.inventory.repository.InsumeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class InsumeService(
    private val insumeRepository: InsumeRepository
) {

    @Transactional
    fun create(request: CreateInsumeRequest): InsumeResponse {

        val insume = Insume(
            name = request.name,
            brand = request.brand,
            quantity = request.quantity,
            price = request.price,
            unityPrice = request.unityPrice
        )
        return insumeRepository.save(insume).toResponse()
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): InsumeResponse {
        val insume = insumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Insume.NOT_FOUND) }
        return insume.toResponse()
    }

    @Transactional(readOnly = true)
    fun getEntityById(id: UUID): Insume {
        return insumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Insume.notFoundById(id)) }
    }


    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<InsumeResponse> =
        insumeRepository.findAll(pageable).map { it.toResponse() }

    @Transactional
    fun update(id: UUID, request: UpdateInsumeRequest): InsumeResponse {
        val insume = insumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Insume.NOT_FOUND) }

        insume.name = request.name
        insume.quantity = request.quantity
        insume.price = request.price
        insume.unityPrice = request.unityPrice

        return insumeRepository.save(insume).toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        val insume = insumeRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorMessages.Insume.NOT_FOUND) }
        insumeRepository.delete(insume)
    }

    @Transactional
    fun deductStock(id: UUID, amount: Int) {
        val insume = insumeRepository.findByIdForUpdate(id)
            ?: throw EntityNotFoundException(ErrorMessages.Insume.NOT_FOUND)
        logger.info { "Deducting stock: insumeId=$id, amount=$amount, currentQty=${insume.quantity}" }
        insume.deductStock(amount)
        insumeRepository.save(insume)
        logger.info { "Stock deducted: insumeId=$id, newQty=${insume.quantity}" }
    }

    @Transactional
    fun restoreStock(id: UUID, amount: Int) {
        val insume = insumeRepository.findByIdForUpdate(id)
            ?: throw EntityNotFoundException(ErrorMessages.Insume.NOT_FOUND)
        logger.info { "Restoring stock: insumeId=$id, amount=$amount, currentQty=${insume.quantity}" }
        insume.restoreStock(amount)
        insumeRepository.save(insume)
        logger.info { "Stock restored: insumeId=$id, newQty=${insume.quantity}" }
    }
}
