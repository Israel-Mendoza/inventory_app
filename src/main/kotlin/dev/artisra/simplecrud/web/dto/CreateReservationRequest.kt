package dev.artisra.simplecrud.web.dto

import java.util.UUID

data class CreateReservationRequest(
    val productId: UUID,
    val userId: UUID,
    val quantity: Int
)
