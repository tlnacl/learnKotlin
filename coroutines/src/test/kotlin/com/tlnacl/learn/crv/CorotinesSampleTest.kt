package com.tlnacl.learn.crv

import com.tlnacl.learn.cvr.EldEvent
import com.tlnacl.learn.cvr.User
import com.tlnacl.learn.cvr.UserState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.distinct
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.system.measureTimeMillis

class CorotinesSampleTest {
    //    val sharedFlow: SharedFlow<UserState> = _sharedFlow
    suspend fun getUserById(userId: String): User? {
        if (userId == "4") return null
        if (userId == "errorId") throw IllegalArgumentException("Test error handling")
        delay(10)
        return User(userId, "User$userId")
    }

    suspend fun getPrimaryUserId(): String {
        delay(10)
        return "1"
    }

    suspend fun getCurrentAccountId(): String {
        delay(10)
        return "Account1"
    }

    suspend fun saveEldEvent(accountId: String, user: User): EldEvent {
        delay(50)
        return EldEvent(accountId, user)
    }

    suspend fun createCoDriverEvent(userState: UserState) {
        try {
            val time = measureTimeMillis {
                if (userState.userIds.isEmpty() || userState.accountId != getCurrentAccountId()) return
                userState.userIds
                        .filter { it != getPrimaryUserId() }
                        .forEach { userId -> // Not running concurrent
                            val user = getUserById(userId) ?: return@forEach
                            val eldEvent = saveEldEvent(userState.accountId, user)
                            println("Create user change eldEvent: $eldEvent success")
                        }
            }
            println("UserState: $userState Completed in $time ms")
        } catch (e: Exception) {
            println(println("error : e"))
        }
    }

    @Test
    fun `test test user states change to create co driver event coroutine`() {
        runBlocking {
            flow {
                emit(UserState("Account1", listOf("1", "2")))
                emit(UserState("Account1", listOf("1", "2")))
                emit(UserState("Account2", listOf("1", "2")))
                emit(UserState("Account1", listOf("1", "3")))
//            emit(UserState("Account1", listOf("1", "errorId")))
                emit(UserState("Account1", listOf("1", "4")))
                emit(UserState("Account1", listOf("1", "2", "3")))
//            delay(50)
            }.distinctUntilChanged().collect { userState ->
                createCoDriverEvent(userState)
            }
        }
    }

    suspend fun concurrentSave(userState: UserState) {
        try {
            val time = measureTimeMillis {
                if (userState.userIds.isEmpty() || userState.accountId != getCurrentAccountId()) return
                for (userId in userState.userIds) {
                    if (userId != getPrimaryUserId()) {
                        continue
                    } else {
                        val user = getUserById(userId) ?: continue
                        val eldEvent = saveEldEvent(userState.accountId, user)
                        println("Create user change eldEvent: $eldEvent success")
                    }
                }
            }
            println("UserState: $userState Completed in $time ms")
        } catch (e: Exception) {
            println(println("error : e"))
        }
    }
}