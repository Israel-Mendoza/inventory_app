package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.service.ReservationService
import dev.artisra.simplecrud.web.dto.CreateReservationRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation management endpoints")
class ReservationController(private val reservationService: ReservationService) {

    @PostMapping
    @Operation(summary = "Create a reservation for a product")
    suspend fun createReservation(@RequestBody request: CreateReservationRequest): Reservation {
        return reservationService.reserveProduct(
            productId = request.productId,
            userId = request.userId,
            quantity = request.quantity
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    fun getReservation(@PathVariable id: UUID): Reservation {
        return reservationService.getReservation(id)
    }
}
