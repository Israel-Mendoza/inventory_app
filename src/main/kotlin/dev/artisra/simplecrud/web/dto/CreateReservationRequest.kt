package dev.artisra.simplecrud.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Request to create a product reservation")
data class CreateReservationRequest(
    @Schema(description = "UUID of the product to reserve")
    val productId: UUID,
    @Schema(description = "UUID of the user making the reservation")
    val userId: UUID,
    @Schema(description = "Quantity of items to reserve", example = "5")
    val quantity: Int
)
