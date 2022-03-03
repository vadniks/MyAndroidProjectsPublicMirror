/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.billing

import android.app.Activity
import android.content.Context
import androidx.annotation.WorkerThread
import .mvp.model.individual.ICommands
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelFactory
import .mvp.model.individual.IndividualModelWrapper

/**
 * @author Vad Nik
 * @version dated Sep 06, 2019.
 * @link https://github.com/vadniks
 */
interface Billing : IIndividualModel {

    @WorkerThread
    fun startForChecking(callback: (b: Billing) -> Unit)

    @WorkerThread
    fun startForPurchasing(activity: Activity)

    fun areAllEnabled(): Boolean

    fun areAdsDisabled(): Boolean

    companion object {
        private var bs: BillingStub? = null

        fun get(@Suppress("unused_parameter") context: Context): Billing = bs ?:
                BillingStub(IndividualModelFactory.imf.getWrapped(ICommands.STUB))
                    .apply { bs = this }
    }

    private class BillingStub(im: IIndividualModel) : IndividualModelWrapper(im), Billing {

        @WorkerThread
        override fun startForChecking(callback: (b: Billing) -> Unit) = Unit

        @WorkerThread
        override fun startForPurchasing(activity: Activity) = Unit

        override fun areAllEnabled(): Boolean = true

        override fun areAdsDisabled(): Boolean = false
    }
}
