package com.some.hw3a3

import android.app.Application
import com.some.hw3a3.processing.dagger.DaggerDatabaseComponent
import com.some.hw3a3.processing.dagger.DatabaseComponent
import com.some.hw3a3.processing.db.DatabaseClient

/**
 * @author Vad Nik.
 * @version dated Sep 22, 2018.
 * @link http://github.com/vadniks
 */
class App : Application() {

    companion object {
        private lateinit var component: DatabaseComponent

        fun getComponent(): DatabaseComponent = component
    }

    override fun onCreate() {
        super.onCreate()

        DatabaseClient.init(this)

        component = DaggerDatabaseComponent.create()
    }
}
