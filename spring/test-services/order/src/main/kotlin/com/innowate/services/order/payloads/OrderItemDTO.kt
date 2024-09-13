package com.innowate.services.order.payloads

data class OrderItemDTO(
    var orderItemId: Long? = null,
    var product: ProductDTO? = null,
    var quantity: Int? = null,
    var discount: Double = 0.0,
    var orderedProductPrice: Double = 0.0
)
