package com.tlnacl.learn.rxjava

import io.reactivex.Observable
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DebounceWithFirst<T>(
        val timeout: Long,
        val timeUnit: TimeUnit) : Function<T, Observable<T>> {

    val isFirstEmission: AtomicBoolean = AtomicBoolean(true)

    override fun apply(item: T): Observable<T> {
        if (isFirstEmission.getAndSet(false)) return Observable.just(item);
        return Observable.just(item).delay(timeout, timeUnit);
    }
}