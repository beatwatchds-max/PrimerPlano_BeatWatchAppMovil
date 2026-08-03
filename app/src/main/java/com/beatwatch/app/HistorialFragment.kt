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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.repository.HistorialRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistorialFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var historialRepository: HistorialRepository

    private lateinit var weeklyTrendView: WeeklyTrendView
    private lateinit var rvHistorial: RecyclerView
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

        sessionManager = SessionManager(requireContext())
        historialRepository = HistorialRepository()

        weeklyTrendView = view.findViewById(R.id.weeklyTrendView)
        rvHistorial = view.findViewById(R.id.rvHistorial)
        progressHistorial = view.findViewById(R.id.progressHistorial)
        tvHistorialVacio = view.findViewById(R.id.tvHistorialVacio)
        tvHistorialError = view.findViewById(R.id.tvHistorialError)

        rvHistorial.layoutManager = LinearLayoutManager(requireContext())

        if (!datosCargados) {
            cargarHistorial()
        }
    }

    private fun cargarHistorial() {
        val jwt = sessionManager.getToken()
        val idPaciente = sessionManager.getPacienteId()

        Log.d("HISTORIAL_API", "Endpoint: api/historial")
        Log.d("HISTORIAL_API", "Base URL usada: ${RetrofitClient.BASE_URL}")
        Log.d("HISTORIAL_API", "JWT existe: ${jwt.isNotBlank()}")
        Log.d("HISTORIAL_API", "idPaciente: $idPaciente")

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
                val response = historialRepository.obtenerHistorial(jwt, idPaciente)

                Log.d("HISTORIAL_API", "HTTP code: ${response.code()}")
                Log.d("HISTORIAL_API", "isSuccessful: ${response.isSuccessful}")
                Log.d("HISTORIAL_API", "Cantidad recibida backend: ${response.body()?.size}")
                Log.d("HISTORIAL_API", "Body: ${response.body()}")
                Log.d("HISTORIAL_API", "IDs recibidos: ${response.body()?.map { it.id }}")

                progressHistorial.visibility = View.GONE

                if (response.isSuccessful) {
                    val historialList = response.body().orEmpty()

                    Log.d("HISTORIAL_UI", "Cantidad enviada al adapter: ${historialList.size}")
                    Log.d("HISTORIAL_UI", "IDs enviados al adapter: ${historialList.map { it.id }}")

                    if (historialList.isEmpty()) {
                        tvHistorialVacio.visibility = View.VISIBLE
                        tvHistorialVacio.text = "Aún no hay registros en el historial."
                    } else {
                        tvHistorialVacio.visibility = View.GONE
                        rvHistorial.adapter = HistorialAdapter(historialList)
                        actualizarGrafica(historialList)
                    }

                    datosCargados = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("HISTORIAL_API", "ErrorBody: $errorBody")

                    when (response.code()) {
                        400 -> {
                            tvHistorialError.visibility = View.VISIBLE
                            tvHistorialError.text = "Solicitud inválida. Verifica el paciente."
                        }
                        401 -> {
                            Toast.makeText(requireContext(), "Sesión expirada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                            sessionManager.cerrarSesion()
                            redirigirLogin()
                        }
                        404 -> {
                            tvHistorialVacio.visibility = View.VISIBLE
                            tvHistorialVacio.text = "No hay historial disponible."
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
                Log.e("HISTORIAL_API", "Error de conexión", e)
                progressHistorial.visibility = View.GONE
                tvHistorialError.visibility = View.VISIBLE
                tvHistorialError.text = "No se pudo conectar con el servidor."
            } catch (e: Exception) {
                Log.e("HISTORIAL_API", "Error inesperado", e)
                progressHistorial.visibility = View.GONE
                tvHistorialError.visibility = View.VISIBLE
                tvHistorialError.text = "Error inesperado: ${e.message}"
            }
        }
    }

    private fun actualizarGrafica(historialList: List<com.beatwatch.app.data.model.HistorialResponse>) {
        val valoresPorDia = FloatArray(7) { 0f }
        val contadorPorDia = IntArray(7) { 0 }

        for (item in historialList) {
            val diaSemana = obtenerDiaSemana(item)
            if (diaSemana in 0..6) {
                val bpm = item.frecuenciaCardiaca ?: continue
                valoresPorDia[diaSemana] += bpm.toFloat()
                contadorPorDia[diaSemana]++
            }
        }

        val promedios = FloatArray(7) { index ->
            if (contadorPorDia[index] > 0) {
                valoresPorDia[index] / contadorPorDia[index]
            } else {
                0f
            }
        }

        weeklyTrendView.setData(promedios)
    }

    private fun obtenerDiaSemana(item: com.beatwatch.app.data.model.HistorialResponse): Int {
        val fechaRaw = item.fecha ?: return -1

        return try {
            val isoDate = fechaRaw.take(19).replace("T", " ")
            val formatos = arrayOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            )
            var date: java.util.Date? = null
            for (f in formatos) {
                try {
                    date = f.parse(isoDate)
                    if (date != null) break
                } catch (_: Exception) {}
            }
            if (date == null) return -1

            val calendar = Calendar.getInstance()
            calendar.time = date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> -1
            }
        } catch (e: Exception) {
            -1
        }
    }

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }
}
