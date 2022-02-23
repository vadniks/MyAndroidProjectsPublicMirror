/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_record.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

import java.lang.ref.WeakReference
import kotlin.system.exitProcess

/**
 * @author Vad Nik.
 * @version dated August 31, 2018.
 * @link http://github.com/vadniks
 */
class RecordActivity : AppCompatActivity() {
    //@Transient
    private var isnFirst = false
    private lateinit var r: Button
    private lateinit var sp: ScreenProj

    private var hasGrantedRW
        set(value) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean("hasGrantedRW", value)
                    .apply()
        }
        get() = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hasGrantedRW", false)

    companion object {
        private var isRunning = false

        internal fun isRunning() = isRunning
    }

    init {
        println("testo constr")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        if (App.isRecording)
            finishAndRemoveTask()

        //TODO: replace with kotlinx.synthetic

//        launch(CommonPool) {
//            Handler(Looper.getMainLooper()).post {
//                MobileAds.initialize(this@RecordActivity, "")
//                adView.loadAd(AdRequest.Builder().build())
//            }
//        }

//        launch(CommonPool) {
//            try {
//                //Toast.makeText(this@RecordActivity, R.string.rootTip, Toast.LENGTH_LONG).show()
//                if (isRooted() && !hasDirChanged()) {
//
//                    println("testo ra ")
//
//                    changeDir()
//                    selfRestart()
//                }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//            }
//        }

        val c = findViewById<CheckBox>(R.id.ch)
        r = findViewById(R.id.rec)
        val e1 = findViewById<EditText>(R.id.ed_t)
        //val e2 = findViewById<EditText>(R.id.ed_st)

//        App.sp = WeakReference(ScreenProj(this, applicationContext))
//
//        if (App.pi == null)
//            App.sp!!.get()!!.requestRecording()

        var title = ""
        var desc = ""
        var recordAudio = c.isChecked

        if (intent.getBooleanExtra(EXTRA_IS_REC, false)) {
            r.text = getString(R.string.stop_recording)
            c.isEnabled = false
        }

        c.setOnCheckedChangeListener { _, isChecked -> recordAudio = isChecked }

        r.setOnClickListener {
            try {
                App.sp = WeakReference(sp)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            println("testo 002 ${App.sp!!.get() == null}")

            if (!App.sp!!.get()!!.isRecording()) {
                r.text = getString(R.string.stop_recording)
                //sp.startRecording()

                title = e1.text.toString()
                //desc = e2.text.toString()

                if (title != "")
                    App.sp!!.get()!!.title = title

//                if (desc != "")
//                    App.sp!!.get()!!.description = desc

                App.sp!!.get()!!.recordAudio = recordAudio

                App.sp!!.get()!!.initRecorder()

                App.sp!!.get()!!.startService()
                finishAndRemoveTask()
            } else {
                r.text = getString(R.string.start_recording)
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().setAction(END_NOTIF))
                //sp.stopRecording()
                c.isEnabled = true
            }
        }

        val br = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null)
                    return

                when (intent.action) {
                    UPDATE_BT -> {
                        if (intent.getBooleanExtra(EXTRA_UBT, false)) {
                            r.text = getString(R.string.start_recording)
                            c.isEnabled = true
                            recreate()
                        } else  {
                            r.text = getString(R.string.stop_recording)
                            c.isEnabled = false
                        }
                    }
                    REQUEST_PERM -> {
                        isnFirst = true
                        //recreate()
                        //App.sp!!.get()!!.requestRecording() //
                        //App.sp!!.get()!!.startRecording()
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(UPDATE_BT)
        filter.addAction(REQUEST_PERM)

        LocalBroadcastManager.getInstance(this.applicationContext).registerReceiver(br, filter)

        isRunning = true

//        if (intent.getBooleanExtra(EXTRA_IS_R_P, false)) {
//            //recreate()
//            //isnFirst = true
//            App.sp!!.get()!!.requestRecording()
//            //App.sp!!.get()!!.startRecording()
//        }

        App.sp = WeakReference(ScreenProj(this, applicationContext))

        if (App.hasSaved) {
            MobileAds.initialize(this, "")
            adView.loadAd(AdRequest.Builder().build())
        }

        println("testo 001 ${App.sp!!.get() == null}")

        if (!App.hasSaved) {
            App.sp!!.get()!!.requestRecording()
            return
        } else
            App.sp!!.get()!!.onActivityResult(Activity.RESULT_OK, App.pi)

        sp = App.sp!!.get()!!

        App.hasSaved = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        println("testo $requestCode $resultCode ${data == null}")

        if (requestCode == 1) {

            if (resultCode != Activity.RESULT_OK) {
                if (App.sp!!.get()!!.isRecording())
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent().setAction(END_NOTIF))

                Toast.makeText(this, R.string.permDen, Toast.LENGTH_LONG).show()
                finishAndRemoveTask()
                return
            }

            if (data == null)
                return

            App.pi = data.clone() as Intent
            App.hasSaved = true

            println("testo n ${App.sp == null} ${if (App.sp != null) (App.sp?.get() == null).toString() else "b"}")

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasGrantedRW) {

                println("testo sh")

                selfRestart()
                return
            }

            ///App.sp!!.get()!!.onActivityResult(resultCode, data)
        }

//        super.onActivityResult(requestCode, resultCode, data)

        requestPermissions()

//        launch(CommonPool) {
//            if (isRooted())
//                requestCapturing()
//        }

//        if (isnFirst) {
//            isnFirst = false
//            App.sp!!.get()!!.startRecording()
//        }
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1055) {
            var b = false
            for (i in 0 until 3)
                b = grantResults[i] == PackageManager.PERMISSION_GRANTED

            if (!b)
                exitProcess(-1)

            selfRestart()
        } /*else if (requestCode == 2055)
            println("testo capt ${grantResults[0] == PackageManager.PERMISSION_GRANTED}")*/
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return

        if (hasReadGranted() && hasWriteGranted() && hasAudioGranted())
            return

        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO), 1055)

        hasGrantedRW = true
    }

    private fun hasReadGranted(): Boolean =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED

    private fun hasWriteGranted(): Boolean =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED

    private fun hasAudioGranted(): Boolean =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED

    private fun isRooted(): Boolean {
        val p = Runtime.getRuntime().exec("su")
        val o: DataOutputStream? = DataOutputStream(p.outputStream)
        val i: DataInputStream? = DataInputStream(p.inputStream)

        if (o == null || i == null)
            return false

        o.writeBytes("id\n")
        o.flush()

        val s: String? = i.readBytes().toString()
        var b = false
        if (s != null && s.contains("uid-0"))
            b = true

        o.writeBytes("exit\n")
        o.flush()
        o.close()

        println("test r $b")

        return b
    }

    private fun changeDir() {
        val p = Runtime.getRuntime().exec("su")
        val os: DataOutputStream? = DataOutputStream(p.outputStream)
        val i: DataInputStream? = DataInputStream(p.inputStream)

        if (os == null || i == null) {
            setDirChanged(true)
            return
        }

        os.writeBytes("mount -o r,remount -t yaffs2 /dev/block/mtdblock3 /system\n")
        os.flush()
        os.writeBytes("mkdir /system/priv-app/screenrecorder")
        os.flush()
        os.writeBytes("cp -a /data/app// /system/priv-app/screenrecorder\n")
        os.flush()
        os.writeBytes("mkdir /system/priv-app/screenrecorder/a")
        os.flush()

        println("testo rr ${i.readBytes()}")

        os.writeBytes("exit\n")
        os.flush()
        os.close()
        setDirChanged(true)
    }

    private fun selfRestart() {
        finish()
        startActivity(packageManager
                .getLaunchIntentForPackage(packageName)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun setDirChanged(changed: Boolean) {
        val sh = PreferenceManager.getDefaultSharedPreferences(this)
        sh.edit().putBoolean("isDirChanged", changed).apply()
    }

    private fun isDirChanged(): Boolean = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("isDirChanged", false)

    private fun hasDirChanged(): Boolean = File("/system/priv-app/screenrecorder/a").exists()

    private fun requestCapturing() {
        println("testo te ${ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT)}")

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAPTURE_AUDIO_OUTPUT), 2055)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_record, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        if (item == null)
//            return super.onOptionsItemSelected(item)
//
//        when (item.itemId) {
//            R.id.share -> {
//
//                println("testo ${App.sp == null}")
//
//                //println(!App.sp?.get()?.hasRecorded()!!)
//
//                println(" testo file:/${App.sp!!.get()!!.lastRecorded()}")
//
//                if (App.sp?.get()?.hasRecorded()!!) {
//                    val i = Intent(Intent.ACTION_SEND).apply {
//                        type = "video/mp4"
//                        putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/${App.sp!!.get()!!.lastRecorded()}"))
//                    }
//                    startActivity(Intent.createChooser(i, getString(R.string.chApp)))
//                }
//            }
//        }
//        return true
//    }
}
