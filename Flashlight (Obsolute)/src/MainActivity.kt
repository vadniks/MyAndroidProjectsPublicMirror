package 

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.preference.PreferenceManager
import android.support.annotation.UiThread
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Vad Nik.
 * @version dated June 26, 2018.
 * @link github.com/vadniks
 */

internal const val adAppKey: String = ""

//TODO: change axaple widget image to real.

public final class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btSwitch: ToggleButton
    private lateinit var btScreen: Button
    private lateinit var sh: SharedPreferences
    private var isFromService: Boolean = false

    internal companion object {
        @JvmStatic
        private var isRunning: Boolean = false

        @JvmStatic
        internal fun isRunning(): Boolean = isRunning

        @SuppressLint("StaticFieldLeak")
        private lateinit var batterySt: TextView

        @SuppressLint("SetTextI18n")
        @UiThread
        internal fun updateBatteryStatus(level: Int) {
            batterySt.text = "$level%"
        }
    }

    private fun initAds() {
        MobileAds.initialize(this, adAppKey)

        adViewMain.loadAd(AdRequest.Builder().build())
    }

    @Suppress("deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sh = PreferenceManager.getDefaultSharedPreferences(this)!!

        btSwitch = findViewById(R.id.lightSwith)
        btSwitch.isChecked = FLForegroundService.isRunning()
        btSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                if (intent != null) {
                    if (intent.getBooleanExtra("isFromNotif", false)) {
                        sendBroadcast(Intent(this, BR::class.java)
                                .setClass(this, BR::class.java)
                                .setAction("STOP_LIGHT"))
                        isFromService = true
                    }
                }
            }

            //There's no ternary operator in kotlin.
            this broadcast if (isChecked) "START_LIGHT" else "STOP_LIGHT"
        }

        btSwitch.isEnabled = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        btScreen = findViewById(R.id.lightScreen)
        btScreen.setOnClickListener(this)

        initBaterrySt()

        print("PA? ")
        println(hasBeenPANotified())

        if (!hasBeenPANotified() && !isChecked()) this broadcast "START_NOTIFY_P_A_FL"

        initAds()

        isRunning = true
    }

    @SuppressLint("SetTextI18n")
    private fun initBaterrySt() {
        val bm: BatteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        batterySt.text = "Battery status: ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)}%" +

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (bm.isCharging) " charging" else " discharging"
                else ""
    }

    //TODO: replace notification actions with layout buttons in the RCCM.
    //TODO: make notification turns off the light by tap.
    //TODO: make the RCCM to be optional.

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    private infix fun broadcast(what: String) {
        sendBroadcast(Intent(this, BR::class.java)
                .setClass(this, BR::class.java)
                .setAction(what))
    }

    //TODO: add battery level to MA.
    //TODO: test after reboot notif.
    //TODO: make something like siren (red changed to blue).

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.lightScreen -> startActivity(Intent(this, LightActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.dontShowRCCM)?.isChecked = isChecked()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null)
            return super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.showRCCM -> {
                if (!hasBeenPANotified() && !isChecked())
                    this broadcast "START_NOTIFY_P_A_FL"
                else
                    Toast.makeText(this, R.string.RCCMTip3, Toast.LENGTH_LONG).show()
            }
            R.id.hideRCCM -> this broadcast "STOP_NOTIFY_P_A_FL"
            R.id.dontShowRCCM -> {
                if (hasBeenPANotified()) this broadcast "STOP_NOTIFY_P_A_FL"
                if (!hasBeenPANotified() && item.isChecked) this broadcast "START_NOTIFY_P_A_FL"

                item.isChecked = !item.isChecked
                storeCheckedState(item.isChecked)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun storeCheckedState(state: Boolean) {
        val ed: SharedPreferences.Editor = sh.edit()
        ed.putBoolean("showRCCMFL", state)
        ed.apply()
    }

    private fun isChecked(): Boolean = sh.getBoolean("showRCCMFL", false)

    //private fun hasBeenNotified(): Boolean = sh.getInt("flashlight_app_notif_id2_pref", -1) != -1

    private fun hasBeenPANotified(): Boolean = sh.getInt("flashlight_app_notif_id2_pref", 0) != 0
}
