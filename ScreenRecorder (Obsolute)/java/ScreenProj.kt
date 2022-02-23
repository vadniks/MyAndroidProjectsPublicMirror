/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.support.v4.content.LocalBroadcastManager
import android.util.DisplayMetrics
import android.widget.Toast

import java.io.File
import java.lang.ref.WeakReference

/**
 * @author Vad Nik.
 * @version dated August 31, 2018.
 * @link http://github.com/vadniks
 *
 * Class processes the screen and audio recording operations.
 *
 * TODO: rename to ScreenProcessing or SP or etc.
 *
 * Constructor's parameters:
 * @param actCont Activity context to request recording permission using the [Activity.onActivityResult] method.
 * @param appCont Application context for other context-needed operations.
 */
internal class ScreenProj(private val actCont: Context, private val appCont: Context) {
    private var manager: MediaProjectionManager? = null
    private var display: VirtualDisplay? = null
    private var recorder: MediaRecorder? = null
    private val d: DisplayMetrics = getMetrix()

    internal var title = "def"
    internal var description = "none"

    /**
     * Internal (package-private) getter and setter of the parameter [recordAudio].
     * This parameter is read by the [recorder], that enables or disables recording audio.
     *
     * Might be not set.
     */
    internal var recordAudio = true

    private var path = "def"

    private var lastRecorded = "none"

    private var millis = 0
        set(value) {
            if (value < 0)
                field = -value
        }

    companion object {
        private var projection: MediaProjection? = null
    }

//    private var w = 0
//    private var h = 0
//    private var dp = 0

    private var isRecording = false
    private var hasRecorded = false

    /**
     * Constructor, must be instantiated in Activity.
     */
    init {
        //initRecorder()

        manager = actCont.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

//        surface = sv.holder.surface
//        sv.holder.addCallback(SCallBack())

        //muxe = MediaMuxer(DIR + "/SRecording_${System.currentTimeMillis()}", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

//        w = d.widthPixels
//        h = d.heightPixels
//        dp = d.densityDpi
    }

    /**
     * This method must be called right after class instantiation and before calling the [startRecording] method.
     */
    internal fun requestRecording() {
        println("testo rp")
        //App.sp = WeakReference(this)
        //if (projection == null) {
            (actCont as Activity).startActivityForResult(manager!!.createScreenCaptureIntent(), 1)
//            return
//        } else {
//            (actCont as Activity).startActivityForResult(manager!!.createScreenCaptureIntent(), 1)
//            //startRecording()
//        }
    }

    internal fun stopProj() {

    }

    /**
     * @return path of last recorded *.mp4 file.
     */
    internal fun lastRecorded() = lastRecorded

    /**
     * Starts the defined service that performs all screen-recording operations.
     */
    internal fun startService() {
        appCont.startService(Intent(actCont, NotifService::class.java)
                .setAction(START_RECORD))
    }

    /**
     * This method must be called in Activity's onActivityResult method.
     */
    internal fun onActivityResult(resultCode: Int, data: Intent) {
        projection = manager!!.getMediaProjection(resultCode, data)
        projection?.registerCallback(Callback(), null)

        println("111 ${projection == null}")

//        display = createVD()
//        recorder?.start()
    }

    private fun getMetrix(): DisplayMetrics {
        val d = DisplayMetrics()
        (actCont as Activity).windowManager.defaultDisplay.getMetrics(d)
        return d
    }

    private fun createVD(): VirtualDisplay =
            projection!!.createVirtualDisplay(V_D_NAME,
                    d.widthPixels,
                    d.heightPixels,
                    d.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    recorder?.surface, null, null)

    /**
     * This method must be called after class initialization and calling the [requestRecording] method,
     * and before any operations with recording i.e. calling the [startRecording] method.
     *
     * It's desirable to call this method right before calling the [startRecording] method.
     */
    internal fun initRecorder() {
//        if (recorder != null)
//            return

        val dir = File(DIR)
        if (!dir.exists())
            dir.mkdir()

        println(" testo $DIR ${dir.exists()} ${dir.absolutePath} ${dir.canonicalPath} ${Uri.parse(dir.toURI().toString())}")

        recorder = MediaRecorder()

        if (recordAudio)
            recorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)

        recorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)

        if (recordAudio)
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        recorder?.setVideoEncodingBitRate(3000000) //3000000
        recorder?.setVideoFrameRate(30)
        recorder?.setVideoSize(d.widthPixels, d.heightPixels)

        recorder?.setOnInfoListener { _, what, _ ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                Toast.makeText(appCont, R.string.info1, Toast.LENGTH_LONG).show()
                stopRecording()
            } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING) {
                Toast.makeText(appCont, R.string.info2, Toast.LENGTH_LONG).show()
                stopRecording()
            }
        }

        recorder?.setOnErrorListener { _, _, _ -> Toast.makeText(appCont, R.string.error, Toast.LENGTH_LONG).show() }

        millis = System.currentTimeMillis().toInt()

        path = if (title == "def") {
            val pa = "$DIR/SRecording_$millis.mp4"
            recorder?.setOutputFile(pa)
            pa
        } else {
            val pa = "$DIR/$title.mp4"
            recorder?.setOutputFile(pa)
            pa
        }
        recorder?.prepare()
    }

    //TODO: add viewing already recorded recordings and viewing their metadata.

    private fun writeMetadata() {
        TODO("not implemented")
    }

    internal fun isRecording() = isRecording

    /**
     * @return true if recording has been recorded and if a new recording hasn't been started yet, false otherwise.
     */
    internal fun hasRecorded() = hasRecorded && !isRecording()

    /**
     * If the [startService] method was called, this method must be called in the service, defined in the [startService]
     */
    internal fun startRecording() {
        isRecording = true

//        if (hasRecorded()) {
//            lastRecorded = if (title == "def") {
//                val pa = "$DIR/SRecording_$millis.mp4"
//                recorder?.setOutputFile(pa)
//                pa
//            } else {
//                val pa = "$DIR/$title.mp4"
//                recorder?.setOutputFile(pa)
//                pa
//            }
//
//            //println(lastRecorded)
//            initRecorder()
//        }

//        Toast.makeText(actCont, "${projection == null} ${manager == null}", Toast.LENGTH_LONG).show()
//        return

//        if (projection == null) {
//            (actCont as Activity).startActivityForResult(manager!!.createScreenCaptureIntent(), 1)
//            return
//        }
        //println(recordAudio)

        hasRecorded = false

        try {
            display = createVD()
        } catch (ex: SecurityException) {
            ex.printStackTrace()

            onActivityResult(Activity.RESULT_OK, App.pi!!)
            startRecording()

//            if (RecordActivity.isRunning())
//                LocalBroadcastManager.getInstance(appCont).sendBroadcast(Intent().setAction(REQUEST_PERM))
//            else
//                appCont.startActivity(Intent(appCont, RecordActivity::class.java).putExtra(EXTRA_IS_R_P, true))
            return
        }

        recorder?.start()
    }

    //TODO: add pause and resume features.

    /**
     * If the [startService] method was called, this method must be called in the service, defined by the [startService]
     */
    internal fun stopRecording() {
        //Toast.makeText(actCont, "111", Toast.LENGTH_SHORT).show()
        //Log.d("testo", "111")

        if (display == null)
            return

        isRecording = false

        //Log.d("testo", "---")

        recorder?.stop()
        recorder?.reset()
        recorder?.release()

        projection?.stop()
        //projection = null

        display?.release()
        display = null

        Toast.makeText(appCont, appCont.getString(R.string.saved) + "$DIR/SRecording_${System.currentTimeMillis()}.mp4",
                Toast.LENGTH_LONG).show()

        lastRecorded = if (title == "def") {
            val pa = "$DIR/SRecording_$millis.mp4"
            recorder?.setOutputFile(pa)
            pa
        } else {
            val pa = "$DIR/$title.mp4"
            recorder?.setOutputFile(pa)
            pa
        }

        println("testo file ${File("$DIR/SRecording_$millis.mp4").exists()}")

        hasRecorded = true
    }

    //TODO: add record system audio feature and only audio record feature.

    @Deprecated("might be unused")
    private class Callback : MediaProjection.Callback() {

        override fun onStop() {
            //Log.d("testo", "222")
            //Toast.makeText(actCont, "222", Toast.LENGTH_SHORT).show()
            //stopRecording()
            //projection = null
            //super.onStop()
        }
    }

//    @Deprecated("unused")
//    private inner class SCallBack : SurfaceHolder.Callback, Serializable {
//
//        companion object {
//            private val serialVersionUID: Long = ObjectStreamClass.lookup(ScreenProj::class.java).serialVersionUID //5832063776451490808L
//        }
//
//        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
//
//        override fun surfaceDestroyed(holder: SurfaceHolder?) {
//            if (!isRecording)
//                stopRecording()
//        }
//
//        override fun surfaceCreated(holder: SurfaceHolder?) {
//            //surface = holder?.surface
////            if (isRecording && RecordActivity.isRunning())
////                startRecording()
//        }
//    }
}
