package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class WaitingApprovalEmailStrategy : EmailStrategy {
    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Aguardando Aprovação"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Sua ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} está aguardando sua aprovação.
            
            Valor total: R$ ${order.totalPrice}
            
            Por favor, entre em contato ou acesse o sistema para aprovar/recusar o orçamento.
            
            <form action="http://localhost:8080/service-orders/${order.id}/approve" method="POST" style="display: inline-block; margin: 8px; vertical-align: middle;">
                <input type="hidden" name="approved" value="true">
                <button type="submit" style="background-color: #10b981; color: #ffffff; border: none; padding: 14px 28px; font-size: 15px; font-weight: 600; border-radius: 8px; cursor: pointer; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2); outline: none;">
                    Aprovar Orçamento
                </button>
            </form>
            
            <!-- Cancel Form -->
            <form action="http://localhost:8080/service-orders/${order.id}/approve" method="POST" style="display: inline-block; margin: 8px; vertical-align: middle;">
                <input type="hidden" name="approved" value="false">
                <button type="submit" style="background-color: #ef4444; color: #ffffff; border: none; padding: 14px 28px; font-size: 15px; font-weight: 600; border-radius: 8px; cursor: pointer; box-shadow: 0 4px 6px -1px rgba(239, 68, 68, 0.2); outline: none;">
                    Cancelar Orçamento
                </button>
            </form>
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
