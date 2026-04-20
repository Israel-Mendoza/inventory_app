package dev.artisra.simplecrud.repository

import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.domain.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface ReservationRepository : JpaRepository<Reservation, UUID> {
    fun findAllByStatusAndExpiresAtBefore(status: ReservationStatus, now: OffsetDateTime): List<Reservation>
}
