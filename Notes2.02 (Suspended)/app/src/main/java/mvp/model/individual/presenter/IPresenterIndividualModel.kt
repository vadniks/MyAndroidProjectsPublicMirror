/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.individual.presenter

import android.content.Context
import androidx.annotation.UiThread
import com.google.android.gms.ads.AdRequest
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.IIndividualModel

/**
 * @author Vad Nik
 * @version dated Aug 14, 2019.
 * @link https://github.com/vadniks
 */
interface IPresenterIndividualModel : IIndividualModel {
    val crossPresenterModel: CrossPresenterModel
    
    fun init(vararg args: Any)
    
    @UiThread
    fun changeTheme(isDark: Boolean, context: Context)

    fun createAdRequest(): AdRequest?
}
