package com.cao.repairshop.inventory.entity

import com.cao.repairshop.core.exception.InsufficientStockException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.text.Normalizer
import java.util.*

@Entity
@Table(name = "tb_insume")
class Insume(
    @Id
    @Column(name = "id_tb_insume")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(length = 100)
    var brand: String? = null,

    @Column(name = "sku_id", length = 50)
    var skuId: String = "",

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(name = "unity_price", nullable = false, precision = 10, scale = 2)
    var unityPrice: BigDecimal,

) {

    init {
        if (skuId.isBlank()) generateSku()
    }

    fun generateSku() {
        val brandPart = brand?.let { normalizeSku(it) } ?: "SEM-MARCA"
        val namePart = normalizeSku(name)
        skuId = "SKU-$namePart-$brandPart"
    }

    private fun normalizeSku(text: String): String {
        val normalized = Normalizer.normalize(text.uppercase(), Normalizer.Form.NFD)
        return normalized
            .replace(Regex("[^\\p{ASCII}]"), "")
            .replace(" ", "-")
    }

    fun deductStock(amount: Int) {
        require(amount > 0) { "Deduction amount must be positive, got: $amount" }
        if (quantity < amount)
            throw InsufficientStockException("Insufficient stock for insume $name. Available: $quantity, Required: $amount.")
        quantity -= amount
    }

    fun restoreStock(amount: Int) {
        require(amount > 0) { "Restore amount must be positive, got: $amount" }
        quantity += amount
    }

    override fun equals(other: Any?): Boolean = other is Insume && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "Insume(id=$id, name=$name)"
}
