/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.individual

import .mvp.model.CrossPresenterModel
import .mvp.model.Model
import .mvp.model.impl.getIndividualModel
import .mvp.model.impl.presenter.fragment.FragmentIndividualModelImpl
import .mvp.model.impl.presenter.fragment.getDrawFragmentModel
import .mvp.model.impl.presenter.fragment.getEditFragmentModel
import .mvp.model.impl.presenter.fragment.getMainFragmentModel
import .mvp.model.impl.presenter.getMainActivityModel
import .mvp.model.impl.presenter.getPresenterIndividualModel
import .mvp.presenter.commands.MainActivityCommands
import .mvp.presenter.commands.fragment.DrawFragmentCommands
import .mvp.presenter.commands.fragment.EditFragmentCommands
import .mvp.presenter.commands.fragment.MainFragmentCommands

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
class IndividualModelFactory(private val crossPresenterModel: CrossPresenterModel, private val model: Model) {
    
    init {
        imf = this
    }
    
    fun getPresenterModel(id: Int, commands: ICommands): IIndividualModel {
        val im = getIndividualModel(commands, model)
        val pim = getPresenterIndividualModel(crossPresenterModel, im)
        val fim = FragmentIndividualModelImpl(pim)
        
        return when (id) {
            ID_MA -> getMainActivityModel(pim, commands as MainActivityCommands)
            ID_MF -> getMainFragmentModel(fim, commands as MainFragmentCommands)
            ID_EF -> getEditFragmentModel(fim, commands as EditFragmentCommands)
            ID_DF -> getDrawFragmentModel(fim, commands as DrawFragmentCommands)
            else -> throw IllegalArgumentException()
        }
    }
    
    fun getWrapped(commands: ICommands): IIndividualModel = getIndividualModel(commands, model)
    
    companion object {
        const val ID_MA = 0x2
        const val ID_MF = 0x4
        const val ID_EF = 0x6
        const val ID_DF = 0x8
        
        lateinit var imf: IndividualModelFactory
    }
}
