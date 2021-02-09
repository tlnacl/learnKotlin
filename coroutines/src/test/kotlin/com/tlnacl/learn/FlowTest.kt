package com.tlnacl.learn

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

class FlowTest {
    @Test
    fun testFlow() = runBlockingTest {
        val flow = flow {
            emit(1)
            delay(100)
            emit(2)
        }

        flow.collect { value -> print(value) }
//        Assert.assertEquals(
//                listOf(1, 2),
//                flow.toList()
//        )
    }

    private fun requestFlow(i: Int, delay: Long = 500): Flow<String> = flow {
        emit("$i: First")
        delay(delay) // wait 500 ms
        emit("$i: Second")
    }

    // Flattening flows
    @Test
    fun testFlatMapConcat() = runBlocking {
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
                .flatMapConcat { requestFlow(it) }
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start")
                }
    }

    @Test
    fun testFlatMapMerge() = runBlocking {
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
                .flatMapMerge { requestFlow(it) }
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start")
                }
    }

private fun testFlow(i: Int):Flow<String> = channelFlow {
    send("$i: First")
    delay(500)
    send("$i: Second")
}

    @Test
    fun testFlatMapLatest() = runBlocking {
        var startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(10) } // a number every 100 ms
                .flatMapLatest { testFlow(it) }
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms")
                }

        println()
        startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
                .flatMapLatest { requestFlow(it, 50) }
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms")
                }
    }

    @Test
    fun testCombine() = runBlocking {
        val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
        val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
        val startTime = System.currentTimeMillis() // remember the start time
        nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start")
                }
    }

    @Test
    fun testConflate() = runBlockingTest {
        val queryFlow = flow {
            emit("a")
            delay(100)
            emit("b")
            delay(100)
            emit("c")
            delay(200)
            emit("d")
        }

//        queryFlow.conflate()
//                .flatMapMerge {  }
//
//        println(flow.toList())
    }

}