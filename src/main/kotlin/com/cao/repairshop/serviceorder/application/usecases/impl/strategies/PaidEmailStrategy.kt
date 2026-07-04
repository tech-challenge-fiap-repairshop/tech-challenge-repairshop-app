package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class PaidEmailStrategy : EmailStrategy {
    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Paga"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Confirmamos o pagamento referente à ordem de serviço #${order.id} para o veículo ${vehicle.plate.value}.
            
            Sua nota fiscal correspondente já foi emitida. Agradecemos a preferência!
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
