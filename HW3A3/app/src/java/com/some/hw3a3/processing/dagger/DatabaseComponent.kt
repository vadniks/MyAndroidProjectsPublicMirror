package com.some.hw3a3.processing.dagger

import com.some.hw3a3.model.Model
import dagger.Component

/**
 * @author Vad Nik.
 * @version dated Sep 22, 2018.
 * @link http://github.com/vadniks
 */
@Component(modules = [DatabaseModule::class])
interface DatabaseComponent {

    fun inject(model: Model)
}
