package com.cao.repairshop.serviceorder.application.usecases.impl

import com.cao.repairshop.core.exception.EntityNotFoundException
import com.cao.repairshop.core.exception.ErrorMessages
import com.cao.repairshop.core.notification.EmailService
import com.cao.repairshop.core.notification.dto.EmailRequest
import com.cao.repairshop.register.application.gateways.CustomerGateway
import com.cao.repairshop.register.application.gateways.VehicleGateway
import com.cao.repairshop.serviceorder.application.gateways.ServiceOrderGateway
import com.cao.repairshop.serviceorder.application.usecases.AdvanceServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import com.cao.repairshop.serviceorder.domain.entities.mapper.toResponse
import com.cao.repairshop.serviceorder.infra.controller.dtos.ServiceOrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvanceServiceOrderStatusImpl(
    private val serviceOrderGateway: ServiceOrderGateway,
    private val customerGateway: CustomerGateway,
    private val vehicleGateway: VehicleGateway,
    private val emailService: EmailService
) : AdvanceServiceOrderStatus {

    @Transactional
    override fun execute(id: UUID, newStatus: ServiceOrderStatus): ServiceOrderResponse {
        val order = serviceOrderGateway.findDetailedById(id)
            ?: throw EntityNotFoundException(ErrorMessages.ServiceOrder.NOT_FOUND)

        order.advanceStatus(newStatus)
        val saved = serviceOrderGateway.save(order)

        if (newStatus == ServiceOrderStatus.WAITING_APPROVAL) {
            notifyCustomerForApproval(saved)
        }

        return saved.toResponse()
    }

    private fun notifyCustomerForApproval(order: ServiceOrder) {
        val customer = customerGateway.findById(order.customerId)
            ?: throw EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND)
        
        customer.email?.let { customerEmail ->
            val vehicle = vehicleGateway.findById(order.vehicleId)
                ?: throw EntityNotFoundException(ErrorMessages.Vehicle.NOT_FOUND)
                
            val request = EmailRequest(
                to = customerEmail.value,
                subject = "Ordem de Serviço #${order.id} - Aguardando Aprovação",
                body = """
                    Olá ${customer.name},
                    
                    Sua ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} está aguardando sua aprovação.
                    
                    Valor total: R$ ${order.totalPrice}
                    
                    Por favor, entre em contato ou acesse o sistema para aprovar/recusar o orçamento.
                    
                    Atenciosamente,
                    Equipe RepairShop
                """.trimIndent()
            )
            emailService.sendEmail(request)
        }
    }
}
