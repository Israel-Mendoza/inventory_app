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
    fun `should handle concurrent expirations correctly`() = runBlocking {
        // Given: A product with initial stock
        val initialStock = 10
        val product = productService.createProduct("Expiration Test Item", initialStock)
        val productId = product.id!!
        val userId = UUID.randomUUID()

        // Create 10 reservations of 1 each
        val reservationIds = (1..10).map {
            reservationService.reserveProduct(productId, userId, 1).id!!
        }

        // Stock should be 0 now
        assertEquals(0, productService.getProduct(productId).stock)

        // When: We concurrently expire all reservations
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
