package com.innowate.services.order.repositories


import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.innowate.services.order.entities.Product

@Repository
interface ProductRepo : JpaRepository<Product, Long> {

    fun findByProductNameLike(keyword: String, pageable: Pageable): Page<Product>
}
