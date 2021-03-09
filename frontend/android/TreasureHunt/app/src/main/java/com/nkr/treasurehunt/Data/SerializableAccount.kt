package com.nkr.treasurehunt.Data

import java.io.Serializable

class SerializableAccount(var email: String, var pwd: String, var fname: String, var lname: String, var username: String) : Serializable {
    fun toAccount() : Account {
        return Account(this.email, this.pwd, this.fname, this.lname, username)
    }
}