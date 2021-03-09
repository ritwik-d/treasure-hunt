package com.nkr.treasurehunt.Data

class Account(var email: String, var pwd: String, var fname: String, var lname: String, var username: String) {
    fun toSer() : SerializableAccount {
        return SerializableAccount(this.email, this.pwd, this.fname, this.lname, username)
    }
}