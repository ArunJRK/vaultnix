package com.innowate.services.order.entities
import jakarta.persistence.*


@Entity
@Table(name = "cart_items")
data class CartItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val cartItemId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "cart_id")
    val cart: Cart? = null,

    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product? = null,

    var quantity: Int = 0,
    var discount: Double = 0.0,
    var productPrice: Double = 0.0
)

