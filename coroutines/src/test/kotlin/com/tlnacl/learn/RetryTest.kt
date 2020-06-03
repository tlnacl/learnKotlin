package com.tlnacl.learn

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.ws.http.HTTPException

class RetryTest {
    @Test
    fun testRetry() {
        runBlocking {
            retryApiCall { apiCallServerException() }
        }
    }

    fun apiCallException(){
        throw Exception("Normal Exception")
    }

    fun apiCallIOException(){
        throw IOException("Normal Exception")
    }

    fun apiCallClientException() {
        throw HTTPException(400)
    }

    fun apiCallServerException() {
        throw HTTPException(500)
    }

    suspend fun <T> retryApiCall(
            times: Int = 3,
            initialDelay: Long = 100, // 0.1 second
            maxDelay: Long = 1000,    // 1 second
            factor: Double = 2.0,
            block: suspend () -> T): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: IOException) {
                println("IOException in retry: $e")
            } catch (e: HTTPException) {
                if(e.statusCode != 500) throw e
                else println("server exception in retry: $e")
            }
            delay(currentDelay)
            println("Got io exception retry it after delay $currentDelay")
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
        return block() // last attempt
    }
}