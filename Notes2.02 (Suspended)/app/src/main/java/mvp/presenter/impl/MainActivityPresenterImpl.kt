/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import .R
import .mvp.model.individual.IndividualModelFactory
import .mvp.model.individual.presenter.MainActivityModel
import .mvp.presenter.MainActivityPresenter
import .mvp.presenter.commands.MainActivityCommands
import .mvp.presenter.viewbridge.MainActivityViewBridge
import .mvp.presenter.viewstate.MainActivityView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */

fun getMainActivityPresenter(view: MainActivityView, vararg args: Any): MainActivityPresenter =
    MainActivityPresenterImpl(view, *args)

private class MainActivityPresenterImpl(view: MainActivityView, vararg args: Any) :
    MainActivityPresenter(view, *args), MainActivityCommands, MainActivityViewBridge {
    
    private var menu: Menu? = null
    private var lastMenuId = -1
    private val onMenuCreationQueue = ArrayList<(menu: Menu?) -> Unit>()
    
    override val model: MainActivityModel =
        IndividualModelFactory.imf.getPresenterModel(IndividualModelFactory.ID_MA, this) as MainActivityModel
    
    override fun onSetContentViewCalled(navController: NavController) {
        navController.navigate(R.id.action_mainFragment_to_stubFragment)
        model.init(navController, getActivity())
    }
    
    override fun isDark(context: Context): Boolean = model.isDark(context)

    override fun onCreateOptionsMenu(menu: Menu?) {
        this.menu = menu
        deflateMenu()

        if (lastMenuId != -1) {
            inflateMenu(lastMenuId)
            lastMenuId = -1
        }

        invokeQueueds()
    }

    override fun onOptionsItemSelected(item: MenuItem) = model.onOptionsItemSelected(item)

    private fun invokeQueueds() {
        val iterator = onMenuCreationQueue.iterator()
        while (iterator.hasNext()) {
            val a = iterator.next()
            a(menu)
            iterator.remove()
        }
        onMenuCreationQueue.clear()
    }

    override fun onBackPressed(): Boolean = model.onActivityBackPressed()

    override fun onMenuOpened() = model.onMenuOpened(menu!!)
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) =
        model.onActivityResult(requestCode, resultCode, result)
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        model.onRequestPermissionsResult(requestCode, permissions, grantResults)

    override fun onUIRendered() = model.onUIRendered()

    override fun getFragmentManager(): FragmentManager = view._getFragmentManager()

    override fun setToolbar(t: Toolbar): Unit = view.setToolbar(t)

    override fun inflateMenu(id: Int) {
        lastMenuId = id
        view._getMenuInflater().inflate(id, menu ?: return)
    }

    override fun deflateMenu(): Unit = view._getMenuInflater().inflate(R.menu.menu_empty, menu)

    override fun queueForMenuCreation(action: (menu: Menu?) -> Unit) {
        onMenuCreationQueue.add(action)
    }

    override fun finish(): Unit = view._finish()

    override fun setResult(res: Int, data: Intent?): Unit = view._setResult(res, data)

    override fun setTheme(isDark: Boolean): Unit = view._setTheme(isDark)

    override fun getIntent(): Intent = view._getIntent()

    override fun startActionMode(callback: ActionMode.Callback): Unit = view._startActionMode(callback)

    override fun startActivityForResult(i: Intent, r: Int): Unit = view._startActivityForResult(i, r)
    
    override fun getActivity(): Activity = view.getContext() as Activity
    
    override fun setSearchViewEnabled(e: Boolean) {
        menu?.findItem(R.id.menu_main_actions_search)?.isEnabled = e
    }

    override fun setShowHomeButton(b: Boolean) = view.setShowHomeButton(b)

    override fun performBack() = view.performBack()

    override fun getRootView(): View = view.getRootView()

    override fun onPause() {
        super.onPause()
        model.onPause()
    }

    override fun onResume() {
        super.onResume()
        model.onResume()
    }
}
