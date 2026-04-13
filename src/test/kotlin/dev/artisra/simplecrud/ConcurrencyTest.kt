package dev.artisra.simplecrud

import dev.artisra.simplecrud.service.ProductService
import dev.artisra.simplecrud.service.ReservationService
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Testcontainers
class ConcurrencyTest {

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
    fun `should handle concurrent reservations correctly`() = runBlocking {
        // Given: A product with 10 items in stock
        val product = productService.createProduct("Flash Sale Item", 10)
        val productId = product.id!!

        // When: 100 concurrent reservation requests are made
        val successfulReservations = AtomicInteger(0)
        val failedReservations = AtomicInteger(0)

        val jobs = List(100) {
            async(Dispatchers.Default) {
                try {
                    reservationService.reserveProduct(productId, UUID.randomUUID(), 1)
                    successfulReservations.incrementAndGet()
                } catch (e: Exception) {
                    failedReservations.incrementAndGet()
                }
            }
        }

        jobs.awaitAll()

        // Then: Exactly 10 reservations should succeed
        assertEquals(10, successfulReservations.get(), "Should have exactly 10 successful reservations")
        assertEquals(90, failedReservations.get(), "Should have 90 failed reservations")

        // Final stock should be 0
        val finalProduct = productService.getProduct(productId)
        assertEquals(0, finalProduct.stock, "Final stock should be 0")
    }
}
