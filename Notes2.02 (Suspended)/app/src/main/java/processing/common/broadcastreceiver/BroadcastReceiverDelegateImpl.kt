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
import .common.doInCoroutine
import .mvp.model.individual.ICommands
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.notifications.Notifications

/**
 * @author Vad Nik
 * @version dated Aug 17, 2019.
 * @link https://github.com/vadniks
 */
private class BroadcastReceiverDelegateImpl(im: IIndividualModel) :
    IndividualModelWrapper(im), BroadcastReceiverDelegate {
    
    override val commands: ICommands = ICommands.STUB
    
    override fun onBootCompleted(context: Context) {
        if (isDBEncrypted(context)) {
            model.onReceivedEventForDecryption(IIndividualModel.EVENT_BOOT_COMPLETED, null)
            return
        }
        
        doInCoroutine {
            model.resetReminders()
            model.resetWidgets()
        }
    }
    
    override fun notifyOthers(context: Context, intent: Intent) {
        if (!isDBEncrypted(context))
            Notifications.create(context, intent)
        else
            model.onReceivedEventForDecryption(IIndividualModel.EVENT_NOTIFICATIONS, intent)
    }
    
    override fun open(context: Context, intent: Intent) {
        initiateOpening(context, intent.extras ?: return)
    }
}
