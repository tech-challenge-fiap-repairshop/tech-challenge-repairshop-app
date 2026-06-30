package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class ReceivedEmailStrategy : EmailStrategy {
    override val status = ServiceOrderStatus.RECEIVED

    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Recebida"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Sua ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} foi recebida e iniciada com sucesso.
            
            Em breve iniciaremos a fase de diagnóstico e você receberá novas atualizações.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
