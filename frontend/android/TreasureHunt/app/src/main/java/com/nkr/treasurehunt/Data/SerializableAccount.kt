package com.nkr.treasurehunt.Data

import java.io.Serializable

internal class SerializableAccount(var email: String, var pwd: String, var fname: String, var lname: String, var username: String) : Serializable {
    fun toAccount(): Account {
        return Account(email, pwd, fname, lname, username)
    }
}