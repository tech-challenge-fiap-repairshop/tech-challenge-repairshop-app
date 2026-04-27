package com.cao.repairshop.execution.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExecutionStatusTest {

    @Test
    fun `INITIATED allows only PENDING`() {
        assertEquals(setOf(ExecutionStatus.PENDING), ExecutionStatus.INITIATED.allowedTransitions())
    }

    @Test
    fun `PENDING allows only FINALIZED`() {
        assertEquals(setOf(ExecutionStatus.FINALIZED), ExecutionStatus.PENDING.allowedTransitions())
    }

    @Test
    fun `FINALIZED is terminal and allows nothing`() {
        assertEquals(emptySet<ExecutionStatus>(), ExecutionStatus.FINALIZED.allowedTransitions())
    }
}
