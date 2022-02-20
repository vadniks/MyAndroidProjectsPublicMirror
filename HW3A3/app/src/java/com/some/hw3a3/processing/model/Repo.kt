package com.some.hw3a3.processing.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * @author Vad Nik.
 * @version dated Sep 14, 2018.
 * @link http://github.com/vadniks
 */
open class Repo(/*@PrimaryKey */var id: Int, var name: String, var owner: GithubUser, var full_name: String)
    : RealmObject() {

    constructor() : this(0, "", GithubUser(0, "", ""), "")
}
