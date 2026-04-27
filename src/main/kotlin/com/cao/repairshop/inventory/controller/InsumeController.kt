package com.cao.repairshop.inventory.controller

import com.cao.repairshop.inventory.service.InsumeService
import com.cao.repairshop.inventory.dto.CreateInsumeRequest
import com.cao.repairshop.inventory.dto.UpdateInsumeRequest
import com.cao.repairshop.inventory.dto.InsumeResponse

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/insumes")
class InsumeController(
    private val insumeService: InsumeService
) : com.cao.repairshop.inventory.controller.interfaces.InsumeApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateInsumeRequest): InsumeResponse =
        insumeService.create(request)

    @GetMapping
    override fun findAll(pageable: Pageable): Page<InsumeResponse> =
        insumeService.findAll(pageable)

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: UUID): InsumeResponse =
        insumeService.findById(id)

    @PutMapping("/{id}")
    override fun update(@PathVariable id: UUID, @Valid @RequestBody request: UpdateInsumeRequest): InsumeResponse =
        insumeService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(@PathVariable id: UUID) = insumeService.delete(id)
}
