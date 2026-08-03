package com.beatwatch.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.repository.PacienteRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Random

class InicioFragment : Fragment() {

    private lateinit var tvFrecuenciaCardiaca: TextView
    private lateinit var tvEstadoFrecuencia: TextView
    private lateinit var tvEstadoReloj: TextView
    private lateinit var tvUltimoPulso: TextView
    private lateinit var tvNombrePaciente: TextView
    private lateinit var tvDetallesPaciente: TextView
    private lateinit var tvDiagnosticoPaciente: TextView
    private lateinit var switchReloj: SwitchCompat
    private lateinit var btnTomarPulso: View
    private lateinit var sessionManager: SessionManager
    private lateinit var pacienteRepository: PacienteRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        pacienteRepository = PacienteRepository()

        tvFrecuenciaCardiaca = view.findViewById(R.id.tvFrecuenciaCardiaca)
        tvEstadoFrecuencia = view.findViewById(R.id.tvEstadoFrecuencia)
        tvEstadoReloj = view.findViewById(R.id.tvEstadoReloj)
        tvUltimoPulso = view.findViewById(R.id.tvUltimoPulso)
        tvNombrePaciente = view.findViewById(R.id.tvNombrePaciente)
        tvDetallesPaciente = view.findViewById(R.id.tvDetallesPaciente)
        tvDiagnosticoPaciente = view.findViewById(R.id.tvDiagnosticoPaciente)
        switchReloj = view.findViewById(R.id.switchReloj)
        btnTomarPulso = view.findViewById(R.id.btnTomarPulso)

        cargarDatosPaciente()
        configurarSwitchReloj()
        configurarBotonPulso()
        configurarCardsRapidas(view)
    }

    private fun cargarDatosPaciente() {
        val nombre = sessionManager.getNombre()
        val jwt = sessionManager.getToken()
        val usuarioId = sessionManager.getUsuarioId()

        if (jwt.isBlank()) {
            Toast.makeText(requireContext(), "Sesión inválida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
            redirigirLogin()
            return
        }

        if (usuarioId.isBlank()) {
            Toast.makeText(requireContext(), "No se encontró información del usuario.", Toast.LENGTH_LONG).show()
            redirigirLogin()
            return
        }

        tvNombrePaciente.text = nombre.ifBlank { "Paciente" }
        tvDetallesPaciente.text = "-- años · Tipo --"
        tvDiagnosticoPaciente.text = "Diagnóstico no disponible"

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

                    val nombreMostrar = nombre.ifBlank { "Paciente" }
                    tvNombrePaciente.text = nombreMostrar

                    val edad = paciente?.edad
                    val tipoSangre = paciente?.tipoSangre.orEmpty()

                    val edadTexto = if (edad != null) "$edad años" else "-- años"
                    val sangreTexto = if (tipoSangre.isNotBlank()) "Tipo $tipoSangre" else "Tipo --"

                    tvDetallesPaciente.text = "$edadTexto · $sangreTexto"
                    tvDiagnosticoPaciente.text = "Fibrilación auricular"
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PACIENTE_INFO", "ErrorBody: $errorBody")

                    when (response.code()) {
                        401 -> {
                            Toast.makeText(requireContext(), "Sesión expirada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                            sessionManager.cerrarSesion()
                            redirigirLogin()
                        }
                        404 -> {
                            Toast.makeText(requireContext(), "No se encontró perfil del paciente.", Toast.LENGTH_LONG).show()
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

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }

    private fun configurarSwitchReloj() {
        switchReloj.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvEstadoReloj.text = "Encendido"
                btnTomarPulso.isEnabled = true
                btnTomarPulso.alpha = 1.0f
            } else {
                tvEstadoReloj.text = "Apagado"
                btnTomarPulso.isEnabled = false
                btnTomarPulso.alpha = 0.5f
            }
        }
    }

    private fun configurarBotonPulso() {
        btnTomarPulso.setOnClickListener {
            val pulsoSimulado = Random().nextInt(31) + 60
            tvFrecuenciaCardiaca.text = pulsoSimulado.toString()
            tvUltimoPulso.text = "Último pulso: $pulsoSimulado bpm"

            tvEstadoFrecuencia.text = when {
                pulsoSimulado < 60 -> "Bajo"
                pulsoSimulado > 100 -> "Elevado"
                else -> "Normal"
            }
        }
    }

    private fun configurarCardsRapidas(view: View) {
        view.findViewById<View>(R.id.cardHistorial).setOnClickListener {
            val activity = activity as? MainActivity
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_historial
        }

        view.findViewById<View>(R.id.cardReportes).setOnClickListener {
            val activity = activity as? MainActivity
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_reportes
        }
    }
}
