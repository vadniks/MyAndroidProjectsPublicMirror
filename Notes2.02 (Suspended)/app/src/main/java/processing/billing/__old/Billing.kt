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
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.IIndividualModel
import .processing.billing.BillingGetter.getBilling

/**
 * @author Vad Nik
 * @version dated Sep 06, 2019.
 * @link https://github.com/vadniks
 */
interface Billing : IIndividualModel, CrossPresenterModel.OnUIRendered {

    @WorkerThread
    fun startForChecking(callback: (b: Billing) -> Unit)

    @WorkerThread
    fun startForPurchasing(activity: Activity)

    fun areAllEnabled(): Boolean

    fun areAdsDisabled(): Boolean

    companion object {

        fun get(context: Context): Billing = getBilling(context)
    }
}
