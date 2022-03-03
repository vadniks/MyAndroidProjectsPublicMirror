/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.fragment

import .mvp.model.individual.presenter.fragment.EditFragmentModel
import .mvp.presenter.BasePresenter
import .mvp.presenter.viewbridge.fragment.EditFragmentViewBridge
import .mvp.presenter.viewstate.fragment.EditFragmentView

/**
 * @author Vad Nik
 * @version dated Jul 15, 2019.
 * @link https://github.com/vadniks
 */
abstract class EditFragmentPresenter(view: EditFragmentView, vararg args: Any) :
    BasePresenter<EditFragmentView, EditFragmentModel, EditFragmentViewBridge>(view, *args)
