package com.ritwikscompany.treasurehunt.utils

import com.ritwikscompany.treasurehunt.utils.SerializableAccount

internal class Account(var email: String, var pwd: String, var fname: String, var lname: String, var username: String) {
    fun toSer(): SerializableAccount {
        return SerializableAccount(email, pwd, fname, lname, username)
    }
}