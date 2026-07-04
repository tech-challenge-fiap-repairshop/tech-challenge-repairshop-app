package com.cao.repairshop.serviceorder.domain

import com.cao.repairshop.core.exception.ErrorMessages.ServiceOrder
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.ApprovedEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.CanceledEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.EmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.FinalizedEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.InDiagnosisEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.InExecutionEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.PaidEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.ReceivedEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.RefusedEmailStrategy
import com.cao.repairshop.serviceorder.application.usecases.impl.strategies.WaitingApprovalEmailStrategy

enum class ServiceOrderStatus(
    val defaultMessage: String,
    val strategy: EmailStrategy
) {
    RECEIVED(
        defaultMessage = "Service order received and initiated",
        strategy = ReceivedEmailStrategy()
    ),
    IN_DIAGNOSIS(
        defaultMessage = "Diagnosis in progress",
        strategy = InDiagnosisEmailStrategy()
    ),
    WAITING_APPROVAL(
        defaultMessage = "Waiting for customer approval",
        strategy = WaitingApprovalEmailStrategy()
    ),
    APPROVED(
        defaultMessage = "Order approved by customer",
        strategy = ApprovedEmailStrategy()
    ),
    REFUSED(
        defaultMessage = "Order refused by customer",
        strategy = RefusedEmailStrategy()
    ),
    IN_EXECUTION(
        defaultMessage = "Service execution in progress",
        strategy = InExecutionEmailStrategy()
    ),
    FINALIZED(
        defaultMessage = "All services completed and finalized",
        strategy = FinalizedEmailStrategy()
    ),
    PAID(
        defaultMessage = "Payment confirmed and invoice issued",
        strategy = PaidEmailStrategy()
    ),
    CANCELED(
        defaultMessage = "Service order canceled",
        strategy = CanceledEmailStrategy()
    );

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
