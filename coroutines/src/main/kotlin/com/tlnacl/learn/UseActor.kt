package com.tlnacl.learn

import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Actor == named coroutine & inbox channel
 * Channel is used to communicate between different thread/coroutine? to avoid mutable shared state
 * Actor has Channel inside/inbox
 */
fun useActor() = runBlocking {
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
