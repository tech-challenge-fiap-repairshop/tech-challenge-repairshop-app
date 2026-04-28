package com.cao.repairshop.payment.dto

import com.cao.repairshop.core.validator.annotation.SafeString
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CreateInvoiceRequest(
    @Schema(description = "Service Order ID to invoice", example = "550e8400-e29b-41d4-a716-446655440000")
    @field:NotNull val serviceOrderId: UUID,

    @Schema(description = "Invoice number", example = "NF-2026-000001")
    @field:NotBlank @field:SafeString val invoiceNumber: String,

    @Schema(description = "Invoice price", example = "1500.00")
    @field:NotNull @field:Positive val price: BigDecimal
)

data class InvoiceResponse(
    @Schema(description = "Invoice unique identifier")
    val id: UUID,
    @Schema(description = "Customer ID")
    val customerId: UUID,
    @Schema(description = "Customer name")
    val customerName: String,
    @Schema(description = "Service Order ID")
    val serviceOrderId: UUID,
    @Schema(description = "Invoice number", example = "NF-2026-000001")
    val invoiceNumber: String,
    @Schema(description = "Invoice price", example = "1500.00")
    val price: BigDecimal,
    @Schema(description = "Emission date")
    val emissionDate: LocalDateTime,
    @Schema(description = "Record creation timestamp")
    val created: LocalDateTime?,
    @Schema(description = "Last update timestamp")
    val updated: LocalDateTime?
)
