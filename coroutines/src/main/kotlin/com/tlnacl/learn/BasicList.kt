package com.tlnacl.learn

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main() = runBlocking {
    val time = measureTimeMillis {
//        val lists = (1..2).pmap { doSomethingRandom() }
        val lists = concurrentList()
        log("The answer is $lists")
    }
    log("Completed in $time ms")
}

suspend fun concurrentList() = coroutineScope {
    val list = ArrayList<Int>()
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }

    list.add(one.await())
    list.add(two.await())
    list
}

suspend fun concurrentFun() = coroutineScope {
    val list = ArrayList<Int>()
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }

    list.add(one.await())
    list.add(two.await())
    list
}


suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.map { it.await() }
}

suspend fun doSomethingRandom(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    val n = Random.nextInt(20)
    log("The value is $n")
    return n
}