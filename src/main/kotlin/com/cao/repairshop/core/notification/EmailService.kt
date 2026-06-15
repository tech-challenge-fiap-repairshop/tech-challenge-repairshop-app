package com.cao.repairshop.core.notification

import com.cao.repairshop.core.notification.dto.EmailRequest

fun interface EmailService {
    fun sendEmail(request: EmailRequest)
}
