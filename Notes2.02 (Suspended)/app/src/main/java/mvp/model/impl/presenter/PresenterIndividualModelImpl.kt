/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.impl.presenter

import android.content.Context
import com.google.android.gms.ads.AdRequest
import .model.CrossPresenterModel
import .model.individual.IIndividualModel
import .model.individual.IndividualModelWrapper
import .model.individual.presenter.IPresenterIndividualModel

/**
 * @author Vad Nik
 * @version dated Aug 14, 2019.
 * @link https://github.com/vadniks
 */

fun getPresenterIndividualModel(crossPresenterModel: CrossPresenterModel, im: IIndividualModel):
        IPresenterIndividualModel = PresenterIndividualModelImpl(crossPresenterModel, im)

private class PresenterIndividualModelImpl
    (override val crossPresenterModel: CrossPresenterModel, im: IIndividualModel) :
    IndividualModelWrapper(im), IPresenterIndividualModel {
    
    override fun init(vararg args: Any) = Unit
    
    override fun changeTheme(isDark: Boolean, context: Context) {
        if (!checkIsAllAvailable(context))
            return

        setDark(context, isDark)
        restart(context)
    }

    override fun createAdRequest(): AdRequest? = model.createAdRequest()
}
