package com.ritwikscompany.treasurehunt.utils

import java.io.Serializable

internal class SerializableAccount(var email: String, var pwd: String, var username: String) : Serializable {
    fun toAccount(): Account {
        return Account(email, pwd, username)
    }
}