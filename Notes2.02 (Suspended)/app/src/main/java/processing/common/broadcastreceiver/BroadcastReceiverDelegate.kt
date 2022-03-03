/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.common.broadcastreceiver

import android.content.Context
import android.content.Intent
import .mvp.model.individual.IIndividualModel
import .processing.common.broadcastreceiver.BroadcastReceiverDelegateGetter.getBroadcastReceiverDelegate

/**
 * @author Vad Nik
 * @version dated Aug 17, 2019.
 * @link https://github.com/vadniks
 */
interface BroadcastReceiverDelegate : IIndividualModel {
    
    fun open(context: Context, intent: Intent)
    
    fun onBootCompleted(context: Context)
    
    fun notifyOthers(context: Context, intent: Intent)
    
    companion object {
        
        fun get(): BroadcastReceiverDelegate = getBroadcastReceiverDelegate()
    }
}
