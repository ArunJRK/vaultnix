package com.innowate.services.order.entities

import jakarta.persistence.*

@Entity
@Table(name = "carts")
data class Cart(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val cartId: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: User? = null,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true)
    val cartItems: MutableList<CartItem> = ArrayList(),

    var totalPrice: Double = 0.0
)

