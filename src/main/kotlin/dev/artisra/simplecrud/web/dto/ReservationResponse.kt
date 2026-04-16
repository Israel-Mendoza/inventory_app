package dev.artisra.simplecrud.web.dto

import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.domain.ReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Schema(description = "Reservation response payload")
data class ReservationResponse(
    @Schema(description = "Unique identifier of the reservation")
    val id: UUID,
    @Schema(description = "ID of the reserved product")
    val productId: UUID,
    @Schema(description = "ID of the user who made the reservation")
    val userId: UUID,
    @Schema(description = "Current status of the reservation")
    val status: ReservationStatus,
    @Schema(description = "Quantity of items reserved")
    val quantity: Int,
    @Schema(description = "Timestamp when the reservation was created")
    val createdAt: OffsetDateTime?,
    @Schema(description = "Timestamp when the reservation was last updated")
    val updatedAt: OffsetDateTime?,
    @Schema(description = "Timestamp when the reservation will expire")
    val expiresAt: OffsetDateTime?
)

fun Reservation.toReservationResponse(): ReservationResponse {
    return ReservationResponse(
        id = requireNotNull(id) { "Reservation ID must be present when building API response" },
        productId = requireNotNull(product.id) { "Product ID must be present when building API response" },
        userId = userId,
        status = status,
        quantity = quantity,
        createdAt = createdAt,
        updatedAt = updatedAt,
        expiresAt = expiresAt
    )
}

