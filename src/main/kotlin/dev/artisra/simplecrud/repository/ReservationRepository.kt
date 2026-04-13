package dev.artisra.simplecrud.repository

import dev.artisra.simplecrud.domain.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReservationRepository : JpaRepository<Reservation, UUID>
