package com.tlnacl.learn.crv

import com.tlnacl.learn.cvr.EldEvent
import com.tlnacl.learn.cvr.User
import com.tlnacl.learn.cvr.UserState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.distinct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.Exception

class CorotinesSampleTest {
    val userChannel = Channel<UserState>()
    suspend fun addUserState(userState: UserState) {
        userChannel.send(userState)
    }

    suspend fun getUsers(userIds: List<String>): List<User> {
        delay(10)
        return userIds.map { User(it, "User$it") }
    }

    suspend fun getUserById(userId: String): User? {
        if (userId == "4") return null
        if (userId == "5") throw IllegalArgumentException("Test error handling")
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

    suspend fun getprimaryVehicleId(): String? {
        delay(10)
        return "1"
    }

    suspend fun saveEldEvent(accountId: String, user: User): EldEvent {
        delay(50)
        return EldEvent(accountId, user)
    }

    suspend fun createCoDriverEvent(userState: UserState) {
        try {
            if (userState.userIds.isEmpty() || userState.accountId != getCurrentAccountId()) return
            println("UserState: $userState")
            userState.userIds
                    .filter { it != getPrimaryUserId() }
                    .forEach { userId ->
                        val user = getUserById(userId) ?: return@forEach
                        val eldEvent = saveEldEvent(userState.accountId, user)
                        println("Create user change eldEvent: $eldEvent success")
                    }
        } catch (e: Exception) {
            println(println("error : e"))
        }
    }

    @Test
    fun `test create co driver event coroutine`() {
        runBlocking {
            launch {
                userChannel.distinct().consumeEach { userState ->
                    createCoDriverEvent(userState)
                }
            }
            userChannel.send(UserState("Account1", listOf("1", "2")))
            userChannel.send(UserState("Account1", listOf("1", "2")))
            userChannel.send(UserState("Account2", listOf("1", "2")))
            userChannel.send(UserState("Account1", listOf("1", "3")))
//            userChannel.send(UserState("Account1", listOf("1", "5")))
            userChannel.send(UserState("Account1", listOf("1", "4")))
            userChannel.send(UserState("Account1", listOf("1", "2", "3")))
            delay(1000)
            userChannel.close()
        }
    }
}