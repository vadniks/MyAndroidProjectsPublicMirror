/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.impl.presenter.fragment

import .mvp.model.individual.presenter.IPresenterIndividualModel
import .mvp.model.individual.presenter.PresenterIndividualModelWrapper
import .mvp.model.individual.presenter.fragment.IFragmentIndividualModel

/**
 * @author Vad Nik
 * @version dated Aug 14, 2019.
 * @link https://github.com/vadniks
 */
class FragmentIndividualModelImpl(pim: IPresenterIndividualModel) :
    PresenterIndividualModelWrapper(pim), IFragmentIndividualModel
