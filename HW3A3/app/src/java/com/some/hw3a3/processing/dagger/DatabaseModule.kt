package com.some.hw3a3.processing.dagger

import com.some.hw3a3.processing.db.DatabaseClient
import dagger.Module
import dagger.Provides

/**
 * @author Vad Nik.
 * @version dated Sep 22, 2018.
 * @link http://github.com/vadniks
 */
@Module
class DatabaseModule {

    @Provides
    fun getDatabase(): DatabaseClient = DatabaseClient()
}
