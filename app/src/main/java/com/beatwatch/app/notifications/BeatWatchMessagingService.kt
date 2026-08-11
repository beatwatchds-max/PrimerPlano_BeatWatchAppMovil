package com.beatwatch.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BeatWatchMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"] ?: message.notification?.title
        val body = data["body"] ?: message.notification?.body
        BeatWatchNotificationManager.show(applicationContext, title, body, data["alertId"])
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenManager.sync(applicationContext, token)
    }
}
