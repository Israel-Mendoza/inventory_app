package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.domain.Product
import dev.artisra.simplecrud.domain.Reservation
import dev.artisra.simplecrud.domain.ReservationStatus
import dev.artisra.simplecrud.service.ReservationService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.verify

@WebMvcTest(ReservationController::class, GlobalExceptionHandler::class)
class ReservationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var reservationService: ReservationService

    @Test
    fun `should create a reservation`() {
        runBlocking {
            val productId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val quantity = 3
            val product = Product(id = productId, name = "Reserved Product", stock = 7)
            val reservation = Reservation(
                id = UUID.randomUUID(),
                product = product,
                userId = userId,
                status = ReservationStatus.PENDING,
                quantity = quantity
            )

            `when`(reservationService.reserveProduct(productId, userId, quantity)).thenReturn(reservation)

            val requestBody = """
                {
                    "productId": "$productId",
                    "userId": "$userId",
                    "quantity": $quantity
                }
            """.trimIndent()

            val mvcResult = mockMvc.perform(
                post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.quantity").value(quantity))

            verify(reservationService).reserveProduct(productId, userId, quantity)
        }
    }

    @Test
    fun `should return 409 when product is out of stock`() {
        runBlocking {
            val productId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val quantity = 100

            `when`(reservationService.reserveProduct(productId, userId, quantity))
                .thenThrow(IllegalStateException("Not enough stock for product: $productId"))

            val requestBody = """
                {
                    "productId": "$productId",
                    "userId": "$userId",
                    "quantity": $quantity
                }
            """.trimIndent()

            val mvcResult = mockMvc.perform(
                post("/api/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            ).andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Not enough stock for product: $productId"))
        }
    }

    @Test
    fun `should return reservation by ID`() {
        val reservationId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val product = Product(id = productId, name = "Test Product", stock = 10)
        val reservation = Reservation(
            id = reservationId,
            product = product,
            userId = userId,
            status = ReservationStatus.PENDING,
            quantity = 5
        )

        runBlocking {
            `when`(reservationService.getReservation(reservationId)).thenReturn(reservation)
        }

        val mvcResult = mockMvc.perform(get("/api/reservations/$reservationId")).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(reservationId.toString()))
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.quantity").value(5))
    }

    @Test
    fun `should return 404 when reservation not found`() {
        val reservationId = UUID.randomUUID()
        runBlocking {
            `when`(reservationService.getReservation(reservationId))
                .thenThrow(IllegalArgumentException("Reservation not found: $reservationId"))
        }

        val mvcResult = mockMvc.perform(get("/api/reservations/$reservationId")).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should cancel a reservation`() {
        val reservationId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val product = Product(id = productId, name = "Test Product", stock = 10)
        val cancelledReservation = Reservation(
            id = reservationId,
            product = product,
            userId = userId,
            status = ReservationStatus.CANCELLED,
            quantity = 5
        )

        runBlocking {
            `when`(reservationService.cancelReservation(reservationId)).thenReturn(cancelledReservation)
        }

        val mvcResult = mockMvc.perform(post("/api/reservations/$reservationId/cancel")).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(reservationId.toString()))
            .andExpect(jsonPath("$.status").value("CANCELLED"))

        runBlocking {
            verify(reservationService).cancelReservation(reservationId)
        }
    }

    @Test
    fun `should return 409 when cancelling a non-pending reservation`() {
        val reservationId = UUID.randomUUID()
        runBlocking {
            `when`(reservationService.cancelReservation(reservationId))
                .thenThrow(IllegalStateException("Reservation is not pending: $reservationId"))
        }

        val mvcResult = mockMvc.perform(post("/api/reservations/$reservationId/cancel")).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Reservation is not pending: $reservationId"))
    }

    @Test
    fun `should return 404 when cancelling a non-existent reservation`() {
        val reservationId = UUID.randomUUID()
        runBlocking {
            `when`(reservationService.cancelReservation(reservationId))
                .thenThrow(IllegalArgumentException("Reservation not found: $reservationId"))
        }

        val mvcResult = mockMvc.perform(post("/api/reservations/$reservationId/cancel")).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Reservation not found: $reservationId"))
    }

    @Test
    fun `should confirm a reservation`() {
        val reservationId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val product = Product(id = productId, name = "Test Product", stock = 10)
        val confirmedReservation = Reservation(
            id = reservationId,
            product = product,
            userId = userId,
            status = ReservationStatus.CONFIRMED,
            quantity = 5
        )

        runBlocking {
            `when`(reservationService.confirmReservation(reservationId)).thenReturn(confirmedReservation)
        }

        val mvcResult = mockMvc.perform(post("/api/reservations/$reservationId/confirm")).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(reservationId.toString()))
            .andExpect(jsonPath("$.status").value("CONFIRMED"))

        runBlocking {
            verify(reservationService).confirmReservation(reservationId)
        }
    }
}
