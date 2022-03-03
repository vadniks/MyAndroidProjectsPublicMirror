/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.model.impl.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.presenter.fragment.DrawFragmentModel
import .mvp.model.individual.presenter.fragment.FragmentIndividualModelWrapper
import .mvp.model.individual.presenter.fragment.IFragmentIndividualModel
import .mvp.presenter.commands.fragment.DrawFragmentCommands
import .processing.common.Note
import .processing.draw.processing.Drawn

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */

fun getDrawFragmentModel(fim: IFragmentIndividualModel, commands: DrawFragmentCommands): DrawFragmentModel =
    DrawFragmentModelImpl(fim, commands)

private class DrawFragmentModelImpl(fim: IFragmentIndividualModel, override val commands: DrawFragmentCommands) :
    FragmentIndividualModelWrapper(fim), DrawFragmentModel {
    
    private lateinit var drawn: Drawn
    
    override fun init(vararg args: Any) = crossPresenterModel.onDrawFragmentPresenterInitialized(commands)
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        drawn = Drawn.get(
            commands.getArgs()?.getSerializable(CrossPresenterModel.EXTRA_NOTE) as Note?,
            commands.getContext())
        
        return drawn.initializeView()
    }
    
    override fun onDestroy() = drawn.onDestroy()
}
