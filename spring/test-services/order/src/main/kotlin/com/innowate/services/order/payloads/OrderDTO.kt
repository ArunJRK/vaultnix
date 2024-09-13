package com.innowate.services.order.payloads

import java.time.LocalDate

data class OrderDTO(
    var orderId: Long? = null,
    var email: String? = null,
    var orderItems: List<OrderItemDTO> = mutableListOf(),
    var orderDate: LocalDate? = null,
    var payment: PaymentDTO? = null,
    var totalAmount: Double? = null,
    var orderStatus: String? = null
)
