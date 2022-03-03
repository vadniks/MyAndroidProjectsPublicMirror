/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.Application
import androidx.annotation.Keep
import .mvp.model.impl.ModelAccess

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
@Suppress("UNUSED")
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val m = ModelAccess.initModel(this)

        Thread.setDefaultUncaughtExceptionHandler(m::handleUncoughtException)
    }

    companion object {
        @Keep
        const val A = ""
        // Created by Vad Nik (from ) 2018-2019
    }
}
