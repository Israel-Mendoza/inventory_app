package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.domain.Product
import dev.artisra.simplecrud.service.ProductService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import kotlinx.coroutines.runBlocking

@Tag("unit")
@WebMvcTest(ProductController::class, GlobalExceptionHandler::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var productService: ProductService

    @Test
    fun `should create a product`() {
        val productId = UUID.randomUUID()
        val product = Product(id = productId, name = "Smartphone", stock = 100, version = 0, expirationMinutes = 60)

        `when`(productService.createProduct("Smartphone", 100, 60)).thenReturn(product)

        val requestBody = """
            {
                "name": "Smartphone",
                "stock": 100,
                "expirationMinutes": 60
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Smartphone"))
            .andExpect(jsonPath("$.stock").value(100))
            .andExpect(jsonPath("$.version").value(0))
            .andExpect(jsonPath("$.expirationMinutes").value(60))

        verify(productService).createProduct("Smartphone", 100, 60)
    }

    @Test
    fun `should return product by ID`() {
        val productId = UUID.randomUUID()
        val product = Product(id = productId, name = "Laptop", stock = 50, version = 2)

        `when`(productService.getProduct(productId)).thenReturn(product)

        mockMvc.perform(get("/api/products/$productId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.stock").value(50))
            .andExpect(jsonPath("$.version").value(2))

        verify(productService).getProduct(productId)
    }

    @Test
    fun `should create a product with default expiration when not provided`() {
        val productId = UUID.randomUUID()
        val product = Product(id = productId, name = "Tablet", stock = 50, version = 0, expirationMinutes = 0)

        `when`(productService.createProduct("Tablet", 50, 0)).thenReturn(product)

        val requestBody = """
            {
                "name": "Tablet",
                "stock": 50
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.expirationMinutes").value(0))

        verify(productService).createProduct("Tablet", 50, 0)
    }

    @Test
    fun `should return 404 when product not found`() {
        val productId = UUID.randomUUID()

        `when`(productService.getProduct(productId))
            .thenThrow(IllegalArgumentException("Product not found: $productId"))

        mockMvc.perform(get("/api/products/$productId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Product not found: $productId"))
    }

    @Test
    fun `should increase product stock`() {
        val productId = UUID.randomUUID()
        val product = Product(id = productId, name = "Widget", stock = 20, version = 1)

        runBlocking {
            `when`(productService.increaseStock(productId, 5)).thenReturn(product)
        }

        val mvcResult = mockMvc.perform(
            post("/api/products/$productId/stock/increase")
                .param("amount", "5")
        ).andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(productId.toString()))
            .andExpect(jsonPath("$.stock").value(20))

        runBlocking {
            verify(productService).increaseStock(productId, 5)
        }
    }
}

