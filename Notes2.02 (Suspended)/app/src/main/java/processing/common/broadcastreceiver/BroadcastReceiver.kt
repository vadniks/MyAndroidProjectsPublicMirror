/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.common.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author Vad Nik
 * @version dated Jul 21, 2019.
 * @link https://github.com/vadniks
 */
class BroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null)
            return

        if (intent.action == makeAction(context, ACTION_OPEN))
            delegate.open(context, intent)

        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == delegate.decodeHardcoded(""))
            // android.intent.action.QUICKBOOT_POWERON
            delegate.onBootCompleted(context)
        
        delegate.notifyOthers(context, intent)
    }
    
    companion object {
        private val delegate = BroadcastReceiverDelegate.get()
        
        val ACTION_OPEN = delegate.decodeHardcodedWithoutEqual("") // ACTION_OPEN

        const val EXTRA_ID = 0x00000018.toString()
        const val EXTRA_MODE = 0x00000024.toString()

        fun makeAction(context: Context, action: String): String = context.packageName + '.' + action

        fun open(context: Context, intent: Intent) = delegate.open(context, intent)
    }
}
