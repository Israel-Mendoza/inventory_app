package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.service.ReservationCleanupTask
import dev.artisra.simplecrud.service.ReservationService
import dev.artisra.simplecrud.web.dto.CreateReservationRequest
import dev.artisra.simplecrud.web.dto.ReservationResponse
import dev.artisra.simplecrud.web.dto.toReservationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation management endpoints")
class ReservationController(
    private val reservationService: ReservationService,
    private val reservationCleanupTask: ReservationCleanupTask
) {

    @PostMapping
    @Operation(summary = "Create a reservation for a product")
    suspend fun createReservation(@RequestBody request: CreateReservationRequest): ReservationResponse {
        return reservationService.reserveProduct(
            productId = request.productId,
            userId = request.userId,
            quantity = request.quantity
        ).toReservationResponse()
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a reservation by ID")
    suspend fun confirmReservation(@PathVariable id: UUID): ReservationResponse {
        return reservationService.confirmReservation(id).toReservationResponse()
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation by ID")
    suspend fun cancelReservation(@PathVariable id: UUID): ReservationResponse {
        return reservationService.cancelReservation(id).toReservationResponse()
    }

    @PostMapping("/trigger-cleanup")
    @Operation(summary = "Trigger manual reservation cleanup task")
    fun triggerCleanup() {
        reservationCleanupTask.cleanupExpiredReservations()
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    suspend fun getReservation(@PathVariable id: UUID): ReservationResponse {
        return reservationService.getReservation(id).toReservationResponse()
    }
}
