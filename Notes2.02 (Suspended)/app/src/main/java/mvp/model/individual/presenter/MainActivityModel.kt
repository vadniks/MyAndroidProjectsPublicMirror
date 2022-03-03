/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.individual.presenter

import android.content.Intent
import android.view.Menu
import android.view.MenuItem

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
interface MainActivityModel : IPresenterIndividualModel {
    
    fun onActivityBackPressed(): Boolean
    
    fun onMenuOpened(menu: Menu)
    
    fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?)
    
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

    fun onOptionsItemSelected(item: MenuItem)

    fun onUIRendered()

    fun onResume()

    fun onPause()
}
