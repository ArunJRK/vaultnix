package com.innowate.services.order.repositories


import com.innowate.services.order.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepo : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.addresses a WHERE a.addressId = ?1")
    fun findByAddress(addressId: Long): List<User>

    fun findByEmail(email: String): Optional<User>
}
