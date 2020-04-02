package com.tlnacl.learn

import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val time = measureTimeMillis {
        // launch return a job and we can get job inside launch
        launch {
            val job = coroutineContext[Job]!!
            println("Job $job")
            withTimeout(10000) {
                val result = doSomething()
                println("The answer is $result")
            }
        }
    }
    println("Completed in $time ms")
}

//suspendCancellableCoroutine

suspend fun doSomething(): Int {
    delay(1000) // pretend we are doing something useful here
    return 13
}