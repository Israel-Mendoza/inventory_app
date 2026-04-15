package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.domain.Product
import dev.artisra.simplecrud.service.ProductService
import dev.artisra.simplecrud.web.dto.CreateProductRequest
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
    fun createProduct(@RequestBody request: CreateProductRequest): Product {
        return productService.createProduct(request.name, request.stock)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    fun getProduct(@PathVariable id: UUID): Product {
        return productService.getProduct(id)
    }
}
