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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(ReservationController::class, GlobalExceptionHandler::class)
class ReservationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var reservationService: ReservationService

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
            status = ReservationStatus.PENDING
        )

        `when`(reservationService.getReservation(reservationId)).thenReturn(reservation)

        mockMvc.perform(get("/api/reservations/$reservationId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(reservationId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    fun `should return 404 when reservation not found`() {
        val reservationId = UUID.randomUUID()
        `when`(reservationService.getReservation(reservationId))
            .thenThrow(IllegalArgumentException("Reservation not found: $reservationId"))

        mockMvc.perform(get("/api/reservations/$reservationId"))
            .andExpect(status().isNotFound)
    }
}
