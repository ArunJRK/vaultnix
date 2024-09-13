package com.innowate.services.order.entities

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val paymentId: Long? = null,

    @OneToOne(mappedBy = "payment", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var order: Order? = null,

    @field:NotBlank
    @field:Size(min = 4, message = "Payment method must contain at least 4 characters")
    var paymentMethod: String
)
