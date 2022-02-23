package 

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

import kotlinx.android.synthetic.main.activity_light.*

/**
 * @author Vad Nik.
 * @version dated June 26, 2018.
 * @link github.com/vadniks
 */
public final class LightActivity : Activity() {
    private var exBr: Int = 1

    internal companion object {
        @JvmStatic
        private var isLaunched: Boolean = false

        @JvmStatic
        internal fun isLaunched(): Boolean = isLaunched
    }

    private fun initAds() {
        MobileAds.initialize(this, adAppKey)

        adViewLight.loadAd(AdRequest.Builder().build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light)

        exBr = getBrightness()

        setBrightness(255)

        initAds()

        isLaunched = true
    }

    override fun onStop() {
        setBrightness(exBr)
        isLaunched = false
        super.onStop()
    }

    override fun onBackPressed() {
        if (MainActivity.isRunning())
            super.onBackPressed()
        else {
            startActivity(Intent(this, MainActivity::class.java))
            super.onBackPressed()
        }
    }

    override fun onRestart() {
        setBrightness(255)
        isLaunched = true
        super.onRestart()
    }

    private fun setBrightness(b: Int) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, b)
    }

    private fun getBrightness(): Int = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
}
