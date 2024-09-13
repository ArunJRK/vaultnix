package com.innowate.services.order.repositories


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import com.innowate.services.order.entities.Cart

@Repository
interface CartRepo : JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1 AND c.id = ?2")
    fun findCartByEmailAndCartId(email: String, cartId: Long): Cart?

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.id = ?1")
    fun findCartsByProductId(productId: Long): List<Cart>
}
