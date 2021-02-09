package com.tlnacl.learn

import com.tlnacl.learn.utils.ControlledRunner
import com.tlnacl.learn.utils.SingleRunner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CoroutineRunnerTest {
    @Test
    fun testJoinPrevious() {
        val controlledRunner = ControlledRunner<Int>()
        runBlocking {
            launch { println("1result ${joinPrevious(controlledRunner, 1)}") }
            delay(150)
            launch { println("2result ${joinPrevious(controlledRunner, 2)}") }
            delay(210)
            launch { println("3result ${joinPrevious(controlledRunner, 3)}") }
        }
    }

    @Test
    fun testCancelPrevious() {
        val controlledRunner = ControlledRunner<Int>()
        runBlocking {
            launch { println("1result ${cancelPrevious(controlledRunner, 1)}") }
            delay(150)
            launch { println("2result ${cancelPrevious(controlledRunner, 2)}") }
            delay(220)
            launch { println("3result ${cancelPrevious(controlledRunner, 3)}") }
        }
    }

    @Test
    fun testSingleRunner() {
        val singleRunner = SingleRunner()
        runBlocking {
            launch { println("1result ${single(singleRunner, 1)}") }
            delay(10)
            launch { println("2result ${single(singleRunner, 2)}") }
            delay(20)
            launch { println("3result ${single(singleRunner, 3)}") }
        }
    }

    private suspend fun joinPrevious(controlledRunner: ControlledRunner<Int>, taskNo: Int): Int {
        return controlledRunner.joinPreviousOrRun {
            longRunningTask(taskNo)
        }
    }

    private suspend fun cancelPrevious(controlledRunner: ControlledRunner<Int>, taskNo: Int): Int {
        return controlledRunner.cancelPreviousThenRun {
            longRunningTask(taskNo)
        }
    }

    private suspend fun single(singleRunner: SingleRunner, taskNo: Int): Int {
        return singleRunner.afterPrevious {
            longRunningTask(taskNo)
        }
    }

    private suspend fun longRunningTask(taskNo: Int): Int {
        println("start longRunningTask $taskNo")
        delay(100)
        println("longRunningTask after 100ms $taskNo")
        delay(100)
        println("finish longRunningTask $taskNo")
        return taskNo
    }
}