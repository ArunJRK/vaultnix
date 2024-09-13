package com.innowate.services.order.payloads

data class PaymentDTO(
    var paymentId: Long? = null,
    var paymentMethod: String? = null
)
