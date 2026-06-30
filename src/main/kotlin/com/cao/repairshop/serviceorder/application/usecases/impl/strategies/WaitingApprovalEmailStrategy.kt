package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class WaitingApprovalEmailStrategy : EmailStrategy {
    override val status = ServiceOrderStatus.WAITING_APPROVAL

    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Aguardando Aprovação"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Sua ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} está aguardando sua aprovação.
            
            Valor total: R$ ${order.totalPrice}
            
            Por favor, entre em contato ou acesse o sistema para aprovar/recusar o orçamento.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
