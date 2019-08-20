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
        if (userId == "errorId") throw IllegalArgumentException("Test error handling")
        return Maybe.just(User(userId, "User$userId")).delay(10, TimeUnit.MILLISECONDS)
    }

    fun getPrimaryUserId(): Single<String> = Single.just("1").delay(10, TimeUnit.MILLISECONDS)

    fun getCurrentAccountId(): Single<String> = Single.just("Account1").delay(10, TimeUnit.MILLISECONDS)

    fun saveEldEvent(accountId: String, user: User): Single<EldEvent> = Single.just(EldEvent(accountId, user)).delay(50, TimeUnit.MILLISECONDS)

    // When user change we create a EldEvent for co-driver
    fun createCoDriverEvent(): Observable<EldEvent> {
        return currentUserSubject.distinctUntilChanged()
                .filter { it.userIds.isNotEmpty() }
                .filter { it.accountId == getCurrentAccountId().blockingGet() } // very ugly if not use blockingGet()
//                .concatMap { userState -> // Totally different with concatMap/flatMap/switchMap/
//                    println("UserState: $userState")
//                    getCurrentAccountId()
//                            .flatMapObservable { id ->
////                                println("handle userState accountId ${userState.accountId}")
//                                if (id == userState.accountId) {
//                                    return@flatMapObservable Observable.just(userState)
//                                }
//                                Observable.empty<UserState>()
//                            }
//                }
                .flatMap { userState ->
                    Observable.fromIterable(userState.userIds)
                            .filter { userId -> userId != getPrimaryUserId().blockingGet() }
                            .flatMapMaybe { userId -> mayUserById(userId) } // If can't find user by id it will onComplete which is hidden
                            .flatMapSingle { user -> saveEldEvent(userState.accountId, user) } //it is doing concurrently
                }
                .doOnNext { println("Create user change eldEvent: $it success") }
//                .subscribe({ println("Create user change eldEvent: $it success") },
//                        { println("error: it") })
    }

    @Test
    fun `test user states change to create co driver event rxjava`() {
        val testSubscriber = TestObserver<EldEvent>()
        createCoDriverEvent()
                .subscribe(testSubscriber)
        currentUserSubject.onNext(UserState("Account1", listOf("1", "2")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "2")))
        currentUserSubject.onNext(UserState("Account2", listOf("1", "2")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "3")))
//        currentUserSubject.onNext(UserState("Account1", listOf("1", "errorId")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "4")))
        currentUserSubject.onNext(UserState("Account1", listOf("1", "2", "3")))

//      EldEvent(accountId=Account1, user=User(id=2, name=User2)) success
//      EldEvent(accountId=Account1, user=User(id=3, name=User3)) success
//      EldEvent(accountId=Account1, user=User(id=2, name=User2)) success
//      EldEvent(accountId=Account1, user=User(id=3, name=User3)) success

        testSubscriber.awaitCount(4)
    }
}