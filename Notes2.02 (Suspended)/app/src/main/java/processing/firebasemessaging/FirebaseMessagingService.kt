/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.firebasemessaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * @author Vad Nik
 * @version dated Sep 15, 2019.
 * @link https://github.com/vadniks
 */
class FirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var delegate: FirebaseMessagingServiceDelegate

    override fun onCreate() {
        super.onCreate()
        delegate = FirebaseMessagingServiceDelegate.get(application)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        delegate.onMessageReceived(message)
    }
}
