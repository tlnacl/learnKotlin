package com.tlnacl.learn

import com.tlnacl.learn.utils.ControlledRunner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CoroutineRunnerTest {
    @Test
    fun testControlledRunner() {
        val controlledRunner = ControlledRunner<Unit>()
        runBlocking {
            launch { controlledRun(controlledRunner, 1) }
            delay(50)
            launch { controlledRun(controlledRunner, 2) }
            delay(210)
            launch { controlledRun(controlledRunner, 3) }
        }
    }

    private suspend fun controlledRun(controlledRunner: ControlledRunner<Unit>, taskNo: Int) {
        return controlledRunner.joinPreviousOrRun {
//        return controlledRunner.cancelPreviousThenRun {
            longRunningTask(taskNo)
        }
    }

    private suspend fun longRunningTask(taskNo: Int) {
        println("start longRunningTask $taskNo")
        delay(200)
        println("finish longRunningTask $taskNo")

    }
}