package com.beatwatch.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.ReporteResumenResponse
import com.beatwatch.app.data.repository.ReportesRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

class ReportesFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private val reportesRepository = ReportesRepository()
    private var diasSeleccionados = 7
    private lateinit var progress: ProgressBar
    private lateinit var error: TextView
    private lateinit var content: View
    private lateinit var periodo: TextView
    private lateinit var bpmPromedio: TextView
    private lateinit var pasos: TextView
    private lateinit var arritmias: TextView
    private lateinit var sueno: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) =
        inflater.inflate(R.layout.fragment_reportes, container, false)

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        sessionManager = SessionManager.getInstance(requireContext())
        progress = view.findViewById(R.id.progressReportes); error = view.findViewById(R.id.tvReportesError)
        content = view.findViewById(R.id.reportesContent); periodo = view.findViewById(R.id.tvPeriodoTexto)
        bpmPromedio = view.findViewById(R.id.tvBpmPromedio); pasos = view.findViewById(R.id.tvPasos)
        arritmias = view.findViewById(R.id.tvArritmias); sueno = view.findViewById(R.id.tvSueno)
        view.findViewById<View>(R.id.tvCambiarPeriodo).setOnClickListener { seleccionarPeriodo() }
        actualizarPeriodo(); cargarReportes()
    }

    private fun cargarReportes() {
        val token = sessionManager.getToken(); val paciente = sessionManager.getPacienteId()
        if (token.isBlank() || paciente.isBlank()) { mostrarError("No se encontró una sesión o paciente válido."); return }
        progress.visibility = View.VISIBLE; content.visibility = View.GONE; error.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val resumenResponse = reportesRepository.obtenerResumenGraficas(token, paciente, diasSeleccionados)
                if (!resumenResponse.isSuccessful) { mostrarError("No fue posible cargar el resumen del reporte."); return@launch }
                pintar(resumenResponse.body())
                content.visibility = View.VISIBLE
            } catch (_: Exception) { mostrarError("No se pudo conectar con el servidor.") }
            finally { progress.visibility = View.GONE }
        }
    }

    private fun pintar(resumen: ReporteResumenResponse?) {
        bpmPromedio.text = formato(resumen?.promedioBPM, " BPM")
        pasos.text = "${resumen?.totalPasos ?: 0}"; arritmias.text = "${resumen?.totalArritmias ?: 0}"
        sueno.text = formato(resumen?.promedioHorasSueno, " h")
    }

    private fun formato(value: Double?, suffix: String) = String.format(Locale.US, "%.1f%s", value ?: 0.0, suffix)
    private fun seleccionarPeriodo() {
        val valores = intArrayOf(7, 15, 30)
        AlertDialog.Builder(requireContext()).setTitle("Seleccionar período").setSingleChoiceItems(arrayOf("Últimos 7 días", "Últimos 15 días", "Últimos 30 días"), valores.indexOf(diasSeleccionados)) { dialog, which -> diasSeleccionados = valores[which]; actualizarPeriodo(); dialog.dismiss(); cargarReportes() }.show()
    }
    private fun actualizarPeriodo() { periodo.text = "Últimos $diasSeleccionados días" }
    private fun mostrarError(message: String) { progress.visibility = View.GONE; content.visibility = View.GONE; error.text = message; error.visibility = View.VISIBLE }
}
