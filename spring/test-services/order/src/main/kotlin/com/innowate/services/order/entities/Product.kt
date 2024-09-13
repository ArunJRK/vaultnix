package com.innowate.services.order.entities

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "products")
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val productId: Long? = null,

    @field:NotBlank
    @field:Size(min = 3, message = "Product name must contain at least 3 characters")
    val productName: String,

    val image: String? = null,

    @field:NotBlank
    @field:Size(min = 6, message = "Product description must contain at least 6 characters")
    val description: String,

    var quantity: Int? = null,
    val price: Double = 0.0,
    val discount: Double = 0.0,
    val specialPrice: Double = 0.0,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.EAGER)
    val cartItems: List<CartItem> = ArrayList(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val orderItems: List<OrderItem> = ArrayList()
)
