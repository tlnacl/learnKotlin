package com.tlnacl.learn.cvr

data class User(val id: String, val name: String)
data class UserState(val accountId: String, val userIds: List<String>)
data class EldEvent(val accountId: String, val user: User)