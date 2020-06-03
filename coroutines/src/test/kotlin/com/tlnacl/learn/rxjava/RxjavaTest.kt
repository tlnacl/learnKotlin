package com.tlnacl.learn.rxjava

import io.reactivex.Observable
import io.reactivex.Observable.interval
import io.reactivex.Single
import io.reactivex.Maybe
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.TimeUnit


class RxjavaTest {

    private var count: Int = 0

    @Test
    fun testSwitchMap() {
        val subscriber = TestObserver.create<String>()
        interval(0, 1000, TimeUnit.MILLISECONDS)
                .switchMap { sourceNum ->
                    interval(0, 200, TimeUnit.MILLISECONDS)
                            .map { num -> "$sourceNum   _$num" }
                }.doOnNext { s -> println(s) }.subscribe(subscriber)
        subscriber.awaitCount(10)
    }

    @Test
    fun testSingleMerge() {
        val subscriber = TestObserver.create<String>()
        val single1 = Single.just("first")
        val single2 = Single.just("second")

        Single.merge(single1, single2).toObservable()
                .doOnNext { s -> println(s) }
                .subscribe(subscriber)
        subscriber.awaitTerminalEvent()

    }

    @Test
    fun testConcatMapEager() {
        val subscriber = TestObserver.create<String>()
        val subject = PublishSubject.create<String>()

        subject.concatMapEager { s -> Observable.just("$s-eager") }
                .compose(MapTest.TestTransformer())
                .doOnNext { s -> println("after compose$s") }
                .flatMapMaybe { s -> toMaybe(s) }
                .doOnNext { s -> println("after maybe$s") }
                .subscribe(subscriber)

        try {
            subscriber.await(1, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        subject.onNext("count:" + count++)
        subject.onNext("count:" + count++)
        subject.onNext("count:" + count++)
        subject.onNext("count:" + count++)
    }

    private fun toMaybe(s: String): Maybe<String> {
        return Maybe.just("$s-toMaybe")
    }

    @Test
    fun testFlatMap() {
        Observable.range(1, 10)
                .doOnNext { i -> println("origin:" + i!!) }
                .flatMap({ v ->
                    if (v < 5) {
                        return@flatMap Observable.range(1, 10)
                                .doOnNext { System.out.println("origin: $it") }
                                .flatMap { Observable.just(v * v) }
                    }
                    Observable.error<Int>(IOException("Why not?!"))
                }, true)
                .onErrorReturnItem(-1)
                .subscribe({ println(it) }, { it.printStackTrace() })
    }

    @Test
    fun testPrefetch() {

        val subscriber = TestObserver.create<String>()
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .take(20)
                .concatMap({ timeStamp -> Observable.timer(1, TimeUnit.SECONDS).map { o -> timeStamp } }, 1)
                .map { it.toString() }
                //        .flatMap(i -> Observable.just(i.toString()))
                .doOnNext { i -> println("time:$i") }
                .subscribe(subscriber)
        subscriber.awaitTerminalEvent()
        //    try {
        //      subscriber.await(10,TimeUnit.SECONDS);
        //    } catch (InterruptedException e) {
        //      e.printStackTrace();
        //    }
    }

    internal inner class TestObject(val id: String, val name: String)

    @Test
    @Throws(InterruptedException::class)
    fun testDebounceSelector() {
        val subscriber = TestObserver.create<TestObject>()
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .map { n -> if (n % 2 == 0L) TestObject("1", "one") else TestObject("2", "two") }
                .debounce(DebounceWithFirst(1, TimeUnit.SECONDS))
                .doOnNext { i -> println("time:" + i.name) }
                .subscribe(subscriber)
        subscriber.await(5, TimeUnit.SECONDS)
    }

    @Test
    @Throws(Exception::class)
    fun testSwitchMapCancelledByNewEvent() {
        val source = BehaviorSubject.create<Int>()
        source
                .switchMap { o -> Observable.interval(1000, TimeUnit.MILLISECONDS) }
                .subscribe { o -> println("Event : " + o!!) }

        source.onNext(0)
        for (i in 0..9) {
            Thread.sleep(500)
            source.onNext(0)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testReplay() {
        val booleanObservable = Observable.just(true).replay(1).autoConnect().distinctUntilChanged()

        val booleanSingle = booleanObservable.onErrorReturnItem(false).first(false)

        val subscriber = TestObserver.create<TestObject>()
        booleanSingle.subscribe { b -> println("first get" + b!!) }
        booleanSingle.subscribe { b -> println("second get" + b!!) }
    }

    @Test
    @Throws(Exception::class)
    fun testDistinctUnitChange() {
        val subscriber = TestObserver.create<Boolean>()
        val source = BehaviorSubject.create<Pair<Boolean, Boolean>>()
        source.distinctUntilChanged()
                .map { (first) -> first }
                .distinctUntilChanged()
                .doOnNext { f -> println("first:" + f!!) }
                .subscribe(subscriber)

        source.onNext(Pair(false, false))
        source.onNext(Pair(false, true))
        source.onNext(Pair(false, false))
        source.onNext(Pair(true, false))
        subscriber.await(1, TimeUnit.SECONDS)
    }

    @Test
    @Throws(Exception::class)
    fun testFirstElement() {
        val subscriber = TestObserver.create<Boolean>()
        val defer = Observable.defer {
            Thread.sleep(1000)
            Observable.just(true)
        }

        defer.firstElement()
                .subscribe(subscriber)
        subscriber.assertValue(true)
    }

    @Test
    @Throws(Exception::class)
    fun testFlatMapMaybe() {
        val subscriber = TestObserver.create<String>()
        Observable.fromArray("1", "2", "3")
                .flatMapMaybe { if (it == "2") Maybe.empty() else Maybe.just(it) }
                .doOnNext { println("number $it") }
                .subscribe(subscriber)
        subscriber.awaitTerminalEvent()
    }

    @Test
    @Throws(Exception::class)
    fun testMaybeEmpty() {
        val subscriber = TestObserver.create<String>()
        Maybe.empty<String>()
                .doOnComplete { println("complete") }
                .doAfterSuccess { println("number $it") }
                .subscribe(subscriber)
        subscriber.awaitTerminalEvent()
    }

    @Test
    fun testMulticasting() {
        val subscriber = TestObserver.create<Int>()
        val multicasting = Multicasting()

        multicasting.addValue(1)

        multicasting.observeValue()
                .doOnNext { println("First $it") }
                .subscribe { subscriber }

        multicasting.observeValue()
                .doOnNext { println("Second $it") }
                .subscribe { subscriber }

        multicasting.observeV()
                .doOnNext { println("filtered $it") }
                .subscribe { subscriber }

        multicasting.addValue(2)
        multicasting.addValue(3)
        multicasting.addValue(4)
        multicasting.addValue(5)

        subscriber.awaitCount(10)

    }

    class Multicasting {
        val subject: BehaviorSubject<Int> = BehaviorSubject.create()

        fun addValue(value: Int) {
            subject.onNext(value)
        }

        fun observeValue(): Observable<Int> {
            return subject.observeOn(Schedulers.io())
        }

        fun observeV(): Observable<Int> {
            return observeValue().filter { it < 3 }.observeOn(Schedulers.io())
        }
    }

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