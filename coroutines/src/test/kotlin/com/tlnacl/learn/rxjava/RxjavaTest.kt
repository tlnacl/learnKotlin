package com.tlnacl.learn.rxjava

import io.reactivex.Observable
import io.reactivex.Observable.interval
import io.reactivex.observers.TestObserver
import org.junit.Test
import java.util.concurrent.TimeUnit


class RxjavaTest {
    @Test
    fun accumulateToProcess() {
        val subscriber = TestObserver.create<Unit>()
        val periodic: Observable<Long> = interval(1, 10, TimeUnit.MILLISECONDS)
        val acc = ArrayList<Long>()
        var count = 0L
        periodic.map { count++ }
                .map { acc.add(it) }
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    println(acc)
                    acc.clear()
                }
                .subscribe(subscriber)
        subscriber.await(1, TimeUnit.SECONDS)
    }
}