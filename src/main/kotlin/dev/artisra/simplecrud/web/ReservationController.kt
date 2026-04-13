package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.service.ReservationService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reservations")
class ReservationController(private val reservationService: ReservationService) {

    @PostMapping
    suspend fun createReservation(@RequestBody request: CreateReservationRequest): Reservation {
        return reservationService.reserveProduct(
            productId = request.productId,
            userId = request.userId,
            quantity = request.quantity
        )
    }
}

data class CreateReservationRequest(
    val productId: UUID,
    val userId: UUID,
    val quantity: Int
)
