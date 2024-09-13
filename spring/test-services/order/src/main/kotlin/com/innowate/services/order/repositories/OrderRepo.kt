package com.innowate.services.order.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import com.innowate.services.order.entities.Order

@Repository
interface OrderRepo : JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.email = ?1 AND o.id = ?2")
    fun findOrderByEmailAndOrderId(email: String, orderId: Long): Order?

    fun findAllByEmail(emailId: String): List<Order>
}
