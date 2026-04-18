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

@Service
class ReservationService(
    private val productService: ProductService,
    private val reservationRepository: ReservationRepository,
    private val transactionTemplate: TransactionTemplate,
    private val productLockService: ProductLockService
) {

    suspend fun reserveProduct(productId: UUID, userId: UUID, quantity: Int): Reservation {
        return productLockService.withLock(productId) {
            withContext(Dispatchers.IO) {
                transactionTemplate.execute {
                    // Call non-locking internal method within the transaction
                    val product = productService.deductStockInternal(productId, quantity)

                    // Create reservation
                    val reservation = Reservation(
                        product = product,
                        userId = userId,
                        status = ReservationStatus.PENDING,
                        quantity = quantity
                    )
                    reservationRepository.save(reservation)
                }!!
            }
        }
    }

    suspend fun confirmReservation(reservationId: UUID): Reservation {
        val reservation = getReservation(reservationId)
        val productId = reservation.product.id!!
        
        return productLockService.withLock(productId) {
            withContext(Dispatchers.IO) {
                transactionTemplate.execute {
                    val currentReservation = getReservationSync(reservationId)

                    if (currentReservation.status != ReservationStatus.PENDING) {
                        logger.error("Reservation is not pending: $reservationId")
                        throw IllegalStateException("Reservation is not pending: $reservationId")
                    }

                    currentReservation.status = ReservationStatus.CONFIRMED
                    reservationRepository.save(currentReservation)
                }!!
            }
        }
    }

    suspend fun cancelReservation(reservationId: UUID): Reservation {
        val reservation = getReservation(reservationId)
        val productId = reservation.product.id!!

        return productLockService.withLock(productId) {
            withContext(Dispatchers.IO) {
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

                    // Call non-locking internal method within the transaction
                    productService.increaseStockInternal(productId, currentReservation.quantity)
                    
                    currentReservation.status = ReservationStatus.CANCELLED
                    reservationRepository.save(currentReservation)
                }!!
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
