package com.beatwatch.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.repository.PacienteRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class PerfilFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var pacienteRepository: PacienteRepository

    private lateinit var tvPerfilNombre: TextView
    private lateinit var tvPerfilRol: TextView
    private lateinit var tvPerfilEdad: TextView
    private lateinit var tvPerfilTipoSangre: TextView
    private lateinit var tvPerfilTelefono: TextView
    private lateinit var tvPerfilCorreo: TextView
    private lateinit var tvPerfilDireccion: TextView
    private lateinit var tvEditarPerfil: TextView
    private lateinit var tvCondicionMedica: TextView
    private lateinit var tvFrecuenciaPromedio: TextView
    private lateinit var tvCuidadorNombre: TextView
    private lateinit var tvCuidadorTelefono: TextView
    private lateinit var btnCerrarSesion: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager.getInstance(requireContext())
        pacienteRepository = PacienteRepository()

        tvPerfilNombre = view.findViewById(R.id.tvPerfilNombre)
        tvPerfilRol = view.findViewById(R.id.tvPerfilRol)
        tvPerfilEdad = view.findViewById(R.id.tvPerfilEdad)
        tvPerfilTipoSangre = view.findViewById(R.id.tvPerfilTipoSangre)
        tvPerfilTelefono = view.findViewById(R.id.tvPerfilTelefono)
        tvPerfilCorreo = view.findViewById(R.id.tvPerfilCorreo)
        tvPerfilDireccion = view.findViewById(R.id.tvPerfilDireccion)
        tvEditarPerfil = view.findViewById(R.id.tvEditarPerfil)
        tvCondicionMedica = view.findViewById(R.id.tvCondicionMedica)
        tvFrecuenciaPromedio = view.findViewById(R.id.tvFrecuenciaPromedio)
        tvCuidadorNombre = view.findViewById(R.id.tvCuidadorNombre)
        tvCuidadorTelefono = view.findViewById(R.id.tvCuidadorTelefono)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)

        cargarDatosPerfil()
        configurarListeners()
    }

    private fun cargarDatosPerfil() {
        val nombre = sessionManager.getNombre()
        val correo = sessionManager.getCorreo()
        val telefono = sessionManager.getTelefono()
        val rol = sessionManager.getRol()

        tvPerfilNombre.text = nombre.ifBlank { "Paciente" }
        tvPerfilRol.text = rol.ifBlank { "Paciente" }
        tvPerfilEdad.text = "--"
        tvPerfilTipoSangre.text = "--"
        tvPerfilTelefono.text = telefono.ifBlank { "No disponible" }
        tvPerfilCorreo.text = correo.ifBlank { "No disponible" }
        tvPerfilDireccion.text = "No disponible"

        tvCondicionMedica.text = "Fibrilación auricular"
        tvFrecuenciaPromedio.text = "Frecuencia cardíaca promedio: 95 bpm"
        tvCuidadorNombre.text = "Dr. Carlos Ramírez"
        tvCuidadorTelefono.text = "55 9876 5432"

        val jwt = sessionManager.getToken()
        val usuarioId = sessionManager.getUsuarioId()

        if (jwt.isBlank() || usuarioId.isBlank()) {
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("PACIENTE_INFO", "Endpoint: api/Pacientes/usuario/$usuarioId")
                Log.d("PACIENTE_INFO", "JWT existe: ${jwt.isNotBlank()}")
                Log.d("PACIENTE_INFO", "usuarioId: $usuarioId")

                val response = pacienteRepository.obtenerPacientePorUsuarioId(jwt, usuarioId)

                Log.d("PACIENTE_INFO", "HTTP code: ${response.code()}")
                Log.d("PACIENTE_INFO", "isSuccessful: ${response.isSuccessful}")
                Log.d("PACIENTE_INFO", "Body: ${response.body()}")

                if (response.isSuccessful) {
                    val paciente = response.body()

                    val pacienteId = paciente?.pacienteId.orEmpty()
                    if (pacienteId.isNotBlank()) {
                        sessionManager.guardarPacienteId(pacienteId)
                    }

                    val edad = paciente?.edad
                    val tipoSangre = paciente?.tipoSangre.orEmpty()
                    val direccion = paciente?.direccion.orEmpty()

                    tvPerfilEdad.text = if (edad != null) "$edad años" else "--"
                    tvPerfilTipoSangre.text = tipoSangre.ifBlank { "--" }
                    tvPerfilDireccion.text = direccion.ifBlank { "No disponible" }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PACIENTE_INFO", "ErrorBody: $errorBody")

                    when (response.code()) {
                        401 -> {
                            Toast.makeText(requireContext(), "Sesión expirada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                            sessionManager.cerrarSesion()
                            redirigirLogin()
                        }
                        in 500..599 -> {
                            Toast.makeText(requireContext(), "Error del servidor. Intenta más tarde.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("PACIENTE_INFO", "Error de conexión", e)
            } catch (e: Exception) {
                Log.e("PACIENTE_INFO", "Error inesperado", e)
            }
        }
    }

    private fun configurarListeners() {
        tvEditarPerfil.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente", Toast.LENGTH_SHORT).show()
        }

        btnCerrarSesion.setOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }
}
