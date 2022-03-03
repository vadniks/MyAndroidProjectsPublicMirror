/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.firebasemessaging

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.firebase.messaging.RemoteMessage
import .processing.firebasemessaging.FirebaseMessagingServiceDelegateGetter.getFirebaseMessagingServiceDelegate

/**
 * @author Vad Nik
 * @version dated Sep 15, 2019.
 * @link https://github.com/vadniks
 */
interface FirebaseMessagingServiceDelegate {

    fun onMessageReceived(message: RemoteMessage)

    @WorkerThread
    fun subscribeToAllTopic()

    companion object {

        fun get(context: Context): FirebaseMessagingServiceDelegate = getFirebaseMessagingServiceDelegate(context)
    }
}
