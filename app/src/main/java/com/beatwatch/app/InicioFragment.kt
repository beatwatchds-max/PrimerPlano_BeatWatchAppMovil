package com.beatwatch.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beatwatch.app.data.model.ActualizarDispositivoRequest
import com.beatwatch.app.data.model.DispositivoResponse
import com.beatwatch.app.data.local.PulsacionLocal
import com.beatwatch.app.data.local.PulsacionesDatabase
import com.beatwatch.app.data.repository.DispositivoRepository
import com.beatwatch.app.data.repository.PacienteRepository
import com.beatwatch.app.data.repository.SaludRepository
import com.beatwatch.app.ui.adapters.DispositivoAdapter
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class InicioFragment : Fragment() {

    private lateinit var tvFrecuenciaCardiaca: TextView
    private lateinit var tvEstadoFrecuencia: TextView
    private lateinit var tvNombrePaciente: TextView
    private lateinit var tvDetallesPaciente: TextView
    private lateinit var tvDiagnosticoPaciente: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var pacienteRepository: PacienteRepository
    private lateinit var dispositivoRepository: DispositivoRepository
    private lateinit var saludRepository: SaludRepository
    private lateinit var pulsacionesDatabase: PulsacionesDatabase

    private lateinit var rvDispositivos: RecyclerView
    private lateinit var tvCargandoDispositivos: TextView
    private lateinit var emptyDispositivos: LinearLayout
    private lateinit var tvAgregarDispositivo: TextView
    private lateinit var btnAgregarDispositivoEmpty: View
    private lateinit var adapter: DispositivoAdapter
    private var actualizacionPulsacionesJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager.getInstance(requireContext())
        pacienteRepository = PacienteRepository()
        dispositivoRepository = DispositivoRepository()
        saludRepository = SaludRepository()
        pulsacionesDatabase = PulsacionesDatabase(requireContext())

        tvFrecuenciaCardiaca = view.findViewById(R.id.tvFrecuenciaCardiaca)
        tvEstadoFrecuencia = view.findViewById(R.id.tvEstadoFrecuencia)
        tvNombrePaciente = view.findViewById(R.id.tvNombrePaciente)
        tvDetallesPaciente = view.findViewById(R.id.tvDetallesPaciente)
        tvDiagnosticoPaciente = view.findViewById(R.id.tvDiagnosticoPaciente)

        rvDispositivos = view.findViewById(R.id.rvDispositivos)
        tvCargandoDispositivos = view.findViewById(R.id.tvCargandoDispositivos)
        emptyDispositivos = view.findViewById(R.id.emptyDispositivos)
        tvAgregarDispositivo = view.findViewById(R.id.tvAgregarDispositivo)
        btnAgregarDispositivoEmpty = view.findViewById(R.id.btnAgregarDispositivoEmpty)

        adapter = DispositivoAdapter(
            mutableListOf(),
            onEditarAlias = { dispositivo -> mostrarDialogoEditarAlias(dispositivo) },
            onEliminarDispositivo = { dispositivo -> mostrarDialogoEliminarDispositivo(dispositivo) }
        )

        rvDispositivos.layoutManager = LinearLayoutManager(requireContext())
        rvDispositivos.adapter = adapter

        cargarDatosPaciente()
        if (sessionManager.getPacienteId().isNotBlank()) {
            mostrarPulsacionLocal(sessionManager.getPacienteId())
            cargarDispositivos()
        }
        configurarCardsRapidas(view)
        configurarListenersDispositivos()
    }

    override fun onResume() {
        super.onResume()
        iniciarActualizacionPulsaciones()
    }

    override fun onPause() {
        actualizacionPulsacionesJob?.cancel()
        super.onPause()
    }

    private fun iniciarActualizacionPulsaciones() {
        actualizacionPulsacionesJob?.cancel()
        actualizacionPulsacionesJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isAdded) {
                cargarPrimerPulso()
                delay(INTERVALO_ACTUALIZACION_PULSACIONES_MS)
            }
        }
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
                val response = pacienteRepository.obtenerPacientePorUsuarioId(jwt, usuarioId)

                if (response.isSuccessful) {
                    val paciente = response.body()

                    val pacienteId = paciente?.pacienteId
                        ?: paciente?.id
                        ?: paciente?._id
                        ?: ""
                    if (pacienteId.isNotBlank()) {
                        sessionManager.guardarPacienteId(pacienteId)
                        mostrarPulsacionLocal(pacienteId)
                        cargarDispositivos()
                        cargarPrimerPulso()
                    }

                    val nombreMostrar = nombre.ifBlank { "Paciente" }
                    tvNombrePaciente.text = nombreMostrar

                    val edad = paciente?.edad
                    val tipoSangre = paciente?.tipoSangre.orEmpty()

                    val edadTexto = if (edad != null) "$edad años" else "-- años"
                    val sangreTexto = if (tipoSangre.isNotBlank()) "Tipo $tipoSangre" else "Tipo --"

                    tvDetallesPaciente.text = "$edadTexto · $sangreTexto"
                    tvDiagnosticoPaciente.text = "Diagnóstico no disponible"
                } else {
                    when (response.code()) {
                        401 -> {
                            Toast.makeText(requireContext(), "No se pudo autorizar la consulta. Intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }
                        404 -> {
                            Toast.makeText(requireContext(), "No se encontró perfil del paciente.", Toast.LENGTH_LONG).show()
                        }
                        in 500..599 -> {
                            Toast.makeText(requireContext(), "Error del servidor. Intenta más tarde.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (_: IOException) {
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun cargarPrimerPulso() {
        val pacienteId = sessionManager.getPacienteId()
        val jwt = sessionManager.getToken()
        if (pacienteId.isBlank() || jwt.isBlank()) return

        try {
            val response = saludRepository.obtenerUltimaMedicion(jwt, pacienteId)
            if (!response.isSuccessful) {
                when (response.code()) {
                    401 -> cerrarSesionYRedirigir()
                    403 -> mostrarEstadoPulsacion("No tienes permiso para consultar este paciente.")
                    else -> cargarPulsoDesdeFirebase()
                }
                return
            }

            val ultimaMedicion = response.body() ?: return

            guardarYPintarPulsacion(ultimaMedicion)
        } catch (_: IOException) {
            cargarPulsoDesdeFirebase()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            cargarPulsoDesdeFirebase()
        }
    }

    private suspend fun cargarPulsoDesdeFirebase() {
        try {
            val response = saludRepository.obtenerUltimaMedicionFirebase()
            val medicion = response.body()
            if (response.isSuccessful && medicion?.frecuenciaCardiacaBpm != null) {
                guardarYPintarPulsacion(medicion)
            } else {
                mostrarEstadoPulsacion("No se pudo cargar la medición.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: IOException) {
            mostrarEstadoPulsacion("No se pudo cargar la medición.")
        }
    }

    private fun mostrarPulsacionLocal(pacienteId: String) {
        pulsacionesDatabase.obtenerUltimaPulsacion(pacienteId)?.let(::pintarPulsacion)
    }

    private fun guardarYPintarPulsacion(medicion: com.beatwatch.app.data.model.MedicionResponse) {
        val frecuencia = medicion.frecuenciaCardiacaBpm ?: return
        val pulsacion = PulsacionLocal(
            frecuenciaCardiacaBpm = frecuencia,
            saturacionOxigenoSpO2 = medicion.saturacionOxigenoSpO2,
            timestamp = medicion.timestamp
        )
        sessionManager.getPacienteId().takeIf { it.isNotBlank() }?.let {
            pulsacionesDatabase.guardarUltimaPulsacion(it, pulsacion)
        }
        pintarPulsacion(pulsacion)
    }

    private fun pintarPulsacion(pulsacion: PulsacionLocal) {
        tvFrecuenciaCardiaca.text = pulsacion.frecuenciaCardiacaBpm.toString()
        tvEstadoFrecuencia.text = pulsacion.saturacionOxigenoSpO2?.let {
            "${getString(R.string.dashboard_pulse_registered)} · Oxígeno $it%"
        } ?: getString(R.string.dashboard_pulse_registered)
    }

    private fun mostrarEstadoPulsacion(mensaje: String) {
        tvFrecuenciaCardiaca.text = "--"
        tvEstadoFrecuencia.text = mensaje
    }

    private fun cerrarSesionYRedirigir() {
        actualizacionPulsacionesJob?.cancel()
        sessionManager.cerrarSesion()
        startActivity(
            Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        activity?.finish()
    }

    private companion object {
        const val INTERVALO_ACTUALIZACION_PULSACIONES_MS = 5_000L
    }

    private fun cargarDispositivos() {
        val jwt = sessionManager.getToken()
        val pacienteId = sessionManager.getPacienteId()

        if (jwt.isBlank()) {
            tvCargandoDispositivos.text = "Sesión inválida."
            return
        }

        if (pacienteId.isBlank()) {
            tvCargandoDispositivos.text = "No se encontró el paciente en sesión."
            return
        }

        lifecycleScope.launch {
            try {
                val response = dispositivoRepository.obtenerDispositivos(jwt, pacienteId)

                if (response.isSuccessful) {
                    val body = response.body().orEmpty()
                    val propios = body.filter { it.idPaciente == pacienteId }

                    adapter.actualizarLista(propios)

                    if (propios.isEmpty()) {
                        rvDispositivos.visibility = View.GONE
                        emptyDispositivos.visibility = View.VISIBLE
                    } else {
                        rvDispositivos.visibility = View.VISIBLE
                        emptyDispositivos.visibility = View.GONE
                    }
                } else {
                    tvCargandoDispositivos.text = "No se pudieron cargar los dispositivos."
                    rvDispositivos.visibility = View.GONE
                    emptyDispositivos.visibility = View.VISIBLE
                }
            } catch (_: IOException) {
                tvCargandoDispositivos.text = "No se pudieron cargar los dispositivos."
            } catch (_: Exception) {
                tvCargandoDispositivos.text = "Error al cargar dispositivos."
            } finally {
                tvCargandoDispositivos.visibility = View.GONE
            }
        }
    }

    private fun configurarListenersDispositivos() {
        tvAgregarDispositivo.setOnClickListener {
            startActivity(Intent(requireContext(), ConectarDispositivoActivity::class.java))
        }

        btnAgregarDispositivoEmpty.setOnClickListener {
            startActivity(Intent(requireContext(), ConectarDispositivoActivity::class.java))
        }
    }

    private fun mostrarDialogoEditarAlias(dispositivo: DispositivoResponse) {
        val context = requireContext()
        val id = dispositivo.id ?: dispositivo.dispositivoId

        if (id.isNullOrBlank()) {
            Toast.makeText(context, "No se encontró el id del dispositivo.", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etAlias = EditText(context).apply {
            setBackgroundResource(R.drawable.bg_edit_text)
            inputType = InputType.TYPE_CLASS_TEXT
            setText(dispositivo.alias ?: "")
            hint = "Nuevo alias"
            setPadding(24, 16, 24, 16)
            textSize = 14f
        }

        layout.addView(etAlias)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Editar alias")
            .setView(layout)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nuevoAlias = etAlias.text.toString().trim()

            if (nuevoAlias.isEmpty()) {
                etAlias.error = "Alias requerido"
                return@setOnClickListener
            }

            if (nuevoAlias == dispositivo.alias) {
                Toast.makeText(context, "El alias es el mismo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jwt = sessionManager.getToken()
            if (jwt.isBlank()) {
                Toast.makeText(context, "Sesión inválida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val request = ActualizarDispositivoRequest(
                numeroSerie = dispositivo.numeroSerie,
                alias = nuevoAlias,
                tipoDispositivo = dispositivo.tipoDispositivo,
                codigoModelo = dispositivo.codigoModelo,
                codigoDispositivo = dispositivo.codigoDispositivo,
                sistemaOperativo = dispositivo.sistemaOperativo,
                idPaciente = dispositivo.idPaciente ?: sessionManager.getPacienteId()
            )

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardando..."

            lifecycleScope.launch {
                try {
                    val response = dispositivoRepository.actualizarDispositivo(jwt, id, request)

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Alias actualizado correctamente.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarDispositivos()
                    } else {
                        val mensaje = when (response.code()) {
                            400 -> "Datos inválidos. Verifica la información."
                            401 -> {
                                "No se pudo autorizar la actualización. Intenta nuevamente."
                            }
                            404 -> "Dispositivo no encontrado."
                            in 500..599 -> "Error del servidor. Intenta más tarde."
                            else -> "No se pudo actualizar el dispositivo. Intenta más tarde."
                        }
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                    }
                } catch (_: IOException) {
                    Toast.makeText(context, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                } catch (_: Exception) {
                    Toast.makeText(context, "No se pudo actualizar el dispositivo. Intenta más tarde.", Toast.LENGTH_LONG).show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                }
            }
        }
    }

    private fun mostrarDialogoEliminarDispositivo(dispositivo: DispositivoResponse) {
        val context = requireContext()
        val id = dispositivo.id ?: dispositivo.dispositivoId

        if (id.isNullOrBlank()) {
            Toast.makeText(context, "No se encontró el id del dispositivo.", Toast.LENGTH_SHORT).show()
            return
        }

        val jwt = sessionManager.getToken()
        if (jwt.isBlank()) {
            Toast.makeText(context, "Sesión inválida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(context)
            .setTitle("Eliminar dispositivo")
            .setMessage("¿Deseas eliminar este dispositivo?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarDispositivo(jwt, id, dispositivo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDispositivo(jwt: String, id: String, dispositivo: DispositivoResponse) {
        val context = requireContext()

        lifecycleScope.launch {
            try {
                val response = dispositivoRepository.eliminarDispositivo(jwt, id)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Dispositivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarDispositivos()
                } else {
                    val mensaje = when (response.code()) {
                            401 -> {
                                "No se pudo autorizar la eliminación. Intenta nuevamente."
                        }
                        404 -> "Dispositivo no encontrado."
                        in 500..599 -> "Error del servidor. Intenta más tarde."
                        else -> "No se pudo eliminar el dispositivo. Intenta más tarde."
                    }
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (_: IOException) {
                Toast.makeText(context, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo eliminar el dispositivo. Intenta más tarde.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }

    private fun configurarCardsRapidas(view: View) {
        view.findViewById<View>(R.id.cardHistorial).setOnClickListener {
            val activity = activity as? MainActivity
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_historial
        }

        val cardReportes = view.findViewById<View>(R.id.cardReportes)
        if (sessionManager.getRol().equals("Paciente", ignoreCase = true)) {
            cardReportes.visibility = View.GONE
            return
        }

        cardReportes.setOnClickListener {
            val activity = activity as? MainActivity
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_reportes
        }
    }
}
