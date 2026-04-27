package com.cao.repairshop.serviceorder.domain

enum class ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    WAITING_APPROVAL,
    APPROVED,
    REFUSED,
    IN_EXECUTION,
    FINALIZED,
    PAID,
    CANCELED;

    fun allowedTransitions(): Set<ServiceOrderStatus> = when (this) {
        RECEIVED -> setOf(IN_DIAGNOSIS)
        IN_DIAGNOSIS -> setOf(WAITING_APPROVAL)
        WAITING_APPROVAL -> setOf(APPROVED, REFUSED)
        APPROVED -> setOf(IN_EXECUTION)
        REFUSED -> setOf(CANCELED)
        IN_EXECUTION -> setOf(FINALIZED)
        FINALIZED -> setOf(PAID)
        PAID -> emptySet()
        CANCELED -> emptySet()
    }
}
