package com.innowate.services.order.payloads

data class ProductDTO(
    var productId: Long? = null,
    var productName: String? = null,
    var description: String? = null,
    var quantity: Int? = null,
    var price: Double = 0.0,
)
