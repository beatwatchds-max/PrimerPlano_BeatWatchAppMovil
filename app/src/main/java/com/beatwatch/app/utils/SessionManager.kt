package com.beatwatch.app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun guardarSesion(
        token: String,
        usuarioId: String,
        nombre: String,
        correo: String,
        telefono: String,
        rol: String,
        idLicencia: String
    ) {
        prefs.edit()
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

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "beatwatch_session"
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
    }
}
