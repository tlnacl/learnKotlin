package com.tlnacl.learn

data class SomeObject(var someField: String = "some default value", var otherField: Int = 55)

fun main() = run {
    val runResult = useRun()
    println("runResult: $runResult")

    val applyResult = useApply()
    println("runResult: $applyResult")
}

fun useRun(): String {
    val so = SomeObject()
    return so.run {
        println("start run: $this")
        someField = "run"
        println("end run: $this")
        someField
    }
}

fun useApply(): SomeObject {
    val so = SomeObject()
    return so.apply {
        println("start apply: $this")
        someField = "run"
        println("end apply: $this")
        someField
    }
}

fun useLet(): String {
    return SomeObject().let {
        println("start run: $it")
        it.someField = "run"
        println("end run: $it")
        it.someField
    }
}


