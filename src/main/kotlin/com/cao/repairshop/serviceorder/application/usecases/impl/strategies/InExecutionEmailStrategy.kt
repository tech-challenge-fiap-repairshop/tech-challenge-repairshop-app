package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class InExecutionEmailStrategy : EmailStrategy {
    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Em Execução"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Informamos que a execução dos serviços para o veículo ${vehicle.plate.value} referentes à ordem de serviço #${order.id} foi iniciada.
            
            Nossa equipe técnica já está trabalhando no seu veículo. Enviaremos uma nova notificação assim que tudo estiver pronto.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
