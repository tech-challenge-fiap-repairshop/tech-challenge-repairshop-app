package com.cao.repairshop.execution.entity

import com.cao.repairshop.execution.domain.ExecutionStatus

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tb_execution_history")
class ExecutionHistory(
    @Id
    @Column(name = "id_tb_execution_history")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id", nullable = false)
    var execution: Execution,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ExecutionStatus,

    @Column(length = 255)
    var description: String? = null,

    @Column(name = "register_time", nullable = false)
    var registerTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "interval_time")
    var intervalTime: Long? = null
) {
    override fun equals(other: Any?): Boolean = other is ExecutionHistory && id == other.id
    override fun hashCode(): Int = id.hashCode()
}
