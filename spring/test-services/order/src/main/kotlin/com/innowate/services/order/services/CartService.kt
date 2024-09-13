package com.innowate.services.order.services

import com.innowate.services.order.payloads.CartDTO

interface CartService {

    fun addProductToCart(cartId: Long, productId: Long, quantity: Int): CartDTO

    fun getAllCarts(): List<CartDTO>

    fun getCart(emailId: String, cartId: Long): CartDTO

    fun updateProductQuantityInCart(cartId: Long, productId: Long, quantity: Int): CartDTO

    fun updateProductInCarts(cartId: Long, productId: Long)

    fun deleteProductFromCart(cartId: Long, productId: Long): String
}
