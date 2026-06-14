package com.cao.repairshop.serviceorder.application.usecases

import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderMetricsResponse

fun interface GetServiceOrderMetrics {
    fun execute(): ServiceOrderMetricsResponse
}
