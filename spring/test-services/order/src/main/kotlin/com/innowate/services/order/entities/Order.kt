package com.innowate.services.order.entities
import java.time.LocalDate
import jakarta.persistence.*
import jakarta.validation.constraints.Email

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val orderId: Long? = null,

    @field:Email
    @Column(nullable = false)
    var email: String,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val orderItems: MutableList<OrderItem> = mutableListOf(),

    var orderDate: LocalDate? = null,

    @OneToOne
    @JoinColumn(name = "payment_id")
    var payment: Payment? = null,

    var totalAmount: Double? = null,
    var orderStatus: String? = null
)
