package com.cao.repairshop.execution.dto

import io.swagger.v3.oas.annotations.media.Schema

data class ExecutionMetricsResponse(
    @Schema(description = "Average execution time of individual services in minutes", example = "45.5")
    val averageExecutionTimeMinutes: Double?
)
