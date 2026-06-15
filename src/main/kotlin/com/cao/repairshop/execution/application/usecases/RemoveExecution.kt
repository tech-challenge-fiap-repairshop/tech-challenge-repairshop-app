package com.cao.repairshop.execution.application.usecases

import java.util.UUID

fun interface RemoveExecution {
    fun execute(serviceOrderId: UUID, executionId: UUID)
}
