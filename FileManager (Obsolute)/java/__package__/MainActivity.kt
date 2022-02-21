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
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.preference.PreferenceManager
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

/**
 * @author Vad Nik.
 * @version dated Dec 11, 2018.
 * @link http://github.com
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainView {
    private lateinit var proc: Processing
    private lateinit var menu: Menu
    @Suppress("deprecation")
    private lateinit var pd: ProgressDialog
    private var backCounter = 0

    //TODO: ad 'go to path' feature.

    private var useRoot: Boolean
        set(value) {
            println("testo su start")

            var b = false
            if (value) {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                val output: String? = BufferedReader(InputStreamReader(p.inputStream)).readLine()

                b = output != null && output.toLowerCase().contains("uid=0")

                p.destroy()
            }

            //TODO: wait for user action when requesting permissions.

            println("testo su $b")

            doPost {
                menu.findItem(R.id.use_su).isChecked = b
            }

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(ENABLE_ROOT, if (value) b else false)
                .apply()
        }
        get() {
            val p = Runtime.getRuntime().exec("id")
            val output: String? = BufferedReader(InputStreamReader(p.inputStream)).readLine()

            val b = output != null && output.toLowerCase().contains("uid=0")

            p.destroy()

            //println("testo su get $b")

            return /*b &&*/ PreferenceManager.getDefaultSharedPreferences(this).getBoolean(ENABLE_ROOT, false)
        }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1233
        const val ENABLE_ROOT = "ENABLE_ROOT"

        init {
            System.loadLibrary("native-lib")
        }
    }

    //TODO: test with obfuscation.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!checkPermissions()) {
            requestPermissions() //TODO: restart app after granting permissions.
            return
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = false
        toggle.syncState()

        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        nav_view.setNavigationItemSelectedListener(this)

        proc = Processing(
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) "/" else*/ Environment.getExternalStorageDirectory().path,
            this as MainView)

        MobileAds.initialize(this, "")
        adView.loadAd(AdRequest.Builder()/*.addTestDevice("")*/.build())
    }

    //TODO: change columnCount to 4 when orientation changes to landscape.

    override fun _findViewById(@IdRes id: Int): View = findViewById(id)

    override fun initGridManager(columnCount: Int): GridLayoutManager = GridLayoutManager(this, columnCount)

    override fun initItemDivider(orientation: Int): DividerItemDecoration = DividerItemDecoration(this, orientation)

    //ignore: kotlin properly access
    override fun setEmptyTextVisibility(visibility: Int): Unit = empty_text.setVisibility(visibility)

    override fun _startActivity(intent: Intent): Unit = startActivity(intent)

    override fun _getString(@StringRes stringId: Int): String = getString(stringId)

    override fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun _startActionMode(callback: ActionMode.Callback) {

        println("testo startActionMode") //TODO: debug.

        startSupportActionMode(callback)
    }

    @Suppress("deprecation")
    override fun _getColor(@ColorRes colorId: Int): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            resources.getColor(colorId, null)
        else
            resources.getColor(colorId)

    override fun makeSnackbar(msg: String, buttons: Array<String>, vararg actions: () -> Unit): Unit =
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).apply {
            for ((j, i) in buttons.withIndex()) {
                setAction(i) { _ -> actions[j].invoke() }
            }
        }.show()

    override fun askForAString(msg: String, actions: Array<(input: String) -> Unit>, buttons: Array<String>) {
        val edt = EditText(this).apply {
            textSize = 20f
            hint = msg
        }
        val d = AlertDialog.Builder(this)
            .setView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(edt)
            })
            .setPositiveButton(buttons[0]) { _, _ -> actions[0].invoke(edt.text.toString()) }

        if (buttons.size > 1)
            d.setNegativeButton(buttons[1]) { _, _ -> actions[1].invoke(edt.text.toString()) }

        d.show()
    }

    override fun setEnableMenu(enable: Boolean) {
        menu.findItem(R.id.search).isEnabled = enable
        menu.findItem(R.id.touch).isEnabled = enable
    }

    @Suppress("deprecation")
    override fun startLoading() {
        pd = ProgressDialog.show(this, "", getString(R.string.loading))
    }

    @Suppress("deprecation")
    override fun stopLoading(): Unit = pd.cancel()

    override fun showInfSnackbar(msg: String, action: () -> Unit, actText: String) {
        Handler(Looper.getMainLooper()).post {
            Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_INDEFINITE).apply {
                setAction(actText) { _ ->
                    dismiss()
                    action.invoke()
                }
                show()
            }
        }
    }

    override fun doPost(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }

    override fun showPath(path: String) {
        if (supportActionBar != null)
            supportActionBar!!.subtitle = path
    }

    override fun setVisibleActionBar(visible: Boolean) {
//        if (supportActionBar != null)
//            if (visible) supportActionBar!!.show() else supportActionBar!!.hide()
    }

    override fun showSearchPD(show: Boolean) {
        doPost {
            searchPD.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun doWithContext(action: (context: Context) -> Unit): Unit = action.invoke(this)

    override fun createSender(requestCode: Int, intent: Intent, flags: Int) =
        PendingIntent.getBroadcast(this, requestCode, intent, flags).intentSender!! //platform depend

    override fun createIntent(clazz: Class<*>, extras: Pair<String, Boolean>) =
        Intent(this, clazz).putExtra(extras.first, extras.second)

    override fun getPreference(what: String): Boolean = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(what, false)

    override fun useRoot(): Boolean = useRoot

    override fun showSnackbar(msg: String): Unit = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()

    override fun getTempFolder(): File = cacheDir

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var b = true
            for (i in grantResults)
                b = i == PackageManager.PERMISSION_GRANTED

            if (!b) {
                showToast("Permissions have been denied, exiting...")
                exitProcess(-1)
            } else
                restart()
        }
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true

        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun restart() {
        finish()

        startActivity(packageManager.getLaunchIntentForPackage(packageName)?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onBackPressed() {
        when {
            //drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            //proc.currentPath != "/" -> proc.goUp()
            proc.dirCounter > backCounter -> proc.goUp()
            else -> {
                //if (!proc.isChoosingItems)
                //if (!proc.dontHideActionMode)
                    super.onBackPressed()
            }
        }
        println("testo count ${proc.dirCounter} $backCounter") //TODO: debug.
        backCounter++
    }

//    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
//        println("testo dke ${proc.dontHideActionMode} ${event!!.keyCode} ${event.action}") //TODO: debug.
//
//        if (proc.dontHideActionMode){
//            if (event != null && event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
//                println("testo dke 3") //TODO: debug.
//                return true
//            }
//        }
//
//        return super.dispatchKeyEvent(event)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        if (checkPermissions())
            proc.initSearch(menu.findItem(R.id.search).actionView as SearchView)
        else
            return false

        menu.findItem(R.id.use_su).isChecked = useRoot

        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.touch -> {
                proc.onNewFile()
                true
            }
            R.id.use_su -> {
                launch(CommonPool) {
                    //Handler(Looper.getMainLooper()).post {
                        useRoot = !item.isChecked
                    //}
                }

                true
            }
            R.id.go_to -> {
                proc.onGoTo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    //TODO: add 'chmod' feature.
    //TODO: replace recursive search with linux native 'find' command.
    //TODO: finish the AudioListener.
    //TODO: add 'about' to all apps.

    override fun onConfigurationChanged(newConfig: Configuration?) {
        if (newConfig != null)
            proc.onOrientationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //R.id.nav_settings -> {
                //TODO: handle settings.
            //}
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
