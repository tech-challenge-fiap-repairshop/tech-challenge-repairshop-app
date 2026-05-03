package com.cao.repairshop.payment.controller

import com.cao.repairshop.payment.controller.interfaces.InvoiceApi
import com.cao.repairshop.payment.dto.CreateInvoiceRequest
import com.cao.repairshop.payment.dto.InvoiceResponse
import com.cao.repairshop.payment.mapper.toResponse
import com.cao.repairshop.payment.service.InvoiceService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService
) : InvoiceApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@Valid @RequestBody request: CreateInvoiceRequest): InvoiceResponse =
        invoiceService.create(request).toResponse()

    @GetMapping
    override fun findAll(pageable: Pageable): Page<InvoiceResponse> =
        invoiceService.findAll(pageable).map { it.toResponse() }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: UUID): InvoiceResponse =
        invoiceService.findById(id).toResponse()
}
