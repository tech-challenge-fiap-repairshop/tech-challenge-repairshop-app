package com.cao.repairshop.core.notification.infrastructure

import com.cao.repairshop.core.notification.EmailService
import com.cao.repairshop.core.notification.dto.EmailRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class JavaMailEmailService(
    private val mailSender: JavaMailSender
) : EmailService {

    @Async
    override fun sendEmail(request: EmailRequest) {
        logger.info { "Sending email to ${request.to} with subject: ${request.subject}" }
        
        runCatching {
            val message = mailSender.createMimeMessage()
            MimeMessageHelper(message, true, "UTF-8").apply {
                setFrom("atendimento@repairshop.com.br", "Atendimento Repair Shop")
                setTo(request.to)
                setSubject(request.subject)
                setText(request.body, request.isHtml)
            }
            mailSender.send(message)
        }.onSuccess {
            logger.info { "Email sent successfully to ${request.to}" }
        }.onFailure { e ->
            logger.error(e) { "Failed to send email to ${request.to}" }
        }
    }
}
