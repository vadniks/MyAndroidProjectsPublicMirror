/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager

/**
 * @author Vad Nik.
 * @version dated August 31, 2018.
 * @link http://github.com/vadniks
 */
class NotifService : IntentService("NotifService") {
    private lateinit var lbr: BroadcastReceiver

    companion object {
        private var isRunning = false

        internal fun isRunning() = isRunning
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId)

        val p = createNotif()

        when (intent.action) {
            START_RECORD -> {
                App.isRecording = true
//                for (i in 0..5000) {
//                    println(i)
//                }
                if (App.sp!!.get()!!.isRecording())
                    return super.onStartCommand(intent, flags, startId)

                App.sp!!.get()!!.startRecording()
                startForeground(p.first, p.second)
                isRunning = true
            }
            STOP_RECORD -> {
                if (App.sp!!.get()!!.isRecording())
                    App.sp!!.get()!!.stopRecording()
                stopForeground(true)
                stopSelf()
            }
        }

        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter()
        filter.addAction(STOP_RECORD)
        filter.addAction(START_ACT)
        filter.addAction(END_NOTIF)

        lbr = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || context == null)
                    return

                //Toast.makeText(this@NotifService, intent.action, Toast.LENGTH_LONG).show()

                println(intent.action)

                when (intent.action) {
                    END_NOTIF -> {
                        stopForeground(true)
                        stopSelf()
                    }
//                    STOP_RECORD -> {
//                        if (sp.isRecording())
//                            sp.stopRecording()
//                        stopForeground(true)
//                        stopSelf()
//                    }
//                    START_ACT -> startService(Intent(context, MainActivity::class.java))
                }
            }
        }

        LocalBroadcastManager.getInstance(this@NotifService.applicationContext).registerReceiver(lbr, filter)
    }

    /**
     * @return Pair (two values) of created notification's id and it self.
     */
    @Suppress("deprecation")
    private fun createNotif(): Pair<Int, Notification> {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            nm.createNotificationChannel(NotificationChannel(CHID, CHID, NotificationManager.IMPORTANCE_LOW))

        val openApp = Intent()
        openApp.action = START_ACT
        openApp.setClass(this, BR::class.java)

        val nid = System.currentTimeMillis().toInt()

        val stopRec = Intent()
        stopRec.action = STOP_RECORD
        stopRec.setClass(this, BR::class.java)
        stopRec.putExtra(EXTRA_NID, nid)

        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationCompat.Builder(this, CHID)
        else
            NotificationCompat.Builder(this)

        b.setAutoCancel(false)
        b.setDefaults(Notification.DEFAULT_ALL)
        b.setSmallIcon(R.drawable.srn_rec_light)
        b.setCategory(Notification.CATEGORY_SERVICE)
        b.setVisibility(Notification.VISIBILITY_PUBLIC)
        b.setContentTitle(getString(R.string.srn_rec))
        b.setContentText(getString(R.string.srn_progress))
        b.setContentIntent(PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), openApp, 0))
        b.setOngoing(true)
        b.addAction(0, getString(R.string.stop_recording), PendingIntent.getBroadcast(this, nid, stopRec, 0))

        return Pair(nid, b.build())
    }

    override fun onHandleIntent(intent: Intent?) {}

    override fun onDestroy() {
        if (App.sp!!.get()!!.isRecording())
            App.sp!!.get()!!.stopRecording()
        //stopForeground(true)
        LocalBroadcastManager.getInstance(this.applicationContext).unregisterReceiver(lbr)
        isRunning = false
        super.onDestroy()
    }
}
