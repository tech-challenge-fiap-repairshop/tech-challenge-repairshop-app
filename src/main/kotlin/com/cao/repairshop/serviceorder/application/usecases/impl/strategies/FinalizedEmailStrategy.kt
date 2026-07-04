package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class FinalizedEmailStrategy : EmailStrategy {
    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Finalizada"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Boas notícias! Todos os serviços referentes à ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} foram concluídos e finalizados.
            
            Sua fatura estará disponível para pagamento em instantes.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
