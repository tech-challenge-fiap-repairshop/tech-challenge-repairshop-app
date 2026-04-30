package com.cao.repairshop.core.notification.dto

data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String,
    val isHtml: Boolean = false
)
