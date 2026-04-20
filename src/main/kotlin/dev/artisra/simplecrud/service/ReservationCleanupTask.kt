package dev.artisra.simplecrud.service

import dev.artisra.simplecrud.domain.ReservationStatus
import dev.artisra.simplecrud.repository.ReservationRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class ReservationCleanupTask(
    private val reservationRepository: ReservationRepository,
    private val reservationService: ReservationService
) {
    @Scheduled(fixedRateString = "\${reservation.cleanup.fixed-rate-ms}")
    fun scheduledCleanup() {
        cleanupExpiredReservations()
    }

    fun cleanupExpiredReservations() {
        logger.info("Running reservation cleanup task")
        val now = OffsetDateTime.now()
        val expiredReservations = reservationRepository.findAllByStatusAndExpiresAtBefore(
            ReservationStatus.PENDING,
            now
        )

        if (expiredReservations.isNotEmpty()) {
            logger.info("Found ${expiredReservations.size} expired reservations to clean up")
            
            runBlocking {
                expiredReservations.forEach { reservation ->
                    try {
                        reservationService.expireReservation(reservation.id!!)
                        logger.info("Expired reservation: ${reservation.id}")
                    } catch (e: Exception) {
                        logger.error("Failed to expire reservation: ${reservation.id}", e)
                    }
                }
            }
        } else {
            logger.info("No expired reservations found")
        }
        logger.info("Reservation cleanup task completed")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReservationCleanupTask::class.java)
    }
}
