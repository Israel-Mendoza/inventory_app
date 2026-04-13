package dev.artisra.simplecrud.repository

import dev.artisra.simplecrud.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID>
