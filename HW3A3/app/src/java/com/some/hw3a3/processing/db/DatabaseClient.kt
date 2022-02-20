package com.some.hw3a3.processing.db

import android.content.Context
import android.support.annotation.StringDef

import com.some.hw3a3.processing.model.Repo

import io.realm.Realm
import io.realm.kotlin.where

/**
 * @author Vad Nik.
 * @version dated Sep 18, 2018.
 * @link http://github.com/vadniks
 */
class DatabaseClient {
    private val realm = Realm.getDefaultInstance()

    companion object {
        fun init(context: Context): Unit = Realm.init(context)

        const val ID = "id"
        const val NAME = "name"
        const val OWNER = "owner"
        const val FULL_NAME = "full_name"

        @StringDef(ID, NAME, OWNER, FULL_NAME)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Field
    }

    fun close(): Unit = realm.close()

    fun write(repos: ArrayList<Repo>) {
        realm.beginTransaction()

        for (r in repos)
            realm.copyToRealm(r)

        realm.commitTransaction()
    }

    fun set(repo: Repo) {
        realm.beginTransaction()
        realm.copyToRealm(repo)
        realm.commitTransaction()
    }

    fun read() : ArrayList<Repo> {
        val array = ArrayList<Repo>()

        for (r in realm.where<Repo>().findAll().listIterator())
            array.add(r)

        return array
    }

    fun get(id: Int): Repo? = realm.where<Repo>().equalTo(ID, id).findFirst()

    fun get(/*not working in Kotlin*/@Field field: String, value: String): Repo? =
            realm.where<Repo>().equalTo(field, value).findFirst()

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        return other::class.java.name == this::class.java.name
    }
}
