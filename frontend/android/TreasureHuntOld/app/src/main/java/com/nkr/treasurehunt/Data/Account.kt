package com.nkr.treasurehunt.Data

internal class Account(var email: String, var pwd: String, var fname: String, var lname: String, var username: String) {
    fun toSer(): SerializableAccount {
        return SerializableAccount(email, pwd, fname, lname, username)
    }
}