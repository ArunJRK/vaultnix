package com.innowate.services.order.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import com.innowate.services.order.entities.CartItem
import com.innowate.services.order.entities.Product
import jakarta.transaction.Transactional

interface CartItemRepo : JpaRepository<CartItem, Long> {

    @Query("SELECT ci.product FROM CartItem ci WHERE ci.product.id = ?1")
    fun findProductById(productId: Long): Product?

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    fun findCartItemByProductIdAndCartId(cartId: Long, productId: Long): CartItem?

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    fun deleteCartItemByProductIdAndCartId(cartId: Long, productId: Long)
}
