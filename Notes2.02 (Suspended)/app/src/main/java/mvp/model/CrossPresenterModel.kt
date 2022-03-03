/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import .common.EventObserver
import .mvp.model.individual.IIndividualModel
import .mvp.presenter.commands.MainActivityCommands
import .mvp.presenter.commands.fragment.DrawFragmentCommands
import .mvp.presenter.commands.fragment.EditFragmentCommands
import .mvp.presenter.commands.fragment.MainFragmentCommands
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Aug 14, 2019.
 * @link https://github.com/vadniks
 */
interface CrossPresenterModel : IIndividualModel {
    
    fun onMainActivityPresenterInitialized(p: MainActivityCommands, navController: NavController, activity: Activity)
    
    fun onMainFragmentPresenterInitialized(p: MainFragmentCommands)
    
    fun onEditFragmentPresenterInitialized(p: EditFragmentCommands)
    
    fun onDrawFragmentPresenterInitialized(p: DrawFragmentCommands)
    
    fun initiateCreating(mode: Int, args: Bundle?)
    
    fun initiateViewing(mode: Int, n: Note)
    
    fun initiateMain(args: Bundle?)
    
    fun onMenuOpened(menu: Menu)
    
    fun setToolbar(t: Toolbar)
    
    @Deprecated("unused")
    fun getFragmentManager(): FragmentManager
    
    fun inflateMenu(@MenuRes id: Int)
    
    fun deflateMenu()
    
    fun subscribeForMenuButton(o: OnMenuButtonClicked)
    
    fun unsubscribeOfMenuButton(o: OnMenuButtonClicked)
    
    fun queueForMenuCreation(action: (menu: Menu?) -> Unit)
    
    fun finishActivity()
    
    fun setActivityResult(res: Int, data: Intent?)
    
    fun startActionMode(callback: ActionMode.Callback)
    
    fun startActivityForResult(i: Intent, r: Int)
    
    fun subscribeForActivityStart(o: OnActivityStarted)
    
    fun unsubscribeOfActivityStart(o: OnActivityStarted)
    
    fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?)
    
    fun subscribeForActivityResult(o: OnActivityResult)
    
    fun unsubscribeOfActivityResult(o: OnActivityResult)
    
    fun getIntent(): Intent

    @UiThread
    fun showWarningDialog(@StringRes act: Int, @StringRes msg: Int, action: () -> Unit): AlertDialog

    @UiThread
    fun showCustomDialog(
        title: String?,
        view: View,
        isCancelable: Boolean,
        onCreateArgs: (AlertDialog.Builder) -> Unit = {},
        onCreateArgs2: (Dialog) -> Unit = {}): Dialog

    @UiThread
    fun showProgressDialog(title: String): Dialog

    @UiThread
    fun showAlertDialog(
        applying2: (a: AlertDialog) -> AlertDialog = { it },
        applying: (a: AlertDialog.Builder) -> Unit): AlertDialog

    fun setSearchViewEnabled(e: Boolean)
    
    fun onActivityBackPressed(): Boolean
    
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    
    fun requestPermissions(permissions: Array<String>, requestCode: Int)
    
    fun subscribeForPermissionsResult(o: OnRequestPermissionsResult)
    
    fun unsubscribeOfPermissionsResult(o: OnRequestPermissionsResult)

    fun setShowHomeButton(b: Boolean)

    fun onHomeButtonClicked()

    fun getRootView(): View

    fun onUIRendered()

    fun subscribeForUIRendered(o: OnUIRendered)

    fun unsubscribeOfUIRendered(o: OnUIRendered)

    interface OnMenuButtonClicked : EventObserver {
        
        fun onMenuButtonClicked()
    }
    
    interface OnActivityResult : EventObserver {
        
        fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?)
    }
    
    interface OnActivityStarted : EventObserver {
        
        fun onActivityStarted()
    }

    interface OnRequestPermissionsResult : EventObserver {
        
        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    }

    interface OnUIRendered : EventObserver {

        fun onUIRendered()
    }

    companion object {
        const val EXTRA_NOTE = 0x00000002.toString()
        const val EXTRA_RECEIVED_TITLE = 0x00000004.toString()
        const val EXTRA_RECEIVED_TEXT = 0x00000006.toString()
    }
}
