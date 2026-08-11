package com.beatwatch.app.notifications

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.beatwatch.app.data.model.RegistrarTokenRequest
import com.beatwatch.app.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {
    fun fetchAndSync(context: Context) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> sync(context, token) }
                .addOnFailureListener { error -> Log.w(TAG, "No se pudo obtener el token FCM", error) }
        } catch (error: IllegalStateException) {
            Log.w(TAG, "Firebase no está configurado", error)
        }
    }

    fun sync(context: Context, token: String) {
        if (token.isBlank()) return
        val sessionManager = SessionManager.getInstance(context)
        sessionManager.guardarFcmToken(token)
        if (!sessionManager.isLoggedIn() || sessionManager.getToken().isBlank()) return

        val request = RegistrarTokenRequest(
            token = token,
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: context.packageName
        )
        // AuthApiService does not define an FCM registration endpoint yet.
        Log.i(TAG, "Token FCM pendiente de registro para ${request.deviceType}")
    }

    private const val TAG = "FCM_TOKEN"
}
