package dev.artisra.simplecrud.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProductLockService {
    private val lockStripes = 64
    private val locks = Array(lockStripes) { Mutex() }

    private fun getLockForProduct(productId: UUID): Mutex {
        val index = (productId.hashCode() and Int.MAX_VALUE) % lockStripes
        return locks[index]
    }

    suspend fun <T> withLock(productId: UUID, action: suspend () -> T): T {
        return getLockForProduct(productId).withLock {
            action()
        }
    }
}
