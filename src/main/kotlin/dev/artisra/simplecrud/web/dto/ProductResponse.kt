package dev.artisra.simplecrud.web.dto

import dev.artisra.simplecrud.domain.Product
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Schema(description = "Product response payload")
data class ProductResponse(
    @Schema(description = "Unique identifier of the product")
    val id: UUID,
    @Schema(description = "Name of the product", example = "Laptop")
    val name: String,
    @Schema(description = "Current stock quantity", example = "50")
    val stock: Int,
    @Schema(description = "Optimistic locking version")
    val version: Long,
    @Schema(description = "Timestamp when the product was created")
    val createdAt: OffsetDateTime?,
    @Schema(description = "Timestamp when the product was last updated")
    val updatedAt: OffsetDateTime?
)

fun Product.toProductResponse(): ProductResponse {
    return ProductResponse(
        id = requireNotNull(id) { "Product ID must be present when building API response" },
        name = name,
        stock = stock,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

