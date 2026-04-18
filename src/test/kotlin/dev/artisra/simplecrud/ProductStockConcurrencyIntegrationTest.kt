package dev.artisra.simplecrud

import dev.artisra.simplecrud.service.ProductService
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

    @Test
    fun `should handle mixed reservations and stock increases without collisions`() = runBlocking {
        // Given: A product with initial stock
        val initialStock = 100
        val product = productService.createProduct("Concurrent Item", initialStock)
        val productId = product.id!!

        val iterations = 50
        val successfulReservations = AtomicInteger(0)
        val successfulIncreases = AtomicInteger(0)
        val failedOperations = AtomicInteger(0)

        // When: We concurrently try to reserve and increase stock
        // These now share the same ProductLockService mutex
        val jobs = mutableListOf<Job>()
        
        repeat(iterations) {
            // Reservation path
            jobs.add(launch(Dispatchers.Default) {
                try {
                    reservationService.reserveProduct(productId, UUID.randomUUID(), 1)
                    successfulReservations.incrementAndGet()
                } catch (e: Exception) {
                    failedOperations.incrementAndGet()
                }
            })

            // Stock increase path
            jobs.add(launch(Dispatchers.Default) {
                try {
                    productService.increaseStock(productId, 1)
                    successfulIncreases.incrementAndGet()
                } catch (e: Exception) {
                    failedOperations.incrementAndGet()
                }
            })
        }

        jobs.joinAll()

        // Then:
        val finalProduct = productService.getProduct(productId)
        val expectedStock = initialStock - successfulReservations.get() + successfulIncreases.get()
        
        // With the Mutex in place, failedOperations should be 0 because 
        // they wait for the lock instead of colliding and throwing OptimisticLockingFailureException
        assertEquals(0, failedOperations.get(), "Should have 0 failed operations due to contention")
        assertEquals(expectedStock, finalProduct.stock, "Final stock should match the calculated expectation")
        assertEquals(initialStock, finalProduct.stock, "In this specific 1:1 test, stock should return to initial")
    }
}
