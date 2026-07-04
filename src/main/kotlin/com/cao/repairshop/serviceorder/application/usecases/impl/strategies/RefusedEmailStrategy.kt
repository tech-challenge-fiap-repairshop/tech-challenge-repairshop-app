package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class RefusedEmailStrategy : EmailStrategy {
    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Recusada"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            O orçamento referente à ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} foi recusado.
            
            A ordem de serviço prosseguirá para o cancelamento automático. Caso deseje rever o orçamento ou tenha dúvidas, por favor entre em contato com nossa equipe.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
