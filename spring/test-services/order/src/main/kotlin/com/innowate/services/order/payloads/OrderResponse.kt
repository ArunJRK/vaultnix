package com.innowate.services.order.payloads

data class OrderResponse(
    var content: List<OrderDTO> = mutableListOf(),
    var pageNumber: Int? = null,
    var pageSize: Int? = null,
    var totalElements: Long? = null,
    var totalPages: Int? = null,
    var lastPage: Boolean = false
)
