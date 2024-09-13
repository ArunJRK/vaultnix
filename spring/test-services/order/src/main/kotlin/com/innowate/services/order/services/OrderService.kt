package com.innowate.services.order.services

import com.innowate.services.order.payloads.OrderDTO
import com.innowate.services.order.payloads.OrderResponse

interface OrderService {

    fun placeOrder(emailId: String, cartId: Long, paymentMethod: String): OrderDTO

    fun getOrder(emailId: String, orderId: Long): OrderDTO

    fun getOrdersByUser(emailId: String): List<OrderDTO>

    fun getAllOrders(pageNumber: Int, pageSize: Int, sortBy: String, sortOrder: String): OrderResponse

    fun updateOrder(emailId: String, orderId: Long, orderStatus: String): OrderDTO
}
