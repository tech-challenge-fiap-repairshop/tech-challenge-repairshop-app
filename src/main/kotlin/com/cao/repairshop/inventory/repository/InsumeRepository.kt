package com.cao.repairshop.inventory.repository

import com.cao.repairshop.inventory.entity.Insume
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface InsumeRepository : JpaRepository<Insume, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Insume i WHERE i.id = :id")
    fun findByIdForUpdate(id: UUID): Insume?
}
