/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import .R
import .common.EventObservable
import .mvp.model.CrossPresenterModel
import .mvp.model.Model
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .mvp.presenter.commands.MainActivityCommands
import .mvp.presenter.commands.fragment.DrawFragmentCommands
import .mvp.presenter.commands.fragment.EditFragmentCommands
import .mvp.presenter.commands.fragment.MainFragmentCommands
import .processing.audios.Audios
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Aug 14, 2019.
 * @link https://github.com/vadniks
 */

fun getCrossPresenterModel(im: IIndividualModel): CrossPresenterModel = CrossPresenterModelImpl(im)

private class CrossPresenterModelImpl(im: IIndividualModel) : IndividualModelWrapper(im), CrossPresenterModel {
    private lateinit var mapCommands: MainActivityCommands
    private lateinit var mfpCommands: MainFragmentCommands
    private lateinit var efpCommands: EditFragmentCommands
    private lateinit var dfpCommands: DrawFragmentCommands
    private lateinit var navController: NavController
    private val onMenuButtonClickedObservable = OnMenuButtonClickedObservable()
    private val onActivityResultObservable = OnActivityResultObservable()
    private val onActivityStartedObservable = OnActivityStartedObservable()
    private lateinit var activity: Activity
    private val onRequestPermissionsResultObservable = OnRequestPermissionsResultObservable()
    private val onUIRenderedObservable = OnUIRenderedObservable()
    
    override fun onMainActivityPresenterInitialized(p: MainActivityCommands, navController: NavController, activity: Activity) {
        mapCommands = p
        this.navController = navController
        this.activity = activity
        
        onActivityStartedObservable.onEventHappened()
    }
    
    override fun onMainFragmentPresenterInitialized(p: MainFragmentCommands) {
        mfpCommands = p
    }
    
    override fun onEditFragmentPresenterInitialized(p: EditFragmentCommands) {
        efpCommands = p
    }
    
    override fun onDrawFragmentPresenterInitialized(p: DrawFragmentCommands) {
        dfpCommands = p
    }
    
    override fun initiateCreating(mode: Int, args: Bundle?) =
        when (mode) {
            Model.MODE_NOTE_USUAL -> navigateToEditFragment(args)
            Model.MODE_NOTE_AUDIO -> {
                if (checkIsAllAvailable(activity))
                    Audios.get(activity, null).showAudioDialog()
                Unit
            }
            Model.MODE_NOTE_DRAWN -> {
                if (checkIsAllAvailable(activity))
                    navigateToDrawFragment(null)
                Unit
            }
            else -> throw IllegalArgumentException()
        }

    override fun initiateViewing(mode: Int, n: Note) =
        when (mode) {
            Model.MODE_NOTE_USUAL ->
                navigateToEditFragment(Bundle().apply { putSerializable(CrossPresenterModel.EXTRA_NOTE, n) })
            Model.MODE_NOTE_AUDIO -> {
                if (checkIsAllAvailable(activity))
                    Audios.get(activity, n).showAudioDialog()
                Unit
            }
            Model.MODE_NOTE_DRAWN -> {
                if (checkIsAllAvailable(activity))
                    navigateToDrawFragment(Bundle().apply { putSerializable(CrossPresenterModel.EXTRA_NOTE, n) })
                Unit
            }
            else -> throw IllegalArgumentException()
        }
    
    private fun navigateToEditFragment(extras: Bundle?) =
        if (navController.currentDestination?.id == R.id.stubFragment)
            navigateFromStubToEditFragment(extras)
        else
            navController.navigate(R.id.action_mainFragment_to_editFragment, extras)

    private fun navigateToDrawFragment(extras: Bundle?) =
        navController.navigate(R.id.action_mainFragment_to_drawFragment, extras)

    override fun initiateMain(args: Bundle?) =
        when (navController.currentDestination?.id) {
            R.id.stubFragment -> navigateFromStubToMainFragment(args)
            R.id.editFragment -> navController.navigate(R.id.action_editFragment_to_mainFragment, args)
            R.id.drawFragment -> navController.navigate(R.id.action_drawFragment_to_mainFragment, args)
            else -> throw IllegalArgumentException()
        }
    
    private fun navigateFromStubToMainFragment(args: Bundle?) =
        navController.navigate(R.id.action_stubFragment_to_mainFragment, args)
    
    private fun navigateFromStubToEditFragment(args: Bundle?) =
        navController.navigate(R.id.action_stubFragment_to_editFragment, args)
    
    override fun onMenuOpened(menu: Menu) = onMenuButtonClickedObservable.onEventHappened()
    
    override fun setToolbar(t: Toolbar) = mapCommands.setToolbar(t)
    
    override fun getFragmentManager(): FragmentManager = mapCommands.getFragmentManager()
    
    override fun inflateMenu(id: Int) = mapCommands.inflateMenu(id)
    
    override fun deflateMenu() = mapCommands.deflateMenu()
    
    override fun subscribeForMenuButton(o: CrossPresenterModel.OnMenuButtonClicked) {
        onMenuButtonClickedObservable.subscribe(o)
    }
    
    override fun unsubscribeOfMenuButton(o: CrossPresenterModel.OnMenuButtonClicked) {
        onMenuButtonClickedObservable.unsubscribe(o)
    }
    
    override fun queueForMenuCreation(action: (menu: Menu?) -> Unit) = mapCommands.queueForMenuCreation(action)
    
    override fun finishActivity() = mapCommands.finish()
    
    override fun setActivityResult(res: Int, data: Intent?) = mapCommands.setResult(res, data)
    
    override fun startActionMode(callback: ActionMode.Callback) = mapCommands.startActionMode(callback)
    
    override fun startActivityForResult(i: Intent, r: Int) = mapCommands.startActivityForResult(i, r)
    
    override fun subscribeForActivityStart(o: CrossPresenterModel.OnActivityStarted) {
        onActivityStartedObservable.subscribe(o)
    }
    
    override fun unsubscribeOfActivityStart(o: CrossPresenterModel.OnActivityStarted) {
        onActivityStartedObservable.unsubscribe(o)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) =
        onActivityResultObservable.onEventHappened(requestCode, resultCode, result)
    
    override fun subscribeForActivityResult(o: CrossPresenterModel.OnActivityResult) {
        onActivityResultObservable.subscribe(o)
    }
    
    override fun unsubscribeOfActivityResult(o: CrossPresenterModel.OnActivityResult) {
        onActivityResultObservable.unsubscribe(o)
    }
    
    override fun getIntent(): Intent = mapCommands.getIntent()
    
    override fun showWarningDialog(@StringRes act: Int, @StringRes msg: Int, action: () -> Unit): AlertDialog =
        AlertDialog.Builder(activity)
            .setTitle(R.string.warning)
            .setMessage(msg)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setPositiveButton(act) { _, _ -> action() }
            .create()
            .apply { show() }
    
    override fun showCustomDialog(
        title: String?,
        view: View,
        isCancelable: Boolean,
        onCreateArgs: (AlertDialog.Builder) -> Unit,
        onCreateArgs2: (Dialog) -> Unit): Dialog =

        AlertDialog.Builder(activity).apply {
            if (title != null)
                setTitle(title)
            
            setView(view)
            
            setCancelable(isCancelable)

            onCreateArgs(this)
        }.create().apply {
            setCanceledOnTouchOutside(isCancelable)

            onCreateArgs2(this)

            show()
        }
    
    @SuppressLint("InflateParams")
    override fun showProgressDialog(title: String): Dialog =
        showCustomDialog(
            title,
            LayoutInflater.from(activity).inflate(R.layout.progress_bar_horizontal, null),
            false)
    
    override fun showAlertDialog(
        applying2: (a: AlertDialog) -> AlertDialog,
        applying: (a: AlertDialog.Builder) -> Unit): AlertDialog =
        AlertDialog.Builder(activity)
            .apply { applying(this) }
            .create()
            .apply { applying2(this).show() }
    
    override fun setSearchViewEnabled(e: Boolean) = mapCommands.setSearchViewEnabled(e)
    
    override fun onActivityBackPressed(): Boolean = true
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        onRequestPermissionsResultObservable.onEventHappened(requestCode, permissions, grantResults)
    
    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        else
            onRequestPermissionsResultObservable.onEventHappened(
                requestCode,
                permissions,
                IntArray(permissions.size) { PackageManager.PERMISSION_GRANTED })
    }
    
    override fun subscribeForPermissionsResult(o: CrossPresenterModel.OnRequestPermissionsResult) {
        onRequestPermissionsResultObservable.subscribe(o)
    }
    
    override fun unsubscribeOfPermissionsResult(o: CrossPresenterModel.OnRequestPermissionsResult) {
        onRequestPermissionsResultObservable.unsubscribe(o)
    }

    override fun setShowHomeButton(b: Boolean) {
        mapCommands.setShowHomeButton(b)
    }

    override fun onHomeButtonClicked() = initiateMain(null)

    override fun getRootView(): View = mapCommands.getRootView()

    override fun onUIRendered() = onUIRenderedObservable.onEventHappened()

    override fun subscribeForUIRendered(o: CrossPresenterModel.OnUIRendered) {
        onUIRenderedObservable.subscribe(o)
    }

    override fun unsubscribeOfUIRendered(o: CrossPresenterModel.OnUIRendered) {
        onUIRenderedObservable.unsubscribe(o)
    }

    private class OnMenuButtonClickedObservable : EventObservable<CrossPresenterModel.OnMenuButtonClicked>() {
        
        fun onEventHappened() {
            for (i in subs)
                i.onMenuButtonClicked()
        }
    }
    
    private class OnActivityResultObservable : EventObservable<CrossPresenterModel.OnActivityResult>() {
        
        fun onEventHappened(requestCode: Int, resultCode: Int, result: Intent?) {
            for (i in subs)
                i.onActivityResult(requestCode, resultCode, result)
        }
    }
    
    private class OnActivityStartedObservable : EventObservable<CrossPresenterModel.OnActivityStarted>() {
        
        fun onEventHappened() {
            for (i in subs)
                i.onActivityStarted()
        }
    }
    
    private class OnRequestPermissionsResultObservable : EventObservable<CrossPresenterModel.OnRequestPermissionsResult>() {
        
        fun onEventHappened(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            for (i in subs)
                i.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private class OnUIRenderedObservable : EventObservable<CrossPresenterModel.OnUIRendered>() {

        fun onEventHappened() {
            for (i in subs)
                i.onUIRendered()
        }
    }
}
