package com.tlnacl.learn

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class ActorTest {
    @Test
    fun testActor() {
        val time = measureTimeMillis {
            arrayActor()
        }
        println("Completed in $time ms")
    }

    /**
     * Actor == named coroutine & inbox channel
     * Channel is used to communicate between different thread/coroutine? to avoid mutable shared state
     * Actor has Channel inside/inbox
     */
    private fun useActor() = runBlocking {
        val printer = actor<Int>(coroutineContext) {
            for (i in channel) {
                println(i)
            }
        }

        launch(coroutineContext) {
            repeat(10) { i ->
                delay(100)
                printer.send(i)
            }
            printer.close()
        }
    }

    private fun arrayActor() = runBlocking {
        val msgsActor = actor<List<Int>> {
            var idle = true
            val cache = HashSet<Int>()
            for (msgs in channel) {
                println("add messages $msgs")
                cache.addAll(msgs)
                if (idle && cache.isNotEmpty()) {
                    launch {
                        idle = false
                        processMsgs(cache)
                        cache.clear()
                        idle = true
                    }
                }
            }

        }

        launch(coroutineContext) {
            delay(100)
            msgsActor.send(listOf(1, 2))
            delay(50)
            msgsActor.send(listOf(1, 2, 3, 4, 5))
            delay(100)
            msgsActor.send(listOf(3, 4, 5, 6, 7, 8))
            delay(200)
            msgsActor.send(listOf(7, 8, 9))
            msgsActor.close()
        }
    }

    suspend fun processMsgs(msgs: HashSet<Int>) {
        delay(300)
        println("processMsgs $msgs")
    }

    // Message types for counterActor
    sealed class CounterMsg {
        object IncCounter : CounterMsg() // one-way message to increment counter
        class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply
    }

    fun CoroutineScope.counterActor() = actor<CounterMsg> {
        var counter = 0 // actor state
        for (msg in channel) { // iterate over incoming messages
            when (msg) {
                is CounterMsg.IncCounter -> counter++
                is CounterMsg.GetCounter -> msg.response.complete(counter)
            }
        }
    }

    private fun counterActor() = runBlocking {
        val counter = counterActor() // create the actor
        withContext(Dispatchers.Default) {
            repeat(10) { i ->
                delay(100)
                counter.send(CounterMsg.IncCounter)
            }
        }
        // send a message to get a counter value from an actor
        val response = CompletableDeferred<Int>()
        counter.send(CounterMsg.GetCounter(response))
        println("Counter = ${response.await()}")
        counter.close() // shutdown the actor
    }
}