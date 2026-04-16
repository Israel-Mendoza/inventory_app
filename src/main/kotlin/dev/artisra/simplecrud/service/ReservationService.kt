package dev.artisra.simplecrud.service

import dev.artisra.simplecrud.domain.Product
import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.domain.ReservationStatus
import dev.artisra.simplecrud.repository.ProductRepository
import dev.artisra.simplecrud.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class ReservationService(
    private val productService: ProductService,
    private val reservationRepository: ReservationRepository
) {
    // Striped locking: one mutex per product ID
    private val locks = ConcurrentHashMap<UUID, Mutex>()

    suspend fun reserveProduct(productId: UUID, userId: UUID, quantity: Int): Reservation {
        // Get or create a mutex for this specific product
        val lock = locks.computeIfAbsent(productId) { Mutex() }

        return lock.withLock {
            // Run DB operations in the IO dispatcher (ideal for blocking JPA)
            withContext(Dispatchers.IO) {
                val product = productService.deductStock(productId, quantity)
                
                // Create reservation
                val reservation = Reservation(
                    product = product,
                    userId = userId,
                    status = ReservationStatus.PENDING,
                    quantity = quantity
                )
                reservationRepository.save(reservation)
            }
        }
    }

    suspend fun confirmReservation(reservationId: UUID): Reservation {
        val reservation = getReservation(reservationId)
        if (reservation.status != ReservationStatus.PENDING) {
            logger.error("Reservation is not pending: $reservationId")
            throw IllegalStateException("Reservation is not pending: $reservationId")
        }
        reservation.status = ReservationStatus.CONFIRMED
        return withContext(Dispatchers.IO) {
            reservationRepository.save(reservation)
        }
    }

    suspend fun getReservation(id: UUID): Reservation {
        return withContext(Dispatchers.IO) {
            reservationRepository.findById(id).orElseThrow {
                IllegalArgumentException("Reservation not found: $id")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReservationService::class.java)
    }
}
