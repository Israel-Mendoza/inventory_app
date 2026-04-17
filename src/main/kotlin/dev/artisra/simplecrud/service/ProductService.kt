package dev.artisra.simplecrud.service

import dev.artisra.simplecrud.domain.Product
import dev.artisra.simplecrud.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductService(private val productRepository: ProductRepository) {

    @Transactional
    fun createProduct(name: String, stock: Int): Product {
        return productRepository.save(Product(name = name, stock = stock))
    }

    @Transactional
    fun getProduct(id: UUID): Product {
        return productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found: $id") }
    }

    @Transactional
    fun deductStock(productId: UUID, quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }

        if (product.stock < quantity) {
            throw IllegalStateException("Not enough stock for product: $productId. Requested: $quantity, Available: ${product.stock}")
        }

        product.stock -= quantity
        return productRepository.save(product)
    }

    @Transactional
    fun increaseStock(id: UUID, quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        val product = getProduct(id)
        product.stock += quantity
        return productRepository.save(product)
    }
}
