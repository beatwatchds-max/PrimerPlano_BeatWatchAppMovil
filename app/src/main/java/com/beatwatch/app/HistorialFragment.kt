package com.beatwatch.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.RecomendacionMl
import com.beatwatch.app.data.repository.AnalisisRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class HistorialFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var analisisRepository: AnalisisRepository

    private lateinit var tvResumenAnalisis: TextView
    private lateinit var contenedorRecomendaciones: LinearLayout
    private lateinit var progressHistorial: ProgressBar
    private lateinit var tvHistorialVacio: TextView
    private lateinit var tvHistorialError: TextView

    private var datosCargados = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager.getInstance(requireContext())
        analisisRepository = AnalisisRepository()

        tvResumenAnalisis = view.findViewById(R.id.tvResumenAnalisis)
        contenedorRecomendaciones = view.findViewById(R.id.contenedorRecomendaciones)
        progressHistorial = view.findViewById(R.id.progressHistorial)
        tvHistorialVacio = view.findViewById(R.id.tvHistorialVacio)
        tvHistorialError = view.findViewById(R.id.tvHistorialError)

        if (!datosCargados) {
            cargarAnalisis()
        }
    }

    private fun cargarAnalisis() {
        val jwt = sessionManager.getToken()
        val idPaciente = sessionManager.getPacienteId()

        Log.d("ANALISIS_ML", "Endpoint: ${AnalisisRepository.BASE_URL}analysis/latest/$idPaciente")

        if (jwt.isBlank()) {
            Toast.makeText(requireContext(), "Sesión inválida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
            redirigirLogin()
            return
        }

        if (idPaciente.isBlank()) {
            Toast.makeText(requireContext(), "No se encontró información del paciente.", Toast.LENGTH_LONG).show()
            tvHistorialVacio.visibility = View.VISIBLE
            tvHistorialVacio.text = "No se encontró información del paciente."
            return
        }

        progressHistorial.visibility = View.VISIBLE
        tvHistorialVacio.visibility = View.GONE
        tvHistorialError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = analisisRepository.analizarUltimaEstadistica(idPaciente)

                Log.d("ANALISIS_ML", "HTTP code: ${response.code()}")

                progressHistorial.visibility = View.GONE

                if (response.isSuccessful) {
                    val analisis = response.body()
                    val recomendaciones = analisis?.recommendations.orEmpty()
                    if (analisis == null || recomendaciones.isEmpty()) {
                        tvHistorialVacio.visibility = View.VISIBLE
                        tvHistorialVacio.text = "Aún no hay recomendaciones disponibles."
                    } else {
                        mostrarAnalisis(analisis.risk?.risk, analisis.risk?.probability, analisis.anomaly?.anomaly, recomendaciones)
                    }

                    datosCargados = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("HISTORIAL_API", "ErrorBody: $errorBody")

                    when (response.code()) {
                        400 -> {
                            tvHistorialError.visibility = View.VISIBLE
                            tvHistorialError.text = "No se pudo analizar la información del paciente."
                        }
                        401, 403 -> {
                            tvHistorialError.visibility = View.VISIBLE
                            tvHistorialError.text = "El servicio de recomendaciones rechazó sus credenciales."
                        }
                        404 -> {
                            tvHistorialVacio.visibility = View.VISIBLE
                            tvHistorialVacio.text = "No hay estadísticas diarias para generar recomendaciones."
                        }
                        in 500..599 -> {
                            tvHistorialError.visibility = View.VISIBLE
                            tvHistorialError.text = "Error del servidor. Intenta más tarde."
                        }
                        else -> {
                            tvHistorialError.visibility = View.VISIBLE
                            tvHistorialError.text = "Error inesperado: ${response.code()}"
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("ANALISIS_ML", "Error de conexión", e)
                progressHistorial.visibility = View.GONE
                tvHistorialError.visibility = View.VISIBLE
                tvHistorialError.text = "No se pudo conectar con el servidor."
            } catch (e: Exception) {
                Log.e("ANALISIS_ML", "Error inesperado", e)
                progressHistorial.visibility = View.GONE
                tvHistorialError.visibility = View.VISIBLE
                tvHistorialError.text = "Error inesperado: ${e.message}"
            }
        }
    }

    private fun mostrarAnalisis(
        riesgo: String?,
        probabilidad: Double?,
        hayAnomalia: Boolean?,
        recomendaciones: List<RecomendacionMl>
    ) {
        val porcentaje = probabilidad?.times(100)?.toInt()
        tvResumenAnalisis.text = "Riesgo: ${riesgo ?: "no disponible"}${porcentaje?.let { " ($it%)" } ?: ""}\n" +
            "Patrón atípico: ${if (hayAnomalia == true) "detectado" else "no detectado"}"
        contenedorRecomendaciones.removeAllViews()
        recomendaciones.forEach { item ->
            val texto = TextView(requireContext()).apply {
                text = "${item.message.orEmpty()}\n${item.recommendation.orEmpty()}"
                setTextColor(resources.getColor(R.color.beatwatch_dark_blue, null))
                textSize = 14f
                setPadding(16, 16, 16, 16)
                setBackgroundColor(resources.getColor(R.color.white, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }
            contenedorRecomendaciones.addView(texto)
        }
    }

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }
}
