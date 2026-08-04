package com.beatwatch.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.ActualizarPacienteRequest
import com.beatwatch.app.data.model.PacientePerfilResponse
import com.beatwatch.app.data.repository.PacienteRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PerfilFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var pacienteRepository: PacienteRepository

    private lateinit var tvPerfilNombre: TextView
    private lateinit var tvPerfilRol: TextView
    private lateinit var tvPerfilEdad: TextView
    private lateinit var tvPerfilSexo: TextView
    private lateinit var tvPerfilPeso: TextView
    private lateinit var tvPerfilEstatura: TextView
    private lateinit var tvPerfilFechaNacimiento: TextView
    private lateinit var tvPerfilTipoSangre: TextView
    private lateinit var tvPerfilTelefono: TextView
    private lateinit var tvPerfilCorreo: TextView
    private lateinit var tvPerfilDireccion: TextView
    private lateinit var tvEditarPerfil: TextView
    private lateinit var btnCerrarSesion: AppCompatButton

    private var pacientePerfilActual: PacientePerfilResponse? = null

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
        tvPerfilSexo = view.findViewById(R.id.tvPerfilSexo)
        tvPerfilPeso = view.findViewById(R.id.tvPerfilPeso)
        tvPerfilEstatura = view.findViewById(R.id.tvPerfilEstatura)
        tvPerfilFechaNacimiento = view.findViewById(R.id.tvPerfilFechaNacimiento)
        tvPerfilTipoSangre = view.findViewById(R.id.tvPerfilTipoSangre)
        tvPerfilTelefono = view.findViewById(R.id.tvPerfilTelefono)
        tvPerfilCorreo = view.findViewById(R.id.tvPerfilCorreo)
        tvPerfilDireccion = view.findViewById(R.id.tvPerfilDireccion)
        tvEditarPerfil = view.findViewById(R.id.tvEditarPerfil)
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
        tvPerfilTelefono.text = telefono.ifBlank { "No disponible" }
        tvPerfilCorreo.text = correo.ifBlank { "No disponible" }
        tvPerfilEdad.text = "\u2014"
        tvPerfilSexo.text = "\u2014"
        tvPerfilPeso.text = "\u2014"
        tvPerfilEstatura.text = "\u2014"
        tvPerfilFechaNacimiento.text = "\u2014"
        tvPerfilTipoSangre.text = "\u2014"
        tvPerfilDireccion.text = "\u2014"

        val jwt = sessionManager.getToken()
        val usuarioId = sessionManager.getUsuarioId()

        if (jwt.isBlank() || usuarioId.isBlank()) {
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("PERFIL_API", "GET api/Pacientes/usuario/$usuarioId")
                Log.d("PERFIL_API", "JWT existe: ${jwt.isNotBlank()}")

                val response = pacienteRepository.obtenerPacientePorUsuarioId(jwt, usuarioId)

                Log.d("PERFIL_API", "HTTP code GET: ${response.code()}")
                Log.d("PERFIL_API", "Body GET: ${response.body()}")

                if (response.isSuccessful) {
                    val paciente = response.body()
                    pacientePerfilActual = paciente

                    val pacienteId = paciente?.pacienteId.orEmpty()
                    if (pacienteId.isNotBlank()) {
                        sessionManager.guardarPacienteId(pacienteId)
                    }

                    actualizarUI(paciente)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PERFIL_API", "ErrorBody GET: $errorBody")

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
                Log.e("PERFIL_API", "Error de conexión", e)
                Toast.makeText(requireContext(), "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error inesperado", e)
            }
        }
    }

    private fun actualizarUI(paciente: PacientePerfilResponse?) {
        if (paciente == null) return

        val edad = paciente.edad
        tvPerfilEdad.text = if (edad != null) "$edad años" else "--"

        tvPerfilSexo.text = (paciente.sexo ?: "").ifBlank { "--" }

        val peso = paciente.peso
        tvPerfilPeso.text = if (peso != null) "$peso kg" else "--"

        val estatura = paciente.estatura
        tvPerfilEstatura.text = if (estatura != null) "$estatura cm" else "--"

        tvPerfilFechaNacimiento.text = parseFechaNacimientoVisual(paciente.fechaNacimiento).ifBlank { "--" }

        tvPerfilTipoSangre.text = (paciente.tipoSangre ?: "").ifBlank { "--" }

        tvPerfilDireccion.text = (paciente.direccion ?: "").ifBlank { "No disponible" }
    }

    private fun configurarListeners() {
        tvEditarPerfil.setOnClickListener {
            if (pacientePerfilActual == null) {
                Toast.makeText(requireContext(), "Espera a que se cargue la información del perfil.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mostrarDialogoEditarPerfil()
        }

        btnCerrarSesion.setOnClickListener {
            sessionManager.cerrarSesion()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun mostrarDialogoEditarPerfil() {
        val paciente = pacientePerfilActual ?: return
        val context = requireContext()

        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etEdad = createEditText(context, InputType.TYPE_CLASS_NUMBER, paciente.edad?.toString() ?: "")
        val spSexo = createSpinnerSexo(context, paciente.sexo)
        val etPeso = createEditText(context, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL, paciente.peso?.toString() ?: "")
        val etEstatura = createEditText(context, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL, paciente.estatura?.toString() ?: "")
        val spTipoSangre = createSpinnerTipoSangre(context, paciente.tipoSangre)
        val etDireccion = createEditText(context, InputType.TYPE_CLASS_TEXT, paciente.direccion ?: "")
        val etFechaNacimiento = createEditText(context, InputType.TYPE_CLASS_TEXT, parseFechaNacimientoVisual(paciente.fechaNacimiento))
        etFechaNacimiento.isFocusable = false
        etFechaNacimiento.isClickable = true

        var fechaIsoSeleccionada = paciente.fechaNacimiento ?: ""
        var fechaVisualSeleccionada = parseFechaNacimientoVisual(paciente.fechaNacimiento)

        etFechaNacimiento.setOnClickListener {
            mostrarDatePicker { fechaIso, fechaVisual ->
                fechaIsoSeleccionada = fechaIso
                fechaVisualSeleccionada = fechaVisual
                etFechaNacimiento.setText(fechaVisual)
            }
        }

        layout.addView(createLabel(context, "Edad"))
        layout.addView(etEdad)
        layout.addView(createLabel(context, "Sexo"))
        layout.addView(spSexo)
        layout.addView(createLabel(context, "Peso (kg)"))
        layout.addView(etPeso)
        layout.addView(createLabel(context, "Estatura (cm)"))
        layout.addView(etEstatura)
        layout.addView(createLabel(context, "Tipo de sangre"))
        layout.addView(spTipoSangre)
        layout.addView(createLabel(context, "Dirección"))
        layout.addView(etDireccion)
        layout.addView(createLabel(context, "Fecha de nacimiento"))
        layout.addView(etFechaNacimiento)

        scrollView.addView(layout)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Editar perfil")
            .setView(scrollView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val edadTexto = etEdad.text.toString().trim()
            val sexo = spSexo.selectedItem?.toString() ?: ""
            val pesoTexto = etPeso.text.toString().trim()
            val estaturaTexto = etEstatura.text.toString().trim()
            val tipoSangre = spTipoSangre.selectedItem?.toString() ?: ""
            val direccion = etDireccion.text.toString().trim()
            val fechaNac = etFechaNacimiento.text.toString().trim()
            val sexoHint = getString(R.string.hint_sexo)
            val sangreHint = getString(R.string.hint_tipo_sangre)

            var esValido = true

            val edadNueva = if (edadTexto.isNotEmpty()) edadTexto.toIntOrNull() else null
            if (edadTexto.isNotEmpty() && (edadNueva == null || edadNueva <= 0 || edadNueva > 130)) {
                etEdad.error = "Edad inválida (1-130)"
                esValido = false
            }

            if (sexo == sexoHint) {
                Toast.makeText(context, "Selecciona el sexo", Toast.LENGTH_SHORT).show()
                esValido = false
            }

            val pesoNuevo = if (pesoTexto.isNotEmpty()) pesoTexto.toDoubleOrNull() else null
            if (pesoTexto.isNotEmpty() && (pesoNuevo == null || pesoNuevo <= 0)) {
                etPeso.error = "Peso inválido"
                esValido = false
            }

            val estaturaNueva = if (estaturaTexto.isNotEmpty()) estaturaTexto.toDoubleOrNull() else null
            if (estaturaTexto.isNotEmpty() && (estaturaNueva == null || estaturaNueva <= 0)) {
                etEstatura.error = "Estatura inválida"
                esValido = false
            }

            if (tipoSangre == sangreHint) {
                Toast.makeText(context, "Selecciona el tipo de sangre", Toast.LENGTH_SHORT).show()
                esValido = false
            }

            if (direccion.isNotEmpty() && direccion.length < 5) {
                etDireccion.error = "Mínimo 5 caracteres"
                esValido = false
            }

            if (fechaNac.isBlank()) {
                Toast.makeText(context, "Selecciona una fecha válida", Toast.LENGTH_SHORT).show()
                esValido = false
            }

            if (!esValido) return@setOnClickListener

            val sexoSeleccionado = if (sexo != sexoHint) sexo else null
            val sangreSeleccionada = if (tipoSangre != sangreHint) tipoSangre else null
            val direccionSeleccionada = direccion.ifBlank { null }

            val sexoActual = paciente.sexo
            val tipoSangreActual = paciente.tipoSangre
            val direccionActual = paciente.direccion
            val fechaActual = paciente.fechaNacimiento ?: ""

            val request = ActualizarPacienteRequest(
                edad = if (edadNueva != paciente.edad) edadNueva else null,
                sexo = if (sexoSeleccionado != null && sexoSeleccionado != sexoActual) sexoSeleccionado else null,
                peso = if (pesoNuevo != paciente.peso) pesoNuevo else null,
                estatura = if (estaturaNueva != paciente.estatura) estaturaNueva else null,
                fechaNacimiento = if (fechaIsoSeleccionada.isNotBlank() && fechaIsoSeleccionada != fechaActual) fechaIsoSeleccionada else null,
                direccion = if (direccionSeleccionada != null && direccionSeleccionada != direccionActual) direccionSeleccionada else null,
                tipoSangre = if (sangreSeleccionada != null && sangreSeleccionada != tipoSangreActual) sangreSeleccionada else null
            )

            if (request.edad == null && request.sexo == null && request.peso == null &&
                request.estatura == null && request.fechaNacimiento == null &&
                request.direccion == null && request.tipoSangre == null
            ) {
                Toast.makeText(context, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jwt = sessionManager.getToken()
            val usuarioId = sessionManager.getUsuarioId()

            if (jwt.isBlank() || usuarioId.isBlank()) {
                Toast.makeText(context, "Sesión no válida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    Log.d("PERFIL_API", "PATCH api/Pacientes/perfil/$usuarioId")
                    Log.d("PERFIL_API", "Request PATCH: $request")

                    val response = pacienteRepository.actualizarPerfilPaciente(jwt, usuarioId, request)

                    Log.d("PERFIL_API", "HTTP code PATCH: ${response.code()}")
                    Log.d("PERFIL_API", "isSuccessful PATCH: ${response.isSuccessful}")

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarDatosPerfil()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("PERFIL_API", "ErrorBody PATCH: $errorBody")

                        val mensaje = when (response.code()) {
                            400 -> "Datos inválidos. Verifica la información."
                            401 -> {
                                sessionManager.cerrarSesion()
                                redirigirLogin()
                                "Sesión expirada. Inicia sesión nuevamente."
                            }
                            404 -> "Perfil no encontrado."
                            in 500..599 -> "Error del servidor. Intenta más tarde."
                            else -> "Error inesperado: ${response.code()}"
                        }
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    Log.e("PERFIL_API", "Error de conexión", e)
                    Toast.makeText(context, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("PERFIL_API", "Error inesperado", e)
                    Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDatePicker(onDateSelected: (fechaIso: String, fechaVisual: String) -> Unit) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                selectedCalendar.set(Calendar.MINUTE, 0)
                selectedCalendar.set(Calendar.SECOND, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)

                val formatoVisual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaVisual = formatoVisual.format(selectedCalendar.time)

                val formatoApi = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                formatoApi.timeZone = TimeZone.getTimeZone("UTC")
                val fechaIso = formatoApi.format(selectedCalendar.time)

                onDateSelected(fechaIso, fechaVisual)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun parseFechaNacimientoVisual(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val cleanIso = iso.substringBefore(".").substringBefore("Z")
            val date = inputFormat.parse(cleanIso)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun createEditText(context: android.content.Context, inputType: Int, value: String): EditText {
        return EditText(context).apply {
            setBackgroundResource(R.drawable.bg_edit_text)
            this.inputType = inputType
            setText(value)
            setPadding(24, 16, 24, 16)
            textSize = 14f
        }
    }

    private fun createLabel(context: android.content.Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(0xFF6B7A90.toInt())
            textSize = 13f
            setPadding(4, 16, 4, 8)
        }
    }

    private fun createSpinnerSexo(context: android.content.Context, valorActual: String?): Spinner {
        val items = mutableListOf(getString(R.string.hint_sexo))
        items.addAll(resources.getStringArray(R.array.sexo_opciones).toList())

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = Spinner(context)
        spinner.adapter = adapter

        val index = items.indexOfFirst { it.equals(valorActual, ignoreCase = true) }
        spinner.setSelection(if (index >= 0) index else 0)

        return spinner
    }

    private fun createSpinnerTipoSangre(context: android.content.Context, valorActual: String?): Spinner {
        val items = mutableListOf(getString(R.string.hint_tipo_sangre))
        items.addAll(resources.getStringArray(R.array.tipo_sangre_opciones).toList())

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = Spinner(context)
        spinner.adapter = adapter

        val index = items.indexOfFirst { it.equals(valorActual, ignoreCase = true) }
        spinner.setSelection(if (index >= 0) index else 0)

        return spinner
    }

    private fun redirigirLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }
}
