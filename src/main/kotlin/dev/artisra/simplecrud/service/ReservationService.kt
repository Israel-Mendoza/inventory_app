package dev.artisra.simplecrud.service

import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.domain.ReservationStatus
import dev.artisra.simplecrud.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class ReservationService(
    private val productService: ProductService,
    private val reservationRepository: ReservationRepository,
    private val transactionTemplate: TransactionTemplate
) {
    // Striped locking: one mutex per product ID
    private val locks = ConcurrentHashMap<UUID, Mutex>()

    suspend fun reserveProduct(productId: UUID, userId: UUID, quantity: Int): Reservation {
        // Get or create a mutex for this specific product
        val lock = locks.computeIfAbsent(productId) { Mutex() }

        // Jumping to IO context before blocking operation
        return withContext(Dispatchers.IO) {
            lock.withLock {
                // Blocking operation but in IO context
                transactionTemplate.execute {
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
    }

    suspend fun confirmReservation(reservationId: UUID): Reservation {
        return withContext(Dispatchers.IO) {
            // Blocking operation but in IO context
            val reservation = getReservation(reservationId)
            val lock = locks.computeIfAbsent(reservation.product.id!!) { Mutex() }

            lock.withLock {
                transactionTemplate.execute {
                    val currentReservation = getReservationSync(reservationId)

                    if (currentReservation.status != ReservationStatus.PENDING) {
                        logger.error("Reservation is not pending: $reservationId")
                        throw IllegalStateException("Reservation is not pending: $reservationId")
                    }

                    currentReservation.status = ReservationStatus.CONFIRMED
                    reservationRepository.save(currentReservation)
                }
            }
        }
    }

    suspend fun cancelReservation(reservationId: UUID): Reservation {
        return withContext(Dispatchers.IO) {
            // Blocking operation but in IO context
            val reservation = getReservation(reservationId)
            val lock = locks.computeIfAbsent(reservation.product.id!!) { Mutex() }

            lock.withLock {
                transactionTemplate.execute {
                    val currentReservation = getReservationSync(reservationId)

                    if (currentReservation.status != ReservationStatus.PENDING) {
                        logger.error("Reservation is not pending: $reservationId")
                        throw IllegalStateException("Reservation is not pending: $reservationId")
                    }

                    if (currentReservation.quantity <= 0) {
                        logger.error("Reservation quantity must be positive: $reservationId")
                        throw IllegalStateException("Reservation quantity must be positive: $reservationId")
                    }

                    productService.increaseStock(currentReservation.product.id!!, currentReservation.quantity)
                    currentReservation.status = ReservationStatus.CANCELLED
                    reservationRepository.save(currentReservation)
                }
            }
        }
    }

    // Synchronous version of getReservation for non-coroutine context like TransactionTemplate
    private fun getReservationSync(id: UUID): Reservation {
        return reservationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Reservation not found: $id")
        }
    }

    suspend fun getReservation(id: UUID): Reservation {
        return withContext(Dispatchers.IO) {
            getReservationSync(id)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReservationService::class.java)
    }
}
