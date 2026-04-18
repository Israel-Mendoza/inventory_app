package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.service.ProductService
import dev.artisra.simplecrud.web.dto.CreateProductRequest
import dev.artisra.simplecrud.web.dto.ProductResponse
import dev.artisra.simplecrud.web.dto.toProductResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
class ProductController(private val productService: ProductService) {

    @PostMapping
    @Operation(summary = "Create a new product")
    fun createProduct(@RequestBody request: CreateProductRequest): ProductResponse {
        return productService.createProduct(request.name, request.stock).toProductResponse()
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    fun getProduct(@PathVariable id: UUID): ProductResponse {
        return productService.getProduct(id).toProductResponse()
    }

    @PostMapping("/{id}/stock/increase")
    @Operation(summary = "Increase stock for a product by ID")
    suspend fun increaseStock(@PathVariable id: UUID, @RequestParam amount: Int): ProductResponse {
        return productService.increaseStock(id, amount).toProductResponse()
    }
}
