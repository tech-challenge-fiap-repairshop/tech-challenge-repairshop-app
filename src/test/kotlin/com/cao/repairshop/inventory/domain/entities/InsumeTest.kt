package com.cao.repairshop.inventory.domain.entities

import com.cao.repairshop.core.exception.InsufficientStockException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class InsumeTest {

    @Test
    fun `should create Insume and generate SKU when empty`() {
        val insume = Insume(
            name = "Óleo de Motor",
            brand = "Castrol",
            price = BigDecimal("30.00"),
            unityPrice = BigDecimal("30.00")
        )

        assertEquals("SKU-OLEO-DE-MOTOR-CASTROL", insume.skuId)
    }

    @Test
    fun `should create Insume and use SEM-MARCA when brand is null`() {
        val insume = Insume(
            name = "Filtro",
            price = BigDecimal("15.00"),
            unityPrice = BigDecimal("15.00")
        )

        assertEquals("SKU-FILTRO-SEM-MARCA", insume.skuId)
    }

    @Test
    fun `should deduct stock when enough quantity`() {
        val insume = Insume(
            name = "Pneu",
            quantity = 10,
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        insume.deductStock(4)
        assertEquals(6, insume.quantity)
    }

    @Test
    fun `should throw error when deducting stock with negative or zero amount`() {
        val insume = Insume(
            name = "Pneu",
            quantity = 10,
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            insume.deductStock(0)
        }
        assertEquals("Deduction amount must be positive, got: 0", exception.message)
    }

    @Test
    fun `should throw error when deducting more than available`() {
        val insume = Insume(
            name = "Pneu",
            quantity = 2,
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        assertThrows(InsufficientStockException::class.java) {
            insume.deductStock(5)
        }
    }

    @Test
    fun `should restore stock`() {
        val insume = Insume(
            name = "Pneu",
            quantity = 2,
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        insume.restoreStock(3)
        assertEquals(5, insume.quantity)
    }

    @Test
    fun `should throw error when restoring stock with negative or zero amount`() {
        val insume = Insume(
            name = "Pneu",
            quantity = 10,
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            insume.restoreStock(-1)
        }
        assertEquals("Restore amount must be positive, got: -1", exception.message)
    }

    @Test
    fun `should verify equality and hashcode`() {
        val insume1 = Insume(
            name = "Pneu",
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        val insume2 = Insume(
            id = insume1.id,
            name = "Pneu 2",
            price = BigDecimal("250.00"),
            unityPrice = BigDecimal("250.00")
        )
        
        val insume3 = Insume(
            name = "Pneu",
            price = BigDecimal("200.00"),
            unityPrice = BigDecimal("200.00")
        )

        assertEquals(insume1, insume2)
        assertNotEquals(insume1, insume3)
        assertNotEquals(insume1, null)
        assertNotEquals(insume1, Any())
        assertEquals(insume1.hashCode(), insume2.hashCode())
        assertTrue(insume1.toString().contains(insume1.name))
        assertTrue(insume1.toString().contains(insume1.id.toString()))
    }
}
