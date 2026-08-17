package com.beatwatch.app.notifications

import android.app.Application
import android.app.NotificationManager
import android.Manifest
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BeatWatchNotificationManagerTest {
    private lateinit var application: Application
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        notificationManager = application.getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
    }

    @Test
    fun `crea el canal de alertas`() {
        BeatWatchNotificationManager.createChannel(application)

        assertNotNull(notificationManager.getNotificationChannel(BeatWatchNotificationManager.CHANNEL_ID))
    }

    @Test
    fun `muestra title body y alertId recibidos`() {
        BeatWatchNotificationManager.show(application, "Alerta FRECUENCIA_ALTA", "Frecuencia elevada", "alerta-123")

        val notification = shadowOf(notificationManager).allNotifications.single()
        assertEquals("Alerta FRECUENCIA_ALTA", notification.extras.getCharSequence("android.title"))
        assertEquals("Frecuencia elevada", notification.extras.getCharSequence("android.text"))
    }

    @Test
    @Config(sdk = [33])
    fun `no publica notificaciones sin permiso en Android 13`() {
        shadowOf(application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        BeatWatchNotificationManager.show(application, "Alerta", "Frecuencia elevada", "alerta-123")

        assertTrue(shadowOf(notificationManager).allNotifications.isEmpty())
    }
}
