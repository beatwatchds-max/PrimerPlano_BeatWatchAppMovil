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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.ConteoSintomasResponse
import com.beatwatch.app.data.model.ReporteResumenResponse
import com.beatwatch.app.data.repository.ReportesRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class ReportesFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var reportesRepository: ReportesRepository

    private var diasSeleccionados = 7

    private lateinit var progressReportes: ProgressBar
    private lateinit var tvReportesError: TextView
    private lateinit var cardPeriodo: View
    private lateinit var tvPeriodoTexto: TextView
    private lateinit var tvCambiarPeriodo: TextView
    private lateinit var cardSintomas: View
    private lateinit var barPalpitaciones: ProgressBar
    private lateinit var tvPalpitaciones: TextView
    private lateinit var barMareo: ProgressBar
    private lateinit var tvMareo: TextView
    private lateinit var barFatiga: ProgressBar
    private lateinit var tvFatiga: TextView
    private lateinit var barFaltaAire: ProgressBar
    private lateinit var tvFaltaAire: TextView
    private lateinit var barDolorPecho: ProgressBar
    private lateinit var tvDolorPecho: TextView
    private lateinit var barDesmayo: ProgressBar
    private lateinit var tvDesmayo: TextView
    private lateinit var cardMetricas: View
    private lateinit var tvEpisodiosTotales: TextView
    private lateinit var tvDiasEstables: TextView
    private lateinit var tvBpmPromedio: TextView
    private lateinit var btnVerPdf: AppCompatButton
    private lateinit var btnCompartir: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reportes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager.getInstance(requireContext())
        reportesRepository = ReportesRepository()

        progressReportes = view.findViewById(R.id.progressReportes)
        tvReportesError = view.findViewById(R.id.tvReportesError)
        cardPeriodo = view.findViewById(R.id.cardPeriodo)
        tvPeriodoTexto = view.findViewById(R.id.tvPeriodoTexto)
        tvCambiarPeriodo = view.findViewById(R.id.tvCambiarPeriodo)
        cardSintomas = view.findViewById(R.id.cardSintomas)
        barPalpitaciones = view.findViewById(R.id.barPalpitaciones)
        tvPalpitaciones = view.findViewById(R.id.tvPalpitaciones)
        barMareo = view.findViewById(R.id.barMareo)
        tvMareo = view.findViewById(R.id.tvMareo)
        barFatiga = view.findViewById(R.id.barFatiga)
        tvFatiga = view.findViewById(R.id.tvFatiga)
        barFaltaAire = view.findViewById(R.id.barFaltaAire)
        tvFaltaAire = view.findViewById(R.id.tvFaltaAire)
        barDolorPecho = view.findViewById(R.id.barDolorPecho)
        tvDolorPecho = view.findViewById(R.id.tvDolorPecho)
        barDesmayo = view.findViewById(R.id.barDesmayo)
        tvDesmayo = view.findViewById(R.id.tvDesmayo)
        cardMetricas = view.findViewById(R.id.cardMetricas)
        tvEpisodiosTotales = view.findViewById(R.id.tvEpisodiosTotales)
        tvDiasEstables = view.findViewById(R.id.tvDiasEstables)
        tvBpmPromedio = view.findViewById(R.id.tvBpmPromedio)
        btnVerPdf = view.findViewById(R.id.btnVerPdf)
        btnCompartir = view.findViewById(R.id.btnCompartir)

        tvCambiarPeriodo.setOnClickListener { mostrarSelectorPeriodo() }
        btnVerPdf.setOnClickListener { Toast.makeText(requireContext(), "Generación de PDF próximamente", Toast.LENGTH_SHORT).show() }
        btnCompartir.setOnClickListener { Toast.makeText(requireContext(), "Compartir reporte próximamente", Toast.LENGTH_SHORT).show() }

        actualizarTextoPeriodo()
        cargarResumen()
    }

    private fun cargarResumen() {
        val jwt = sessionManager.getToken()
        val idPaciente = sessionManager.getPacienteId()

        if (jwt.isBlank()) {
            Toast.makeText(requireContext(), "Sesión inválida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
            redirigirLogin()
            return
        }

        if (idPaciente.isBlank()) {
            Toast.makeText(requireContext(), "No se encontró información del paciente.", Toast.LENGTH_LONG).show()
            tvReportesError.visibility = View.VISIBLE
            tvReportesError.text = "No se encontró información del paciente."
            return
        }

        mostrarLoading(true)

        lifecycleScope.launch {
            try {
                Log.d("REPORTES_API", "Endpoint: api/tablero/resumen")
                Log.d("REPORTES_API", "JWT existe: ${jwt.isNotBlank()}")
                Log.d("REPORTES_API", "idPaciente: $idPaciente")
                Log.d("REPORTES_API", "dias: $diasSeleccionados")

                val response = reportesRepository.obtenerResumenTablero(jwt, idPaciente, diasSeleccionados)

                Log.d("REPORTES_API", "HTTP code: ${response.code()}")
                Log.d("REPORTES_API", "isSuccessful: ${response.isSuccessful}")
                Log.d("REPORTES_API", "Body: ${response.body()}")

                mostrarLoading(false)

                if (response.isSuccessful) {
                    val body = response.body()
                    mostrarContenido()
                    pintarDatos(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("REPORTES_API", "ErrorBody: $errorBody")

                    when (response.code()) {
                        400 -> {
                            mostrarError("Solicitud inválida. Verifica el paciente.")
                        }
                        401 -> {
                            Toast.makeText(requireContext(), "Sesión expirada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                            sessionManager.cerrarSesion()
                            redirigirLogin()
                        }
                        404 -> {
                            mostrarError("No hay reportes disponibles.")
                        }
                        in 500..599 -> {
                            mostrarError("Error del servidor. Intenta más tarde.")
                        }
                        else -> {
                            mostrarError("Error inesperado: ${response.code()}")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("REPORTES_API", "Error de conexión", e)
                mostrarLoading(false)
                mostrarError("No se pudo conectar con el servidor.")
            } catch (e: Exception) {
                Log.e("REPORTES_API", "Error inesperado", e)
                mostrarLoading(false)
                mostrarError("Error inesperado: ${e.message}")
            }
        }
    }

    private fun pintarDatos(body: ReporteResumenResponse?) {
        if (body == null) return

        Log.d("REPORTES_UI", "totalEpisodiosPeriodo: ${body.totalEpisodiosPeriodo}")
        Log.d("REPORTES_UI", "bpmPromedio: ${body.bpmPromedio}")
        Log.d("REPORTES_UI", "porcentajeDiasEstables: ${body.porcentajeDiasEstables}")
        Log.d("REPORTES_UI", "conteoSintomas: ${body.conteoSintomas}")

        // Métricas
        val episodios = body.totalEpisodiosPeriodo ?: 0
        val diasEstables = formatearPorcentaje(body.porcentajeDiasEstables)
        val bpmProm = body.bpmPromedio ?: 0

        tvEpisodiosTotales.text = episodios.toString()
        tvDiasEstables.text = diasEstables
        tvBpmPromedio.text = bpmProm.toString()

        // Síntomas
        val sintomas = body.conteoSintomas ?: ConteoSintomasResponse()

        val palpitaciones = sintomas.Palpitaciones ?: 0
        val mareo = sintomas.Mareo ?: 0
        val fatiga = sintomas.Fatiga ?: 0
        val faltaAire = sintomas.FaltaAire ?: 0
        val dolorPecho = sintomas.DolorPecho ?: 0
        val desmayo = sintomas.Desmayo ?: 0

        val maxSintoma = maxOf(palpitaciones, mareo, fatiga, faltaAire, dolorPecho, desmayo, 1)

        actualizarBarra(barPalpitaciones, tvPalpitaciones, palpitaciones, maxSintoma)
        actualizarBarra(barMareo, tvMareo, mareo, maxSintoma)
        actualizarBarra(barFatiga, tvFatiga, fatiga, maxSintoma)
        actualizarBarra(barFaltaAire, tvFaltaAire, faltaAire, maxSintoma)
        actualizarBarra(barDolorPecho, tvDolorPecho, dolorPecho, maxSintoma)
        actualizarBarra(barDesmayo, tvDesmayo, desmayo, maxSintoma)
    }

    private fun formatearPorcentaje(valor: Double?): String {
        val v = valor ?: 0.0
        return if (v % 1.0 == 0.0) {
            "${v.toInt()}%"
        } else {
            String.format(Locale.US, "%.1f%%", v)
        }
    }

    private fun actualizarBarra(bar: ProgressBar, tv: TextView, valor: Int, maximo: Int) {
        val progreso = if (maximo > 0) (valor * 100) / maximo else 0
        bar.progress = progreso
        tv.text = "${valor}x"
    }

    private fun mostrarSelectorPeriodo() {
        val opciones = arrayOf("Últimos 7 días", "Últimos 15 días", "Últimos 30 días")
        val valoresDias = intArrayOf(7, 15, 30)

        val seleccionActual = valoresDias.indexOf(diasSeleccionados).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar período")
            .setSingleChoiceItems(opciones, seleccionActual) { dialog, which ->
                diasSeleccionados = valoresDias[which]
                actualizarTextoPeriodo()
                dialog.dismiss()
                cargarResumen()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarTextoPeriodo() {
        val texto = when (diasSeleccionados) {
            7 -> "Últimos 7 días"
            15 -> "Últimos 15 días"
            30 -> "Último mes"
            else -> "Últimos $diasSeleccionados días"
        }
        tvPeriodoTexto.text = texto
    }

    private fun mostrarLoading(mostrar: Boolean) {
        progressReportes.visibility = if (mostrar) View.VISIBLE else View.GONE
        if (mostrar) {
            tvReportesError.visibility = View.GONE
            ocultarContenido()
        }
    }

    private fun mostrarError(mensaje: String) {
        tvReportesError.visibility = View.VISIBLE
        tvReportesError.text = mensaje
        ocultarContenido()
    }

    private fun ocultarContenido() {
        cardPeriodo.visibility = View.GONE
        cardSintomas.visibility = View.GONE
        cardMetricas.visibility = View.GONE
        btnVerPdf.visibility = View.GONE
        btnCompartir.visibility = View.GONE
    }

    private fun mostrarContenido() {
        tvReportesError.visibility = View.GONE
        cardPeriodo.visibility = View.VISIBLE
        cardSintomas.visibility = View.VISIBLE
        cardMetricas.visibility = View.VISIBLE
        btnVerPdf.visibility = View.VISIBLE
        btnCompartir.visibility = View.VISIBLE
    }

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }
}
