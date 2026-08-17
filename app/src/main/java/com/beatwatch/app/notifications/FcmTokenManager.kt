package com.beatwatch.app.notifications

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.beatwatch.app.data.model.RegistrarTokenRequest
import com.beatwatch.app.data.repository.NotificacionesRepository
import com.beatwatch.app.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

object FcmTokenManager {
    private const val TAG = "FCM_TOKEN"
    private const val MAX_CONNECTION_ATTEMPTS = 3
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun fetchAndSync(context: Context) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> sync(context.applicationContext, token) }
                .addOnFailureListener { error ->
                    Log.e(TAG, "No se pudo obtener el token FCM", error)
                }
        } catch (error: IllegalStateException) {
            Log.e(TAG, "Firebase Messaging no está disponible", error)
        }
    }

    fun sync(context: Context, token: String) {
        if (token.isBlank()) {
            Log.w(TAG, "Se ignoró un token FCM vacío")
            return
        }
        val appContext = context.applicationContext
        val sessionManager = SessionManager.getInstance(appContext)
        sessionManager.guardarFcmToken(token)
        val jwt = sessionManager.getToken()
        if (!sessionManager.isLoggedIn() || jwt.isBlank()) {
            Log.d(TAG, "Token FCM guardado localmente; falta iniciar sesión")
            return
        }

        val request = RegistrarTokenRequest(
            token = token,
            deviceId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
                ?: appContext.packageName
        )
        scope.launch {
            val repository = NotificacionesRepository()
            repeat(MAX_CONNECTION_ATTEMPTS) { attempt ->
                try {
                    val response = repository.registrarToken(jwt, request)
                    if (response.isSuccessful) {
                        Log.i(TAG, "Token FCM registrado correctamente")
                    } else {
                        val errorBody = response.errorBody()?.string()
                            ?.replace(token, "TOKEN_REDACTED")
                            ?.replace(jwt, "JWT_REDACTED")
                            ?.take(500)
                        Log.e(TAG, "Error registrando token FCM: HTTP ${response.code()}${errorBody?.let { ", $it" }.orEmpty()}")
                    }
                    return@launch
                } catch (error: IOException) {
                    if (attempt == MAX_CONNECTION_ATTEMPTS - 1) {
                        Log.e(TAG, "No se pudo conectar con el backend tras $MAX_CONNECTION_ATTEMPTS intentos", error)
                    } else {
                        Log.w(TAG, "Error de conexión al registrar token FCM; se reintentará", error)
                        delay(1_000L * (attempt + 1))
                    }
                } catch (error: Exception) {
                    Log.e(TAG, "Error inesperado al registrar el token FCM", error)
                    return@launch
                }
            }
        }
    }
}
