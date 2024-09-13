package com.innowate.services.order.entities

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val orderItemId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "product_id")
    @NotNull
    var product: Product,

    @ManyToOne
    @JoinColumn(name = "order_id")
    @NotNull
    var order: Order,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false)
    var discount: Double,

    @Column(name = "ordered_product_price", nullable = false)
    var orderedProductPrice: Double
)
