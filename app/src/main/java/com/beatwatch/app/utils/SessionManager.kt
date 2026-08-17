package com.beatwatch.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.beatwatch.app.data.local.PulsacionesDatabase
import java.util.UUID

class SessionManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        appContext,
        SECURE_PREF_NAME,
        MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    ).also { migratePlaintextSession(appContext, it) }

    companion object {
        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }
        }

        private const val PREF_NAME = "beatwatch_session"
        private const val SECURE_PREF_NAME = "beatwatch_secure_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USUARIO_ID = "usuarioId"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_CORREO = "correo"
        private const val KEY_TELEFONO = "telefono"
        private const val KEY_ROL = "rol"
        private const val KEY_ID_LICENCIA = "idLicencia"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_PACIENTE_ID = "pacienteId"
        private const val KEY_PERFIL_COMPLETADO = "perfilCompletado"
        private const val KEY_DIAGNOSTICO_COMPLETADO = "diagnosticoCompletado"
        private const val KEY_DISPOSITIVO_VINCULADO = "dispositivoVinculado"
        private const val KEY_FCM_TOKEN = "fcmToken"
        private const val KEY_DATABASE_PASSPHRASE = "databasePassphrase"

        private fun migratePlaintextSession(context: Context, securePrefs: SharedPreferences) {
            if (securePrefs.all.isNotEmpty()) return

            val legacyPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            if (legacyPrefs.all.isEmpty()) return

            val editor = securePrefs.edit()
            legacyPrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
            editor.apply()
            legacyPrefs.edit().clear().apply()
        }
    }

    fun guardarSesion(
        token: String,
        usuarioId: String,
        nombre: String,
        correo: String,
        telefono: String,
        rol: String,
        idLicencia: String
    ) {
        appContext.deleteDatabase(PulsacionesDatabase.DATABASE_NAME)
        prefs.edit()
            // Los datos clínicos pertenecen a la sesión anterior y no deben heredarse.
            .remove(KEY_PACIENTE_ID)
            .remove(KEY_PERFIL_COMPLETADO)
            .remove(KEY_DIAGNOSTICO_COMPLETADO)
            .remove(KEY_DISPOSITIVO_VINCULADO)
            .remove(KEY_DATABASE_PASSPHRASE)
            .putString(KEY_TOKEN, token)
            .putString(KEY_USUARIO_ID, usuarioId)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_CORREO, correo)
            .putString(KEY_TELEFONO, telefono)
            .putString(KEY_ROL, rol)
            .putString(KEY_ID_LICENCIA, idLicencia)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""

    fun getUsuarioId(): String = prefs.getString(KEY_USUARIO_ID, "") ?: ""

    fun getNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""

    fun getCorreo(): String = prefs.getString(KEY_CORREO, "") ?: ""

    fun getTelefono(): String = prefs.getString(KEY_TELEFONO, "") ?: ""

    fun getRol(): String = prefs.getString(KEY_ROL, "") ?: ""

    fun getIdLicencia(): String = prefs.getString(KEY_ID_LICENCIA, "") ?: ""

    fun guardarFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String = prefs.getString(KEY_FCM_TOKEN, "") ?: ""

    fun guardarPacienteId(pacienteId: String) {
        prefs.edit()
            .putString(KEY_PACIENTE_ID, pacienteId)
            .apply()
    }

    fun getPacienteId(): String = prefs.getString(KEY_PACIENTE_ID, "") ?: ""

    fun guardarEstadoFormularios(
        perfilCompletado: Boolean,
        diagnosticoCompletado: Boolean
    ) {
        prefs.edit()
            .putBoolean(KEY_PERFIL_COMPLETADO, perfilCompletado)
            .putBoolean(KEY_DIAGNOSTICO_COMPLETADO, diagnosticoCompletado)
            .apply()
    }

    fun isPerfilCompletado(): Boolean =
        prefs.getBoolean(KEY_PERFIL_COMPLETADO, false)

    fun isDiagnosticoCompletado(): Boolean =
        prefs.getBoolean(KEY_DIAGNOSTICO_COMPLETADO, false)

    fun guardarDispositivoVinculado(dispositivoVinculado: Boolean) {
        prefs.edit()
            .putBoolean(KEY_DISPOSITIVO_VINCULADO, dispositivoVinculado)
            .apply()
    }

    fun isDispositivoVinculado(): Boolean {
        return prefs.getBoolean(KEY_DISPOSITIVO_VINCULADO, false)
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getDatabasePassphrase(): String =
        prefs.getString(KEY_DATABASE_PASSPHRASE, null)
            ?: UUID.randomUUID().toString().also {
                prefs.edit().putString(KEY_DATABASE_PASSPHRASE, it).apply()
            }

    fun cerrarSesion() {
        appContext.deleteDatabase(PulsacionesDatabase.DATABASE_NAME)
        prefs.edit().clear().apply()
    }
}
