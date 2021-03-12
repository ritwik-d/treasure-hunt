package com.ritwikscompany.treasurehunt.utils

internal class Account(var email: String, var pwd: String, var username: String) {
    fun toSer(): SerializableAccount {
        return SerializableAccount(email, pwd, username)
    }
}