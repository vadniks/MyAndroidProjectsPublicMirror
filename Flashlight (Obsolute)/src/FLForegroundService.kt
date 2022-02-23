package 

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat

/**
 * @author Vad Nik.
 * @version dated July 1, 2018.
 * @link github.com/vadniks
 */
public final class FLForegroundService public constructor(): IntentService(FLForegroundService::javaClass.name) {

    internal companion object {
        @JvmStatic
        private var isRunning: Boolean = false

        @JvmStatic
        internal fun isRunning(): Boolean = isRunning
    }

    override fun onHandleIntent(intent: Intent?) {}

    @Suppress("deprecation")
    private fun startNotifLight() {
        val nm: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel("flanch", "Flashlight app nch",
                    NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(nc)
        }

        val id: Int = System.currentTimeMillis().toInt()

        sendBroadcast(Intent(this, BR::class.java)
                .setClass(this, BR::class.java)
                .setAction("STORE_VAL_FL")
                .putExtra("stKeyFL", "notifLightId")
                .putExtra("stValFL", id.toString()))

//        val intent = Intent(context, MainActivity::class.java)
//        intent.action = "TURN_THE_FLASHLIGHT_OFF"
//        intent.setClass(context, BR::class.java)
//        intent.putExtra("notifId", id)
//
//        val pen: PendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)

        val builder: NotificationCompat.Builder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    NotificationCompat.Builder(this, "flanch")
                else
                    NotificationCompat.Builder(this)

        builder
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.flashlight2)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.notifTitle))
                .setContentText(getString(R.string.notifText))
                .setContentIntent(PendingIntent.getBroadcast(this, id+1,
                        Intent(this, BR::class.java)
                                .setAction("START_ACTIVITY_FL")
                                .setClass(this, BR::class.java)
                        , 0))
                //.addAction(0, context.getString(R.string.notifAction), pen)
                .setOngoing(true)

        startForeground(id, builder.build())
    }

    override fun onDestroy() {

        println("destroying fl service")

        stopForeground(true)
        isRunning = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId)

        println(intent.action)

        when (intent.action) {
            "START_FOREGROUND_FL" -> startNotifLight()
            "STOP_FOREGROUND_FL" -> {
                stopForeground(true)
                stopSelf()
            }
        }

        isRunning = true
        return Service.START_STICKY
    }
}
