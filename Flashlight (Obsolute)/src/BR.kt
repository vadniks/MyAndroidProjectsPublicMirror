package 

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.widget.Toast

public final class BR public constructor(): BroadcastReceiver() {
    private lateinit var sh: SharedPreferences
    @Suppress("deprecation")

    private companion object {
//        @JvmStatic
//        private var canInstantiate: Boolean = true
        @JvmStatic
        private var cam: Camera? = null
    }

    //TODO: implement the RCCM.

    override fun onReceive(context: Context, intent: Intent) {
        sh = PreferenceManager.getDefaultSharedPreferences(context)!!

        if (intent == null)
            return

        //println(intent.action)

        when (intent.action) {
            "START_ACTIVITY_FL" -> {
                val i = Intent(context, MainActivity::class.java)
                i.putExtra("isFromNotif", true)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                context.startActivity(i)
            }
            "STOP_LIGHT" -> {
                light(false, context)
                stopNotifLight(context)
            }
            "START_LIGHT" -> {
                light(true, context)
                startNotifLight(context)
            }
            "STOP_NOTIF_FL" -> stopNotifLight(context)
            Intent.ACTION_BOOT_COMPLETED -> {
                //TODO: check is PA has started
                if (sh == null)
                    sh = PreferenceManager.getDefaultSharedPreferences(context)

                if (sh.getInt("flashlight_app_notif_id2_pref", 0) > 0) startPA(context)
            }
            "START_NOTIFY_P_A_FL" -> startPA(context)
            "STOP_NOTIFY_P_A_FL" -> stopPA(context)
            //TODO: BroadcastReceiver can't return result, so remove this.
            "IS_FLASHLIGHT_ON_FL" -> FLForegroundService.isRunning()
            "STORE_VAL_FL" -> storeValue(intent.getStringExtra("stKeyFL"), intent.getStringExtra("stValFL"))
            "GET_VAL_FL" -> getValue(intent.getStringExtra("stKeyFL"), intent.getStringExtra("stDefValFL"))
            "FL_ON/OFF" -> {
                //TODO: check is the flashlight feature available on device, do return if it is, proceed otherwise.

                if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(context, R.string.noFetch, Toast.LENGTH_LONG).show()
                    return
                }

                print("fl ")
                println(FLForegroundService.isRunning())

                light(!FLForegroundService.isRunning(), context)
                if (FLForegroundService.isRunning()) stopNotifLight(context) else startNotifLight(context)
            }
            "LS_ON/OFF" -> {

                print("ls ")
                println(LightActivity.isLaunched())

                if (!LightActivity.isLaunched()) //TODO: test flag in Oreo.
                    context.startActivity(Intent(context, LightActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                //TODO: add battery level to the RCCM.

                if (MainActivity.isRunning()) {
                    Handler(Looper.getMainLooper()).post {
                        MainActivity.updateBatteryStatus(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0))
                    }
                }
            }
        }
    }

    @Suppress("unused")
    //@Suppress("deprecation")
    private fun isServiceRunning(context: Context?): Boolean {
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (o: ActivityManager.RunningServiceInfo in am.getRunningServices(Int.MAX_VALUE)) {
                if (FLForegroundService::javaClass.name == o.service.className)
                    return true
            }
        } else return*/ FLForegroundService.isRunning()

        return false
    }

    private fun storeValue(key: String, value: String) {
        val ed: SharedPreferences.Editor = sh.edit()
        ed.putString(key, value)
        ed.apply()
    }

    private fun getValue(key: String, defVal: String): String = sh.getString(key, defVal)

    @Deprecated("no replacement for deprecated content")
    private fun isFLOn(context: Context): Boolean {
        //android.hardware.camera2.CameraManager.TorchCallback

        if (cam == null/* && canInstantiate*/) {
            cam =
                    try {
                        Camera.open()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cam?.release()
                        Camera.open()
                    }
        }

        val params: Camera.Parameters = cam?.parameters!!
        return params.flashMode == Camera.Parameters.FLASH_MODE_TORCH
    }

    @Suppress("deprecation")
    private fun startPA(context: Context) {

        println("starting PA...")

        val nm: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel("flanch", "Flashlight app nch",
                    NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(nc)
        }

        val id: Int = System.currentTimeMillis().toInt() //TODO: make 'id' to be constant.

        setNotifId2(id)

        val intent = Intent(context, BR::class.java)
        intent.action = "FL_ON/OFF"
        intent.setClass(context, BR::class.java)
        intent.putExtra("notifId", id)

        val intent2 = Intent(context, BR::class.java)
        intent2.action = "LS_ON/OFF"
        intent2.setClass(context, BR::class.java)
        intent2.putExtra("notifId", id)

        println(id)
        println(id.plus(10))

        val pen: PendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)
        val pen2: PendingIntent = PendingIntent.getBroadcast(context, id.plus(10), intent2, 0)

        val builder: NotificationCompat.Builder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    NotificationCompat.Builder(context, "flanch")
                else
                    NotificationCompat.Builder(context)

        builder
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.flashlight2)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(context.getString(R.string.FLControls))
                .setContentIntent(PendingIntent.getBroadcast(context, id+1,
                        Intent(context, BR::class.java)
                                .setAction("START_ACTIVITY_FL")
                                .setClass(context, BR::class.java)
                        , 0))
                .addAction(0, context.getString(R.string.notifAction), pen)
                .addAction(0, context.getString(R.string.notifAction2), pen2)
                .setOngoing(true)

        nm.notify("flashlight_notif_pa", id, builder.build())
    }

    private fun stopPA(context: Context) {
        if (sh == null)
            sh = PreferenceManager.getDefaultSharedPreferences(context)

        val nm: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)!! as NotificationManager
        nm.cancel("flashlight_notif_pa", getNotifId2())
        setNotifId2(0)
    }

    private fun setNotifId2(id: Int) {
        val ed: SharedPreferences.Editor = sh.edit()
        ed.putInt("flashlight_app_notif_id2_pref", id)
        ed.apply()
    }

    @Suppress("deprecation")
    private fun startNotifLight(context: Context) {
        context.startService(Intent(context, FLForegroundService::class.java)
                .setAction("START_FOREGROUND_FL")
                .setClass(context, FLForegroundService::class.java))
    }

    private fun stopNotifLight(context: Context) {
        context.stopService(Intent(context, FLForegroundService::class.java)
                .setAction("STOP_FOREGROUND_FL")
                .setClass(context, FLForegroundService::class.java))
    }

    private fun getNotifId2(): Int = sh.getInt("flashlight_app_notif_id2_pref", 0)

    @Suppress("deprecation")
    private fun light(b: Boolean, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], b)
        } else {
            if (cam == null/* && canInstantiate*/) {
                cam =
                        try {
                            Camera.open()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            cam?.release()
                            Camera.open()
                        }
            }

            val params: Camera.Parameters = cam?.parameters!!

            if (b) {
                params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                cam?.parameters = params
                cam?.startPreview()
                //canInstantiate = false
            } else {
                cam?.stopPreview()
                cam?.release()
                cam = null
                //canInstantiate = true
            }
        }
    }
}
