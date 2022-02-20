package com.some.hw3a3.processing.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * @author Vad Nik.
 * @version dated Sep 14, 2018.
 * @link http://github.com/vadniks
 */
//Realm... Maybe I should use sugar?
open /*data*/ class GithubUser(/*@PrimaryKey */var id: Int, var login: String, var avatar_url: String) :
        RealmObject() {

    constructor() : this(0, "", "")

    override fun toString(): String = "$login-$avatar_url"
}
