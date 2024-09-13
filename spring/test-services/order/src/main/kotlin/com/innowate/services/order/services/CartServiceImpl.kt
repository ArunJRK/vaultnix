package com.innowate.services.order.services


import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.innowate.services.order.entities.Cart
import com.innowate.services.order.entities.CartItem
import com.innowate.services.order.entities.Product
import com.innowate.services.order.exceptions.APIException
import com.innowate.services.order.exceptions.ResourceNotFoundException
import com.innowate.services.order.payloads.CartDTO
import com.innowate.services.order.payloads.ProductDTO
import com.innowate.services.order.repositories.CartItemRepo
import com.innowate.services.order.repositories.CartRepo
import com.innowate.services.order.repositories.ProductRepo
import jakarta.transaction.Transactional

@Transactional
@Service
class CartServiceImpl @Autowired constructor(
    private val cartRepo: CartRepo,
    private val productRepo: ProductRepo,
    private val cartItemRepo: CartItemRepo,
    private val modelMapper: ModelMapper
) : CartService {

    override fun addProductToCart(cartId: Long, productId: Long, quantity: Int): CartDTO {
        val cart = cartRepo.findById(cartId).orElseThrow {
            ResourceNotFoundException("Cart", "cartId", cartId)
        }

        val product = productRepo.findById(productId).orElseThrow {
            ResourceNotFoundException("Product", "productId", productId)
        }

        val cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId)

        if (cartItem != null) {
            throw APIException("Product ${product.productName} already exists in the cart")
        }

        if (product.quantity == 0) {
            throw APIException("${product.productName} is not available")
        }

        if (product.quantity!! < quantity) {
            throw APIException("Please, make an order of the ${product.productName} less than or equal to the quantity ${product.quantity}.")
        }

        val newCartItem = CartItem(
            product = product,
            cart = cart,
            quantity = quantity,
            discount = product.discount,
            productPrice = product.specialPrice
        )

        cartItemRepo.save(newCartItem)

        product.quantity = product.quantity!! - quantity

        cart.totalPrice += (product.specialPrice * quantity)

        val cartDTO = modelMapper.map(cart, CartDTO::class.java)

        val productDTOs = cart.cartItems.map { item ->
            modelMapper.map(item.product, ProductDTO::class.java)
        }

        cartDTO.products = productDTOs

        return cartDTO
    }

    override fun getAllCarts(): List<CartDTO> {
        val carts = cartRepo.findAll()

        if (carts.isEmpty()) {
            throw APIException("No cart exists")
        }

        return carts.map { cart ->
            val cartDTO = modelMapper.map(cart, CartDTO::class.java)

            val products = cart.cartItems.map { cartItem ->
                modelMapper.map(cartItem.product, ProductDTO::class.java)
            }

            cartDTO.products = products

            cartDTO
        }
    }

    override fun getCart(emailId: String, cartId: Long): CartDTO {
        val cart = cartRepo.findCartByEmailAndCartId(emailId, cartId)
            ?: throw ResourceNotFoundException("Cart", "cartId", cartId)

        val cartDTO = modelMapper.map(cart, CartDTO::class.java)

        val products = cart.cartItems.map { cartItem ->
            modelMapper.map(cartItem.product, ProductDTO::class.java)
        }

        cartDTO.products = products

        return cartDTO
    }

    override fun updateProductInCarts(cartId: Long, productId: Long) {
        val cart = cartRepo.findById(cartId).orElseThrow {
            ResourceNotFoundException("Cart", "cartId", cartId)
        }

        val product = productRepo.findById(productId).orElseThrow {
            ResourceNotFoundException("Product", "productId", productId)
        }

        var cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId)
            ?: throw APIException("Product ${product.productName} not available in the cart!!!")

        val cartPrice = cart.totalPrice - (cartItem.productPrice * cartItem.quantity)

        cartItem.productPrice = product.specialPrice

        cart.totalPrice = cartPrice + (cartItem.productPrice * cartItem.quantity)

        cartItemRepo.save(cartItem)
    }

    override fun updateProductQuantityInCart(cartId: Long, productId: Long, quantity: Int): CartDTO {
        val cart = cartRepo.findById(cartId).orElseThrow {
            ResourceNotFoundException("Cart", "cartId", cartId)
        }

        val product = productRepo.findById(productId).orElseThrow {
            ResourceNotFoundException("Product", "productId", productId)
        }

        if (product.quantity == 0) {
            throw APIException("${product.productName} is not available")
        }

        if (product.quantity!! < quantity) {
            throw APIException("Please, make an order of the ${product.productName} less than or equal to the quantity ${product.quantity}.")
        }

        var cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId)
            ?: throw APIException("Product ${product.productName} not available in the cart!!!")

        val cartPrice = cart.totalPrice - (cartItem.productPrice * cartItem.quantity)

        product.quantity = product.quantity!! + (cartItem.quantity - quantity)

        cartItem.productPrice = product.specialPrice
        cartItem.quantity = quantity
        cartItem.discount = product.discount

        cart.totalPrice = cartPrice + (cartItem.productPrice * quantity)

        cartItemRepo.save(cartItem)

        val cartDTO = modelMapper.map(cart, CartDTO::class.java)

        val productDTOs = cart.cartItems.map { item ->
            modelMapper.map(item.product, ProductDTO::class.java)
        }

        cartDTO.products = productDTOs

        return cartDTO
    }

    override fun deleteProductFromCart(cartId: Long, productId: Long): String {
        val cart = cartRepo.findById(cartId).orElseThrow {
            ResourceNotFoundException("Cart", "cartId", cartId)
        }

        val cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId)
            ?: throw ResourceNotFoundException("Product", "productId", productId)

        cart.totalPrice -= (cartItem.productPrice * cartItem.quantity)

        val product = cartItem.product
        if (product != null) {
            product.quantity = product.quantity!! + cartItem.quantity
        }

        cartItemRepo.deleteCartItemByProductIdAndCartId(cartId, productId)

        return "Product ${cartItem.product?.productName} removed from the cart !!!"
    }
}
