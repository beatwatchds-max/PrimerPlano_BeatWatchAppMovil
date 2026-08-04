package com.beatwatch.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.RegistroPacienteRequest
import com.beatwatch.app.data.repository.PacienteRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class RegistroPacienteActivity : AppCompatActivity() {

    private lateinit var etCurp: EditText
    private lateinit var etEdad: EditText
    private lateinit var spSexo: Spinner
    private lateinit var etPeso: EditText
    private lateinit var etEstatura: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var etDireccion: EditText
    private lateinit var spTipoSangre: Spinner
    private lateinit var btnFoto: LinearLayout
    private lateinit var btnContinuar: AppCompatButton
    private lateinit var sessionManager: SessionManager
    private lateinit var pacienteRepository: PacienteRepository

    private var fechaNacimientoIso: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_paciente)

        sessionManager = SessionManager.getInstance(this)
        pacienteRepository = PacienteRepository()

        inicializarVistas()
        configurarSpinners()
        configurarListeners()
    }

    private fun inicializarVistas() {
        etCurp = findViewById(R.id.etCurp)
        etEdad = findViewById(R.id.etEdad)
        spSexo = findViewById(R.id.spSexo)
        etPeso = findViewById(R.id.etPeso)
        etEstatura = findViewById(R.id.etEstatura)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        etDireccion = findViewById(R.id.etDireccion)
        spTipoSangre = findViewById(R.id.spTipoSangre)
        btnFoto = findViewById(R.id.btnFoto)
        btnContinuar = findViewById(R.id.btnContinuar)
    }

    private fun configurarSpinners() {
        val sexoItems = mutableListOf(getString(R.string.hint_sexo))
        sexoItems.addAll(resources.getStringArray(R.array.sexo_opciones).toList())

        val sexoAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            sexoItems
        ) {
            override fun getCount(): Int {
                return super.getCount() - 1
            }
        }
        sexoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSexo.adapter = sexoAdapter
        spSexo.setSelection(0)

        val sangreItems = mutableListOf(getString(R.string.hint_tipo_sangre))
        sangreItems.addAll(resources.getStringArray(R.array.tipo_sangre_opciones).toList())

        val sangreAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            sangreItems
        ) {
            override fun getCount(): Int {
                return super.getCount() - 1
            }
        }
        sangreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipoSangre.adapter = sangreAdapter
        spTipoSangre.setSelection(0)
    }

    private fun configurarListeners() {
        etFechaNacimiento.setOnClickListener {
            mostrarDatePicker()
        }

        btnFoto.setOnClickListener {
            Toast.makeText(this, R.string.msg_foto_pendiente, Toast.LENGTH_SHORT).show()
        }

        btnContinuar.setOnClickListener {
            validarYEnviar()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
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
                etFechaNacimiento.setText(formatoVisual.format(selectedCalendar.time))

                val formatoApi = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                formatoApi.timeZone = TimeZone.getTimeZone("UTC")
                fechaNacimientoIso = formatoApi.format(selectedCalendar.time)

                Log.d("PACIENTE_API", "Fecha visual: ${etFechaNacimiento.text}")
                Log.d("PACIENTE_API", "Fecha ISO enviada: $fechaNacimientoIso")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun validarYEnviar() {
        var esValido = true
        var primerCampo: EditText? = null

        val curp = etCurp.text.toString().trim().uppercase()
        etCurp.setText(curp)
        if (curp.isEmpty()) {
            mostrarError(etCurp, R.string.error_token_empty)
            if (primerCampo == null) primerCampo = etCurp
            esValido = false
        } else if (curp.length != 18) {
            etCurp.error = "La CURP debe tener 18 caracteres"
            if (primerCampo == null) primerCampo = etCurp
            esValido = false
        }

        val edadTexto = etEdad.text.toString().trim()
        val edad = edadTexto.toIntOrNull()
        if (edadTexto.isEmpty()) {
            mostrarError(etEdad, R.string.error_token_empty)
            if (primerCampo == null) primerCampo = etEdad
            esValido = false
        } else if (edad == null || edad <= 0) {
            etEdad.error = "Ingresa una edad válida"
            if (primerCampo == null) primerCampo = etEdad
            esValido = false
        } else if (edad > 130) {
            etEdad.error = "La edad máxima es 130"
            if (primerCampo == null) primerCampo = etEdad
            esValido = false
        }

        if (spSexo.selectedItemPosition == 0) {
            Toast.makeText(this, "Selecciona el sexo", Toast.LENGTH_SHORT).show()
            if (primerCampo == null) primerCampo = etEdad
            esValido = false
        }
        val sexo = spSexo.selectedItem?.toString() ?: ""

        val pesoTexto = etPeso.text.toString().trim()
        val peso = pesoTexto.toDoubleOrNull()
        if (pesoTexto.isEmpty()) {
            mostrarError(etPeso, R.string.error_token_empty)
            if (primerCampo == null) primerCampo = etPeso
            esValido = false
        } else if (peso == null || peso <= 0) {
            etPeso.error = "Ingresa un peso válido"
            if (primerCampo == null) primerCampo = etPeso
            esValido = false
        }

        val estaturaTexto = etEstatura.text.toString().trim()
        val estatura = estaturaTexto.toDoubleOrNull()
        if (estaturaTexto.isEmpty()) {
            mostrarError(etEstatura, R.string.error_token_empty)
            if (primerCampo == null) primerCampo = etEstatura
            esValido = false
        } else if (estatura == null || estatura <= 0) {
            etEstatura.error = "Ingresa una estatura válida"
            if (primerCampo == null) primerCampo = etEstatura
            esValido = false
        }

        if (fechaNacimientoIso.isBlank()) {
            Toast.makeText(this, "Selecciona una fecha de nacimiento válida", Toast.LENGTH_LONG).show()
            if (primerCampo == null) primerCampo = etFechaNacimiento
            esValido = false
        }

        val direccion = etDireccion.text.toString().trim()
        if (direccion.isEmpty()) {
            mostrarError(etDireccion, R.string.error_token_empty)
            if (primerCampo == null) primerCampo = etDireccion
            esValido = false
        } else if (direccion.length < 5) {
            etDireccion.error = "La dirección debe tener al menos 5 caracteres"
            if (primerCampo == null) primerCampo = etDireccion
            esValido = false
        }

        if (spTipoSangre.selectedItemPosition == 0) {
            Toast.makeText(this, "Selecciona el tipo de sangre", Toast.LENGTH_SHORT).show()
            if (primerCampo == null) primerCampo = etEdad
            esValido = false
        }
        val tipoSangre = spTipoSangre.selectedItem?.toString() ?: ""

        if (!esValido) {
            Toast.makeText(this, R.string.msg_campos_obligatorios, Toast.LENGTH_SHORT).show()
            primerCampo?.requestFocus()
            return
        }

        val jwt = sessionManager.getToken()
        val usuarioId = sessionManager.getUsuarioId()
        val idLicencia = sessionManager.getIdLicencia()

        Log.d("SESSION_DEBUG", "RegistroPacienteActivity leyendo sesión")
        Log.d("SESSION_DEBUG", "jwt existe: ${jwt.isNotBlank()}")
        Log.d("SESSION_DEBUG", "usuarioId: $usuarioId")
        Log.d("SESSION_DEBUG", "idLicencia: $idLicencia")
        Log.d("SESSION_DEBUG", "isLoggedIn: ${sessionManager.isLoggedIn()}")

        if (jwt.isBlank() || usuarioId.isBlank() || idLicencia.isBlank()) {
            Toast.makeText(
                this,
                "No se encontró información de sesión. Inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val request = RegistroPacienteRequest(
            usuarioId = usuarioId,
            curp = curp,
            edad = edad!!,
            sexo = sexo,
            peso = peso!!,
            estatura = estatura!!,
            fechaNacimiento = fechaNacimientoIso,
            direccion = direccion,
            tipoSangre = tipoSangre,
            idLicencia = idLicencia,
            fotografia = null
        )

        enviarRegistro(jwt, request)
    }

    private fun enviarRegistro(jwt: String, request: RegistroPacienteRequest) {
        btnContinuar.isEnabled = false
        btnContinuar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                Log.d("PACIENTE_API", "Endpoint: api/Pacientes/perfil")
                Log.d("PACIENTE_API", "JWT existe: ${jwt.isNotBlank()}")
                Log.d("PACIENTE_API", "usuarioId: ${request.usuarioId}")
                Log.d("PACIENTE_API", "idLicencia: ${request.idLicencia}")
                Log.d("PACIENTE_API", "fechaNacimientoIso: ${request.fechaNacimiento}")
                Log.d("PACIENTE_API", "Request enviado: $request")

                val response = pacienteRepository.registrarPerfilPaciente(jwt, request)

                Log.d("PACIENTE_API", "HTTP code: ${response.code()}")
                Log.d("PACIENTE_API", "isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("PACIENTE_API", "Body: $body")

                    val pacienteId = body?.pacienteId
                        ?: body?.id
                        ?: body?._id
                        ?: ""

                    if (pacienteId.isNotBlank()) {
                        sessionManager.guardarPacienteId(pacienteId)
                        Log.d("SESSION_DEBUG", "pacienteId guardado: $pacienteId")
                    } else {
                        Log.e("SESSION_DEBUG", "No se recibió pacienteId en respuesta del perfil")
                    }

                    sessionManager.guardarEstadoFormularios(
                        perfilCompletado = true,
                        diagnosticoCompletado = false
                    )

                    Toast.makeText(
                        this@RegistroPacienteActivity,
                        "Perfil del paciente registrado correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(
                        this@RegistroPacienteActivity,
                        RegistroArritmiaActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PACIENTE_API", "ErrorBody: $errorBody")

                    val mensaje = when (response.code()) {
                        400 -> "Datos inválidos. Verifica la información."
                        401 -> "Sesión expirada. Inicia sesión nuevamente."
                        404 -> "Endpoint no encontrado."
                        in 500..599 -> "Error del servidor. Intenta más tarde."
                        else -> "Error del servidor: ${response.code()}"
                    }
                    Toast.makeText(this@RegistroPacienteActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("PACIENTE_API", "Error de conexión", e)
                Toast.makeText(
                    this@RegistroPacienteActivity,
                    R.string.error_connection,
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("PACIENTE_API", "Error inesperado", e)
                Toast.makeText(
                    this@RegistroPacienteActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnContinuar.isEnabled = true
                btnContinuar.text = getString(R.string.registro_continuar)
            }
        }
    }

    private fun mostrarError(editText: EditText, stringResId: Int) {
        editText.error = getString(stringResId)
    }
}
