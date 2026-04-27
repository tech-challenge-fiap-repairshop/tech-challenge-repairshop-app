package com.cao.repairshop.inventory.mapper

import com.cao.repairshop.inventory.dto.InsumeResponse
import com.cao.repairshop.inventory.entity.Insume

fun Insume.toResponse() = InsumeResponse(
    id = id,
    name = name,
    brand = brand,
    skuId = skuId,
    quantity = quantity,
    price = price,
    unityPrice = unityPrice
)
