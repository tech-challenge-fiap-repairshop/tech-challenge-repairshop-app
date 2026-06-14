package com.cao.repairshop.execution.infra.controller.dtos

import io.swagger.v3.oas.annotations.media.Schema

data class ExecutionMetricsResponse(
    @Schema(description = "Average execution time in minutes", example = "45.2")
    val averageExecutionTimeMinutes: Double?
)
