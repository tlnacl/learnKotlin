package com.tlnacl.learn.crv

import com.tlnacl.learn.cvr.EldEvent
import com.tlnacl.learn.cvr.User
import com.tlnacl.learn.cvr.UserState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxSampleTest {

    private val currentUserSubject: Subject<UserState> = BehaviorSubject.create()

    fun mayUserById(userId: String): Maybe<User> {
        if (userId == "4") return Maybe.empty()
        if (userId == "5") throw IllegalArgumentException("Test error handling")
        return Maybe.just(User(userId, "User$userId")).delay(10, TimeUnit.MILLISECONDS)
    }

    fun getPrimaryUserId(): Single<String> = Single.just("1").delay(10, TimeUnit.MILLISECONDS)

    fun getCurrentAccountId(): Single<String> = Single.just("Account1").delay(10, TimeUnit.MILLISECONDS)

    fun saveEldEvent(accountId: String, user: User): Single<EldEvent> = Single.just(EldEvent(accountId, user)).delay(50, TimeUnit.MILLISECONDS)

    // When user change we create a EldEvent for co-driver
    fun createCoDriverEvent() {
        currentUserSubject.distinctUntilChanged()
                .filter { it.userIds.isNotEmpty() }
                .filter { it.accountId == getCurrentAccountId().blockingGet() } // very ugly if not use blockingGet()
                .doOnNext { println("UserState: $it") } // doOnNext for logging
                .flatMap { userState ->
                    Observable.fromIterable(userState.userIds)
                            .filter { userId -> userId != getPrimaryUserId().blockingGet() }
                            .flatMapMaybe { userId -> mayUserById(userId) } // If can't find user by id it will onComplete which is hidden
                            .flatMapSingle { user -> saveEldEvent(userState.accountId, user) }
                }
                .subscribe({ println("Create user change eldEvent: $it success") },
                        { println("error: it") })
    }

    @Test
    fun `test create co driver event rxjava`() {
        createCoDriverEvent()
        currentUserSubject.onNext(UserState("Account1", listOf("1", "2")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "2")))
        currentUserSubject.onNext(UserState("Account2", listOf("1", "2")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "3")))
//        currentUserSubject.onNext(UserState("Account1", listOf("1", "5")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "4")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "2", "3")))

        Thread.sleep(1000)
    }
}