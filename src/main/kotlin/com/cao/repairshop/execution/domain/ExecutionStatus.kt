package com.cao.repairshop.execution.domain

enum class ExecutionStatus {
    INITIATED,
    PENDING,
    FINALIZED;

    fun allowedTransitions(): Set<ExecutionStatus> = when (this) {
        INITIATED -> setOf(PENDING)
        PENDING -> setOf(FINALIZED)
        FINALIZED -> emptySet()
    }
}
