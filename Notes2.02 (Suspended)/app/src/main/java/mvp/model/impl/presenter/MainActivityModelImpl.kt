/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .model.impl.presenter

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import .mvp.model.individual.presenter.IPresenterIndividualModel
import .mvp.model.individual.presenter.MainActivityModel
import .mvp.model.individual.presenter.PresenterIndividualModelWrapper
import .mvp.presenter.commands.MainActivityCommands

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */

fun getMainActivityModel(
    pim: IPresenterIndividualModel,
    commands: MainActivityCommands): MainActivityModel =
    MainActivityModelImpl(pim, commands)

private class MainActivityModelImpl(
    pim: IPresenterIndividualModel,
    override val commands: MainActivityCommands)
: PresenterIndividualModelWrapper(pim), MainActivityModel {
    
    override fun init(vararg args: Any) =
        crossPresenterModel.onMainActivityPresenterInitialized(commands, args[0] as NavController, args[1] as Activity)
    
    override fun onActivityBackPressed(): Boolean = crossPresenterModel.onActivityBackPressed()
    
    override fun onMenuOpened(menu: Menu) {
        menu.close()
        crossPresenterModel.onMenuOpened(menu)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) =
        crossPresenterModel.onActivityResult(requestCode, resultCode, result)
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        crossPresenterModel.onRequestPermissionsResult(requestCode, permissions, grantResults)

    override fun onOptionsItemSelected(item: MenuItem) {
        if (item.itemId == android.R.id.home)
            crossPresenterModel.onHomeButtonClicked()
    }

    override fun onUIRendered() = crossPresenterModel.onUIRendered()

    override fun onPause() {
        model.isActivityShown = false
    }

    override fun onResume() {
        model.isActivityShown = true
    }
}
