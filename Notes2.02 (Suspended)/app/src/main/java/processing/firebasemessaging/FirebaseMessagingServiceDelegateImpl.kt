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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import .processing.notifications.Notifications

/**
 * @author Vad Nik
 * @version dated Sep 15, 2019.
 * @link https://github.com/vadniks
 */
private class FirebaseMessagingServiceDelegateImpl(private val context: Context) : FirebaseMessagingServiceDelegate {

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.notification != null)
            return

        Notifications.create(
            context,
            message.notification?.title ?: return,
            message.notification?.body ?: return,
            Notifications.MODE_ONCE)
        { _, _ -> }
    }

    override fun subscribeToAllTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
    }
}
