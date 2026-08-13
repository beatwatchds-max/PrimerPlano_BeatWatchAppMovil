package com.beatwatch.app

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.repository.AuthRepository
import com.beatwatch.app.utils.SessionManager
import com.beatwatch.app.notifications.FcmTokenManager
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var etToken: EditText
    private lateinit var btnLogin: AppCompatButton
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etToken = findViewById(R.id.etToken)
        btnLogin = findViewById(R.id.btnLogin)
        authRepository = AuthRepository()
        sessionManager = SessionManager.getInstance(this)

        if (restaurarSesion()) return

        requestNotificationPermission()

        btnLogin.setOnClickListener {
            val token = etToken.text.toString().trim()

            if (token.isEmpty()) {
                etToken.error = getString(R.string.error_token_empty)
                etToken.requestFocus()
            } else {
                etToken.error = null
                iniciarSesion(token)
            }
        }
    }

    private fun iniciarSesion(token: String) {
        btnLogin.isEnabled = false
        btnLogin.text = getString(R.string.login_button_loading)

        lifecycleScope.launch {
            try {
                Log.d("LOGIN_API", "Enviando token: $token")
                val response = authRepository.iniciarSesionMovil(token)

                Log.d("LOGIN_API", "HTTP code: ${response.code()}")
                Log.d("LOGIN_API", "isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("LOGIN_API", "Body recibido: $body")

                    val jwt = body?.tokenJwt ?: body?.token ?: body?.accessToken ?: body?.jwt

                    val usuarioId = body?.usuarioId.orEmpty()
                    val idLicencia = body?.idLicencia.orEmpty()
                    val pacienteId = body?.pacienteId.orEmpty()
                    val perfilCompletado = body?.perfilCompletado ?: false
                    val diagnosticoCompletado = body?.diagnosticoCompletado ?: false
                    val dispositivoVinculado = body?.dispositivoVinculado ?: false

                    Log.d("LOGIN_FLOW", "Body login: $body")
                    Log.d("LOGIN_FLOW", "Login exitoso")
                    Log.d("LOGIN_FLOW", "JWT existe: ${!jwt.isNullOrBlank()}")
                    Log.d("LOGIN_FLOW", "usuarioId: $usuarioId")
                    Log.d("LOGIN_FLOW", "idLicencia: $idLicencia")
                    Log.d("LOGIN_FLOW", "pacienteId: $pacienteId")
                    Log.d("LOGIN_FLOW", "perfilCompletado: $perfilCompletado")
                    Log.d("LOGIN_FLOW", "diagnosticoCompletado: $diagnosticoCompletado")
                    Log.d("LOGIN_FLOW", "dispositivoVinculado: $dispositivoVinculado")

                    if (jwt.isNullOrBlank()) {
                        Log.e("LOGIN_API", "Token JWT vacío en respuesta: $body")
                        Toast.makeText(
                            this@LoginActivity,
                            "Token JWT vacío en respuesta",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    if (usuarioId.isBlank()) {
                        Log.e("LOGIN_API", "usuarioId vacío en respuesta: $body")
                        Toast.makeText(
                            this@LoginActivity,
                            "Usuario inválido en respuesta",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    if (idLicencia.isBlank()) {
                        Log.e("LOGIN_API", "idLicencia vacío en respuesta: $body")
                        Toast.makeText(
                            this@LoginActivity,
                            "Licencia inválida en respuesta",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    sessionManager.guardarSesion(
                        token = jwt,
                        usuarioId = usuarioId,
                        nombre = body?.nombre.orEmpty(),
                        correo = body?.correo.orEmpty(),
                        telefono = body?.telefono.orEmpty(),
                        rol = body?.rol.orEmpty(),
                        idLicencia = idLicencia
                    )

                    if (pacienteId.isNotBlank()) {
                        sessionManager.guardarPacienteId(pacienteId)
                    }

                    sessionManager.guardarEstadoFormularios(
                        perfilCompletado = perfilCompletado,
                        diagnosticoCompletado = diagnosticoCompletado
                    )

                    sessionManager.guardarDispositivoVinculado(dispositivoVinculado)
                    FcmTokenManager.fetchAndSync(this@LoginActivity)

                    Log.d("SESSION_DEBUG", "Sesión guardada")
                    Log.d("SESSION_DEBUG", "token existe: ${sessionManager.getToken().isNotBlank()}")
                    Log.d("SESSION_DEBUG", "usuarioId guardado: ${sessionManager.getUsuarioId()}")
                    Log.d("SESSION_DEBUG", "idLicencia guardada: ${sessionManager.getIdLicencia()}")
                    Log.d("SESSION_DEBUG", "pacienteId guardado: ${sessionManager.getPacienteId()}")
                    Log.d("SESSION_DEBUG", "perfilCompletado guardado: ${sessionManager.isPerfilCompletado()}")
                    Log.d("SESSION_DEBUG", "diagnosticoCompletado guardado: ${sessionManager.isDiagnosticoCompletado()}")
                    Log.d("SESSION_DEBUG", "dispositivoVinculado guardado: ${sessionManager.isDispositivoVinculado()}")

                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.msg_login_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    when {
                        !esPaciente(body?.rol) -> {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }

                        !perfilCompletado -> {
                            Log.d("LOGIN_FLOW", "Perfil no completado -> RegistroPacienteActivity")
                            startActivity(Intent(this@LoginActivity, RegistroPacienteActivity::class.java))
                            finish()
                        }

                        perfilCompletado && !diagnosticoCompletado -> {
                            Log.d("LOGIN_FLOW", "Perfil completado, diagnóstico no completado -> RegistroArritmiaActivity")
                            startActivity(Intent(this@LoginActivity, RegistroArritmiaActivity::class.java))
                            finish()
                        }

                        perfilCompletado && diagnosticoCompletado && !dispositivoVinculado -> {
                            Log.d("LOGIN_FLOW", "Perfil y diagnóstico completados, dispositivo no vinculado -> ConectarDispositivoActivity")
                            startActivity(Intent(this@LoginActivity, ConectarDispositivoActivity::class.java))
                            finish()
                        }

                        perfilCompletado && diagnosticoCompletado && dispositivoVinculado -> {
                            Log.d("LOGIN_FLOW", "Flujo completo, dispositivo vinculado -> MainActivity")
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LOGIN_API", "Error HTTP ${response.code()}: $errorBody")

                    val mensaje = when (response.code()) {
                        400, 401 -> getString(R.string.error_invalid_token)
                        429 -> getString(R.string.error_too_many_attempts)
                        in 500..599 -> getString(R.string.error_server)
                        else -> getString(R.string.error_invalid_token)
                    }
                    Toast.makeText(this@LoginActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("LOGIN_API", "Error de conexión", e)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_connection),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("LOGIN_API", "Error inesperado", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = getString(R.string.login_button)
            }
        }
    }

    private fun esPaciente(rol: String?): Boolean =
        rol.equals("Paciente", ignoreCase = true)

    private fun restaurarSesion(): Boolean {
        if (!sessionManager.isLoggedIn() || sessionManager.getToken().isBlank()) return false

        val destino = when {
            !esPaciente(sessionManager.getRol()) -> MainActivity::class.java
            !sessionManager.isPerfilCompletado() -> RegistroPacienteActivity::class.java
            !sessionManager.isDiagnosticoCompletado() -> RegistroArritmiaActivity::class.java
            !sessionManager.isDispositivoVinculado() -> ConectarDispositivoActivity::class.java
            else -> MainActivity::class.java
        }

        startActivity(Intent(this, destino))
        finish()
        return true
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
