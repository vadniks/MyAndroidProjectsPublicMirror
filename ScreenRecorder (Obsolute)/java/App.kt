/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager

import java.lang.ref.WeakReference

/**
 * @author Vad Nik.
 * @version dated September 1, 2018.
 * @link http://github.com/vadniks
 */
class App : Application() {

    init {
        println("testo app init")
    }

    companion object {
        internal lateinit var app: WeakReference<App>
        internal var sp: WeakReference<ScreenProj>? = null
        internal lateinit var pi: Intent

        private const val IS_RECORDING = "is_recording"

        internal var isRecording: Boolean
            set(value) {
                PreferenceManager.getDefaultSharedPreferences(app.get())
                        .edit()
                        .putBoolean(IS_RECORDING, value)
                        .apply()
            }
            get() = PreferenceManager.getDefaultSharedPreferences(app.get()).getBoolean(IS_RECORDING, false)

        internal var hasSaved: Boolean
            set(value) {
                PreferenceManager.getDefaultSharedPreferences(app.get())
                        .edit()
                        .putBoolean("has_saved", value)
                        .apply()
            }
            get() = PreferenceManager.getDefaultSharedPreferences(app.get()).getBoolean("has_saved", false)
    }

    override fun onCreate() {
//        Thread.setDefaultUncaughtExceptionHandler { t, e ->
//            Log.e("ScreenRecorder", "${t.name} ${e.message}")
//            Toast.makeText(this@App, R.string.error, Toast.LENGTH_LONG).show()
//            exitProcess(1)
//        }
        super.onCreate()
        app = WeakReference(this)
    }
}
