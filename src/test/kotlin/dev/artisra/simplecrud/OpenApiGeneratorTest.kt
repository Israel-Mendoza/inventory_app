package dev.artisra.simplecrud

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OpenApiGeneratorTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17-alpine")
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun generateOpenApiSpec() {
        val result = mockMvc.perform(get("/v3/api-docs.yaml"))
            .andExpect(status().isOk)
            .andReturn()

        val content = result.response.contentAsString
        File("openapi.yaml").writeText(content)
        println("[DEBUG_LOG] OpenAPI specification generated at openapi.yaml")
    }
}
