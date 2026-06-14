package com.cao.repairshop.inventory.application.usecases

import java.util.UUID

interface DeleteInsume {
    fun execute(id: UUID)
}
