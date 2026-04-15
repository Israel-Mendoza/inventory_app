package dev.artisra.simplecrud.web

import dev.artisra.simplecrud.domain.Product
import dev.artisra.simplecrud.service.ProductService
import dev.artisra.simplecrud.web.dto.CreateProductRequest
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): Product {
        return productService.createProduct(request.name, request.stock)
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: UUID): Product {
        return productService.getProduct(id)
    }
}
