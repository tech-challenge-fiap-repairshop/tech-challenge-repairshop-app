package com.cao.repairshop.serviceorder.application.usecases.impl.strategies

import com.cao.repairshop.register.domain.entities.Customer
import com.cao.repairshop.register.domain.entities.Vehicle
import com.cao.repairshop.serviceorder.domain.ServiceOrderStatus
import com.cao.repairshop.serviceorder.domain.entities.ServiceOrder
import org.springframework.stereotype.Component

@Component
class ApprovedEmailStrategy : EmailStrategy {
    override fun formatSubject(order: ServiceOrder): String {
        return "Ordem de Serviço #${order.id} - Aprovada"
    }

    override fun formatBody(customer: Customer, order: ServiceOrder, vehicle: Vehicle): String {
        return """
            Olá ${customer.name},
            
            Sua ordem de serviço #${order.id} para o veículo ${vehicle.plate.value} foi aprovada.
            
            Valor total: R$ ${order.totalPrice}
            
            Os serviços serão iniciados em breve. Você será notificado quando concluído.
            
            Atenciosamente,
            Equipe RepairShop
        """.trimIndent()
    }
}
