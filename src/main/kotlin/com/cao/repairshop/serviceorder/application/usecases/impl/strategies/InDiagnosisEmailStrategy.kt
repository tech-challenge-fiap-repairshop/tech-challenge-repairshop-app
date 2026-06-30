package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class InDiagnosisEmailStrategy : EmailStrategy {
    override val status = ServiceOrderStatus.IN_DIAGNOSIS

    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Em Diagnóstico"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            O diagnóstico para o veículo ${vehicle.plate.value} referente à ordem de serviço #${order.id} está em andamento.
            
            Nossos técnicos estão avaliando as necessidades de manutenção. Assim que concluído, enviaremos o orçamento para sua aprovação.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
