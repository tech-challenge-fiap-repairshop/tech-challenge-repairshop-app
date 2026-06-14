package com.cao.repairshop.execution.application.usecases

import java.util.UUID

interface RemoveExecution {
    fun execute(serviceOrderId: UUID, executionId: UUID)
}
