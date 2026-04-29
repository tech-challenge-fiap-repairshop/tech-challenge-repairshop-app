package com.cao.repairshop.execution.domain

enum class ExecutionStatus(val defaultMessage: String) {
    INITIATED("Execution initiated"),
    PENDING("Execution pending"),
    FINALIZED("Execution finalized");

    fun allowedTransitions(): Set<ExecutionStatus> = when (this) {
        INITIATED -> setOf(PENDING)
        PENDING -> setOf(FINALIZED)
        FINALIZED -> emptySet()
    }
}
