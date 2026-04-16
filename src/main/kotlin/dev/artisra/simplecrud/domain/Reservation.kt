package dev.artisra.simplecrud.domain

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "reservations")
@Schema(description = "Product reservation entity")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the reservation")
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @Schema(description = "Product that is reserved")
    val product: Product,

    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID of the user who made the reservation")
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Current status of the reservation")
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(nullable = false)
    @Schema(description = "Quantity of items reserved")
    var quantity: Int = 1,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Timestamp when the reservation was created")
    val createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(description = "Timestamp when the reservation was last updated")
    val updatedAt: OffsetDateTime? = null,

    @Column(name = "expires_at")
    @Schema(description = "Timestamp when the reservation will expire")
    var expiresAt: OffsetDateTime? = null
)
