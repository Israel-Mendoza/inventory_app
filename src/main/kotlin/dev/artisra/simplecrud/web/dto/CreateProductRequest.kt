package dev.artisra.simplecrud.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to create a new product")
data class CreateProductRequest(
    @Schema(description = "Name of the product", example = "Smartphone")
    val name: String,
    @Schema(description = "Initial stock quantity", example = "100")
    val stock: Int
)
