package dev.artisra.simplecrud

import dev.artisra.simplecrud.service.ProductService
import dev.artisra.simplecrud.service.ReservationCleanupTask
import dev.artisra.simplecrud.service.ReservationService
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Tag("integration")
@SpringBootTest
@Testcontainers
class ProductStockConcurrencyIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17-alpine")
    }

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationCleanupTask: ReservationCleanupTask

    @Test
    fun `should handle concurrent expirations correctly`() = runBlocking {
        // Given: A product with initial stock
        val initialStock = 10
        val product = productService.createProduct("Expiration Test Item", initialStock, 0)
        val productId = product.id!!
        val userId = UUID.randomUUID()

        // Create 10 reservations of 1 each with an immediate expiration
        val reservationIds = (1..10).map {
            // We use a trick here: manually update expiration time in DB or just wait.
            // But since we want to test concurrent expirations, let's keep the logic of calling the service
            // or trigger the cleanup task.
            // If we want to test the cleanup task's concurrency, we'd need to have many expired reservations.
            reservationService.reserveProduct(productId, userId, 1).id!!
        }

        // Stock should be 0 now
        assertEquals(0, productService.getProduct(productId).stock)

        // When: We trigger the cleanup task
        // We need to make sure they are actually expired. 
        // Let's modify the test to manually set them as expired in the database if possible, 
        // or just keep using the reservationService.expireReservation for the concurrency test 
        // as it still exists in the service, even if not in the controller.
        
        // Wait, the requirement says "remove the endpoint that expires a reservation by ID".
        // It doesn't say remove it from the Service. 
        // The Service method might still be useful for the CleanupTask.

        val jobs = reservationIds.map { id ->
            launch(Dispatchers.Default) {
                reservationService.expireReservation(id)
            }
        }
        jobs.joinAll()

        // Then: Stock should be back to 10
        val finalProduct = productService.getProduct(productId)
        assertEquals(initialStock, finalProduct.stock)
        
        // And all reservations should be EXPIRED
        reservationIds.forEach { id ->
            assertEquals(dev.artisra.simplecrud.domain.ReservationStatus.EXPIRED, reservationService.getReservation(id).status)
        }
    }
}
