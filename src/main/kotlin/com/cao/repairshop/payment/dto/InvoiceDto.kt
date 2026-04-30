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
    @field:NotBlank @field:SafeString val invoiceNumber: String
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
    @Schema(description = "Vehicle plate linked to the order", example = "ABC-1234")
    val vehiclePlate: String,
    @Schema(description = "Current status of the service order", example = "FINALIZED")
    val serviceOrderStatus: String,
    @Schema(description = "Invoice number", example = "NF-2026-000001")
    val invoiceNumber: String,
    @Schema(description = "Final invoice price", example = "1500.00")
    val price: BigDecimal,
    @Schema(description = "Emission date")
    val emissionDate: LocalDateTime,
    @Schema(description = "Detailed breakdown of services and parts")
    val items: List<InvoiceItemResponse> = emptyList(),
    @Schema(description = "Record creation timestamp")
    val created: LocalDateTime?,
    @Schema(description = "Last update timestamp")
    val updated: LocalDateTime?
)

data class InvoiceItemResponse(
    @Schema(description = "Service description", example = "OIL_CHANGE")
    val description: String,
    @Schema(description = "Labor price", example = "150.00")
    val laborPrice: BigDecimal,
    @Schema(description = "List of parts/insumes used in this service")
    val insumes: List<InvoiceInsumeResponse>,
    @Schema(description = "Total price for this service item (labor + insumes)", example = "350.00")
    val totalItemPrice: BigDecimal
)

data class InvoiceInsumeResponse(
    @Schema(description = "Insume name", example = "Synthetic Oil 5W30")
    val name: String,
    @Schema(description = "Quantity used", example = "4")
    val quantity: Int,
    @Schema(description = "Unit price", example = "50.00")
    val unitPrice: BigDecimal,
    @Schema(description = "Total price for this insume (unit price * quantity)", example = "200.00")
    val totalPrice: BigDecimal
)
