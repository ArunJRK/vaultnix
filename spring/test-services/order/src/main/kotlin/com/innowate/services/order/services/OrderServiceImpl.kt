package com.innowate.services.order.services

import com.innowate.services.order.entities.*
import com.innowate.services.order.exceptions.APIException
import com.innowate.services.order.exceptions.ResourceNotFoundException
import com.innowate.services.order.payloads.OrderDTO
import com.innowate.services.order.payloads.OrderItemDTO
import com.innowate.services.order.payloads.OrderResponse
import com.innowate.services.order.repositories.*
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate

@Transactional
@Service
class OrderServiceImpl @Autowired constructor(
    private val cartRepo: CartRepo,
    private val orderRepo: OrderRepo,
    private val paymentRepo: PaymentRepo,
    private val orderItemRepo: OrderItemRepo,
    private val cartService: CartService,
    private val modelMapper: ModelMapper
) : OrderService {

    override fun placeOrder(emailId: String, cartId: Long, paymentMethod: String): OrderDTO {
        val cart = cartRepo.findCartByEmailAndCartId(emailId, cartId)
            ?: throw ResourceNotFoundException("Cart", "cartId", cartId)

        if (cart.cartItems.isEmpty()) {
            throw APIException("Cart is empty")
        }

        val order = Order(
            email = emailId,
            orderDate = LocalDate.now(),
            totalAmount = cart.totalPrice,
            orderStatus = "Order Accepted!"
        )

        val payment = Payment(
            order = order,
            paymentMethod = paymentMethod
        )

        val savedPayment = paymentRepo.save(payment)
        order.payment = savedPayment

        val savedOrder = orderRepo.save(order)

        val orderItems = cart.cartItems.map { cartItem ->
            OrderItem(
                product = cartItem.product!!,
                quantity = cartItem.quantity,
                discount = cartItem.discount,
                orderedProductPrice = cartItem.productPrice,
                order = savedOrder
            )
        }

        orderItemRepo.saveAll(orderItems)

        // Update product quantities and clear cart
        cart.cartItems.forEach { item ->
            item.product?.let { product ->
                product.quantity = product.quantity?.minus(item.quantity)
                product.productId?.let { cartService.deleteProductFromCart(cartId, it) }
            }
        }

        val orderDTO = modelMapper.map(savedOrder, OrderDTO::class.java)
//        orderDTO.orderItems.addAll(orderItems.map { modelMapper.map(it, OrderItemDTO::class.java) })
        orderDTO.orderItems = orderItems.map { modelMapper.map(it, OrderItemDTO::class.java) }


        return orderDTO
    }

    override fun getOrdersByUser(emailId: String): List<OrderDTO> {
        val orders = orderRepo.findAllByEmail(emailId)
        if (orders.isEmpty()) {
            throw APIException("No orders placed yet by the user with email: $emailId")
        }
        return orders.map { modelMapper.map(it, OrderDTO::class.java) }
    }

    override fun getOrder(emailId: String, orderId: Long): OrderDTO {
        val order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId)
            ?: throw ResourceNotFoundException("Order", "orderId", orderId)
        return modelMapper.map(order, OrderDTO::class.java)
    }

    override fun getAllOrders(pageNumber: Int, pageSize: Int, sortBy: String, sortOrder: String): OrderResponse {
        val sort = if (sortOrder.equals("asc", ignoreCase = true)) {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }

        val pageable: Pageable = PageRequest.of(pageNumber, pageSize, sort)
        val pageOrders: Page<Order> = orderRepo.findAll(pageable)
        val orders = pageOrders.content

        if (orders.isEmpty()) {
            throw APIException("No orders placed yet by the users")
        }

        val orderDTOs = orders.map { modelMapper.map(it, OrderDTO::class.java) }

        return OrderResponse(
            content = orderDTOs,
            pageNumber = pageOrders.number,
            pageSize = pageOrders.size,
            totalElements = pageOrders.totalElements,
            totalPages = pageOrders.totalPages,
            lastPage = pageOrders.isLast
        )
    }

    override fun updateOrder(emailId: String, orderId: Long, orderStatus: String): OrderDTO {
        val order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId)
            ?: throw ResourceNotFoundException("Order", "orderId", orderId)

        order.orderStatus = orderStatus
        return modelMapper.map(order, OrderDTO::class.java)
    }
}