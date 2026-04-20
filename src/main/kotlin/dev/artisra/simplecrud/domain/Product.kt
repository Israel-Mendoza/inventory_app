package dev.artisra.simplecrud.domain

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "products")
@Schema(description = "Product entity")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the product", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID? = null,

    @Column(nullable = false)
    @Schema(description = "Name of the product", example = "Laptop")
    var name: String,

    @Column(nullable = false)
    @Schema(description = "Current stock quantity", example = "50")
    var stock: Int,

    @Version
    @Column(nullable = false)
    @Schema(description = "Optimistic locking version")
    var version: Long = 0,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when the product was created")
    val createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Timestamp when the product was last updated")
    val updatedAt: OffsetDateTime? = null,

    @Column(name = "expiration_offset_in_minutes", nullable = false)
    @Schema(description = "Expiration offset in minutes for product reservations", example = "1440")
    val expirationMinutes: Int = 0
)
