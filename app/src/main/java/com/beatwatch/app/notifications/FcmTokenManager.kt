package com.beatwatch.app.notifications

import android.content.Context
import android.provider.Settings
import com.beatwatch.app.data.model.RegistrarTokenRequest
import com.beatwatch.app.data.repository.NotificacionesRepository
import com.beatwatch.app.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FcmTokenManager {
    fun fetchAndSync(context: Context) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> sync(context, token) }
                .addOnFailureListener { }
        } catch (_: IllegalStateException) {
        }
    }

    fun sync(context: Context, token: String) {
        if (token.isBlank()) return
        val sessionManager = SessionManager.getInstance(context)
        sessionManager.guardarFcmToken(token)
        val jwt = sessionManager.getToken()
        if (!sessionManager.isLoggedIn() || jwt.isBlank()) return

        val request = RegistrarTokenRequest(
            token = token,
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: context.packageName
        )
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                NotificacionesRepository().registrarToken(jwt, request)
            }
        }
    }

}
