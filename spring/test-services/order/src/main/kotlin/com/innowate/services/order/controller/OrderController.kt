package com.innowate.services.order

import org.springframework.web.bind.annotation.RestController
import com.innowate.services.order.payloads.OrderDTO
import com.innowate.services.order.services.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class OrderController @Autowired constructor(
    private val orderService: OrderService
) {

    @PostMapping("/public/users/{emailId}/carts/{cartId}/payments/{paymentMethod}/order")
    fun orderProducts(
        @PathVariable emailId: String,
        @PathVariable cartId: Long,
        @PathVariable paymentMethod: String
    ): ResponseEntity<OrderDTO> {
        val order = orderService.placeOrder(emailId, cartId, paymentMethod)
        return ResponseEntity(order, HttpStatus.CREATED)
    }

    @GetMapping("public/users/{emailId}/orders")
    fun getOrdersByUser(@PathVariable emailId: String): ResponseEntity<List<OrderDTO>> {
        val orders = orderService.getOrdersByUser(emailId)
        return ResponseEntity(orders, HttpStatus.FOUND)
    }

    @GetMapping("public/users/{emailId}/orders/{orderId}")
    fun getOrderByUser(@PathVariable emailId: String, @PathVariable orderId: Long): ResponseEntity<OrderDTO> {
        val order = orderService.getOrder(emailId, orderId)
        return ResponseEntity(order, HttpStatus.FOUND)
    }

    @PutMapping("admin/users/{emailId}/orders/{orderId}/orderStatus/{orderStatus}")
    fun updateOrderByUser(
        @PathVariable emailId: String,
        @PathVariable orderId: Long,
        @PathVariable orderStatus: String
    ): ResponseEntity<OrderDTO> {
        val order = orderService.updateOrder(emailId, orderId, orderStatus)
        return ResponseEntity(order, HttpStatus.OK)
    }
}
