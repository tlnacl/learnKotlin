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
        sequenceDo()
//        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { getOne() }
    val two = async { getTwo() }
    one.await() + two.await()
}

suspend fun concurrentDo() = coroutineScope {
    launch { doSomething1() }
    launch { doSomething2() }
}

suspend fun sequenceDo() = coroutineScope {
    doSomething1()
    doSomething2()
}

suspend fun getOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 1
}

suspend fun getTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 2
}

suspend fun doSomething1() {
    delay(1000L)
    println("doSomething1")
}

suspend fun doSomething2() {
    delay(1000L)
    println("doSomething2")
}