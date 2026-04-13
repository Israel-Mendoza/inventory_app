package dev.artisra.simplecrud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleCrudApplication

fun main(args: Array<String>) {
    runApplication<SimpleCrudApplication>(*args)
}
