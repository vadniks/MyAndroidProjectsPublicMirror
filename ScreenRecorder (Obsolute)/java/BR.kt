/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager

/**
 * @author Vad Nik.
 * @version dated September 1, 2018.
 * @link http://github.com/vadniks
 */
class BR : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null)
            return

        when (intent.action) {
            STOP_RECORD -> {
                App.isRecording = false

                if (App.sp!!.get()!!.isRecording())
                    App.sp!!.get()!!.stopRecording()
                LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(Intent()
                        .setAction(END_NOTIF)
                        .putExtra(EXTRA_NID, intent.getIntExtra(EXTRA_NID, 0)))
                LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(Intent()
                        .setAction(UPDATE_BT)
                        .putExtra(EXTRA_UBT, true))
                //sendNotif(context)
            }
            START_ACT -> {
//                context.startActivity(Intent(context, RecordActivity::class.java)
//                        .putExtra(EXTRA_IS_REC, true))
            }
            SHARE -> {
                val s = App.sp?.get()?.lastRecorded() ?: return

                //context.startActivity(Intent.createChooser(Intent()))
            }
        }
    }

    @Suppress("deprecation")
    private fun sendNotif(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            nm.createNotificationChannel(NotificationChannel(CHID_2, CHID_2, NotificationManager.IMPORTANCE_LOW))

        val openApp = Intent()
        openApp.action = SHARE
        openApp.setClass(context, BR::class.java)

        val nid = System.currentTimeMillis().toInt()

//        val share = Intent()
//        share.action = STOP_RECORD
//        share.setClass(context, BR::class.java)
//        share.putExtra(EXTRA_NID, nid)

        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationCompat.Builder(context, CHID_2)
        else
            NotificationCompat.Builder(context)

        b.setAutoCancel(true)
        b.setDefaults(Notification.DEFAULT_ALL)
        b.setSmallIcon(R.drawable.srn_rec_light)
        b.setCategory(Notification.CATEGORY_MESSAGE)
        b.setVisibility(Notification.VISIBILITY_PUBLIC)
        b.setContentTitle(context.getString(R.string.share))
        //b.setContentText(context.getString(R.string.srn_progress))
        b.setContentIntent(PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), openApp, 0))
        b.setOngoing(false)
        //b.addAction(0, context.getString(R.string.stop_recording), PendingIntent.getBroadcast(context, nid, stopRec, 0))

        nm.notify(nid, b.build())
    }
}
