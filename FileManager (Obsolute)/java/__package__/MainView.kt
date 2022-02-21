/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.v7.view.ActionMode
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.view.View
import java.io.File

/**
 * @author Vad Nik.
 * @version dated Dec 13, 2018.
 * @link http://github.com/vadniks
 */
internal interface MainView {
    fun _findViewById(@IdRes id: Int): View
    fun initGridManager(columnCount: Int): GridLayoutManager
    fun initItemDivider(orientation: Int): DividerItemDecoration
    fun setEmptyTextVisibility(visibility: Int)
    fun _startActivity(intent: Intent)
    fun _getString(@StringRes stringId: Int): String
    fun showToast(msg: String)
    @Deprecated("unnecessary")
    fun _startActionMode(callback: ActionMode.Callback)
    @Deprecated("use Color class instead")
    fun _getColor(@ColorRes colorId: Int): Int
    @Deprecated("Snackbar supports only one button")
    fun makeSnackbar(msg: String, buttons: Array<String> = arrayOf(""), vararg actions: () -> Unit = arrayOf({}))
    fun askForAString(msg: String, actions: Array<(input: String) -> Unit>, buttons: Array<String>)
    fun setEnableMenu(enable: Boolean)
    fun startLoading()
    fun stopLoading()
    fun showInfSnackbar(msg: String, action: () -> Unit, actText: String)
    fun doPost(action: () -> Unit)
    fun showPath(path: String)
    @Deprecated("it overlays it in styles")
    fun setVisibleActionBar(visible: Boolean)
    fun showSearchPD(show: Boolean)
    fun doWithContext(action: (context: Context) -> Unit)
    @Deprecated("")
    @RequiresApi(22)
    fun createSender(requestCode: Int, intent: Intent, flags: Int): IntentSender
    @Deprecated("")
    @RequiresApi(22)
    fun createIntent(clazz: Class<*>, extras: Pair<String, Boolean>): Intent
    fun getPreference(what: String): Boolean
    fun useRoot(): Boolean
    fun showSnackbar(msg: String)
    fun getTempFolder(): File
}
