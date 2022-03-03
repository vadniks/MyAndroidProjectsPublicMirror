/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.viewbridge

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
interface MainActivityViewBridge : IViewBridge {
    
    fun onSetContentViewCalled(navController: NavController)
    
    fun isDark(context: Context): Boolean
    
    fun onCreateOptionsMenu(menu: Menu?)

    fun onOptionsItemSelected(item: MenuItem)

    fun onBackPressed(): Boolean
    
    fun onMenuOpened()
    
    fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?)
    
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

    fun onUIRendered()

    fun onPause()

    fun onResume()
}
