package com.cao.repairshop.execution.dto

import com.cao.repairshop.core.validator.annotation.SafeString
import com.cao.repairshop.execution.domain.ExecutionStatus
import com.cao.repairshop.execution.domain.BasicExecution
import com.cao.repairshop.serviceorder.dto.ExecutionDefinitionRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import com.cao.repairshop.serviceorder.dto.InsumeItemRequest

data class CreateExecutionRequest(
    @Schema(description = "Service order ID this execution belongs to")
    @field:NotNull val serviceOrderId: UUID,

    @Schema(description = "Basic service execution category", example = "OIL_CHANGE")
    @field:NotNull val basicDescription: BasicExecution,

    @Schema(description = "Detailed execution description", example = "Transmission fluid replacement using 5W30")
    @field:SafeString val fullDescription: String? = null,

    @Schema(description = "Execution price", example = "450.00")
    @field:NotNull val price: BigDecimal,

    @Schema(description = "Estimated time in hours", example = "3.0")
    val estimatedTime: BigDecimal? = null,

    @Schema(description = "List of insumes with quantities to link")
    @field:Size(max = 50, message = "Maximum of 50 insumes allowed per execution")
    val insumes: List<InsumeItemRequest> = emptyList()
)

data class CreateExecutionBatchRequest(
    @Schema(description = "Service order ID this batch belongs to")
    @field:NotNull val serviceOrderId: UUID,

    @Schema(description = "List of services to add")
    @field:Size(max = 30, message = "Maximum of 30 services allowed per batch")
    val executions: List<ExecutionDefinitionRequest>
)

data class UpdateExecutionRequest(
    @Schema(description = "Basic service execution category", example = "OIL_CHANGE")
    @field:NotNull val basicDescription: BasicExecution,

    @Schema(description = "Detailed execution description", example = "Transmission fluid replacement using 5W30")
    @field:SafeString val fullDescription: String? = null,

    @Schema(description = "Execution price", example = "450.00")
    @field:NotNull val price: BigDecimal,

    @Schema(description = "Estimated time in hours", example = "3.0")
    val estimatedTime: BigDecimal? = null
)

data class ExecutionStatusUpdateRequest(
    @Schema(description = "New execution status", examples = ["FINALIZED","INITIATED","PENDING"])
    @field:NotNull val status: ExecutionStatus
)

data class ExecutionResponse(
    @Schema(description = "Execution unique identifier")
    val id: UUID,
    @Schema(description = "Parent service order ID")
    val serviceOrderId: UUID,
    @Schema(description = "Basic service category", example = "OIL_CHANGE")
    val basicDescription: BasicExecution,
    @Schema(description = "Detailed execution description", example = "Transmission fluid replacement using 5W30")
    val fullDescription: String?,
    @Schema(description = "Execution price", example = "450.00")
    val price: BigDecimal,
    @Schema(description = "Estimated time in hours", example = "3.0")
    val estimatedTime: BigDecimal?,
    @Schema(description = "Current status", example = "INITIATED")
    val status: ExecutionStatus,
    @Schema(description = "Record creation timestamp")
    val created: LocalDateTime?,
    @Schema(description = "Last update timestamp")
    val updated: LocalDateTime?
)

data class ExecutionHistoryResponse(
    @Schema(description = "History entry ID")
    val id: UUID,
    @Schema(description = "Execution ID")
    val executionId: UUID,
    @Schema(description = "Status at this point", example = "PENDING")
    val status: ExecutionStatus,
    @Schema(description = "Timestamp of the transition")
    val registerTime: LocalDateTime,
    @Schema(description = "Seconds since previous transition")
    val intervalTime: Long?
)
