package com.cao.repairshop.payment.controller.interfaces

import com.cao.repairshop.payment.dto.CreateInvoiceRequest
import com.cao.repairshop.payment.dto.InvoiceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import java.util.UUID

@Tag(name = "Invoices", description = "Invoice management and billing")
interface InvoiceApi {

    @Operation(summary = "Create a new invoice for a finalized service order")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Invoice created successfully",
            content = [Content(schema = Schema(implementation = InvoiceResponse::class))]),
        ApiResponse(responseCode = "400", description = "Validation error", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Service order or customer not found", content = [Content()]),
        ApiResponse(responseCode = "409", description = "Invoice already exists for this service order", content = [Content()]),
        ApiResponse(responseCode = "422", description = "Service order is not FINALIZED", content = [Content()])
    )
    fun create(request: CreateInvoiceRequest): InvoiceResponse

    @Operation(summary = "List all invoices with pagination")
    @ApiResponses(ApiResponse(responseCode = "200", description = "Paginated invoice list"))
    fun findAll(@PageableDefault(size = 10) pageable: Pageable): Page<InvoiceResponse>

    @Operation(summary = "Find invoice by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Invoice found",
            content = [Content(schema = Schema(implementation = InvoiceResponse::class))]),
        ApiResponse(responseCode = "404", description = "Invoice not found", content = [Content()])
    )
    fun findById(id: UUID): InvoiceResponse
}
