package com.innowate.services.order.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.innowate.services.order.entities.OrderItem

@Repository
interface OrderItemRepo : JpaRepository<OrderItem, Long>
