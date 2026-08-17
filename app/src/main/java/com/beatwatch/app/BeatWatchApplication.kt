package com.beatwatch.app

import android.app.Application
import com.beatwatch.app.notifications.BeatWatchNotificationManager

class BeatWatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BeatWatchNotificationManager.createChannel(this)
    }
}
