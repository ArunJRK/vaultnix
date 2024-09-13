package com.innowate.services.order.payloads

data class CartDTO(
    val cartId: Long? = null,
    val totalPrice: Double = 0.0,
    var products: List<ProductDTO> = emptyList()
)
