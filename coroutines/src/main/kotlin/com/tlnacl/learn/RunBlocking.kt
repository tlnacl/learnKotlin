package com.tlnacl.learn

import kotlinx.coroutines.*
import kotlin.system.*

fun main() = runBlocking {
    val time = measureTimeMillis {
//        runBlocking/launch {
//            delay(1000L)
//            val one = async { doSomethingUsefulOne() }
//            val two = async { doSomethingUsefulTwo() }
//            println("The answer is ${one.await() + two.await()}")
//        }
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}