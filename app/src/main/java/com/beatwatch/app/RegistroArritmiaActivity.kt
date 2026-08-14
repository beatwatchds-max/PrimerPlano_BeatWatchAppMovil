package com.beatwatch.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.FactoresRiesgoRequest
import com.beatwatch.app.data.model.RegistroArritmiaRequest
import com.beatwatch.app.data.model.SintomasRequest
import com.beatwatch.app.data.repository.SaludRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class RegistroArritmiaActivity : AppCompatActivity() {

    private lateinit var spTipoArritmia: Spinner
    private lateinit var etFrecuenciaCardiaca: EditText
    private lateinit var etDuracionEpisodio: EditText
    private lateinit var cbMareo: CheckBox
    private lateinit var cbPalpitaciones: CheckBox
    private lateinit var cbDolorPecho: CheckBox
    private lateinit var cbDesmayo: CheckBox
    private lateinit var cbFaltaAire: CheckBox
    private lateinit var cbFatiga: CheckBox
    private lateinit var cbHipertension: CheckBox
    private lateinit var cbObesidad: CheckBox
    private lateinit var cbApnea: CheckBox
    private lateinit var cbTabaquismo: CheckBox
    private lateinit var cbAlcoholismo: CheckBox
    private lateinit var cbEstres: CheckBox
    private lateinit var btnContinuar: AppCompatButton
    private lateinit var sessionManager: SessionManager
    private lateinit var saludRepository: SaludRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_arritmia)

        sessionManager = SessionManager.getInstance(this)
        saludRepository = SaludRepository()

        inicializarVistas()
        configurarSpinner()
        configurarListener()
    }

    private fun inicializarVistas() {
        spTipoArritmia = findViewById(R.id.spTipoArritmia)
        etFrecuenciaCardiaca = findViewById(R.id.etFrecuenciaCardiaca)
        etDuracionEpisodio = findViewById(R.id.etDuracionEpisodio)
        cbMareo = findViewById(R.id.cbMareo)
        cbPalpitaciones = findViewById(R.id.cbPalpitaciones)
        cbDolorPecho = findViewById(R.id.cbDolorPecho)
        cbDesmayo = findViewById(R.id.cbDesmayo)
        cbFaltaAire = findViewById(R.id.cbFaltaAire)
        cbFatiga = findViewById(R.id.cbFatiga)
        cbHipertension = findViewById(R.id.cbHipertension)
        cbObesidad = findViewById(R.id.cbObesidad)
        cbApnea = findViewById(R.id.cbApnea)
        cbTabaquismo = findViewById(R.id.cbTabaquismo)
        cbAlcoholismo = findViewById(R.id.cbAlcoholismo)
        cbEstres = findViewById(R.id.cbEstres)
        btnContinuar = findViewById(R.id.btnContinuarArritmia)
    }

    private fun configurarSpinner() {
        val items = mutableListOf(getString(R.string.hint_arritmia))
        items.addAll(resources.getStringArray(R.array.tipo_arritmia_opciones).toList())

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipoArritmia.adapter = adapter
        spTipoArritmia.setSelection(0)
    }

    private fun configurarListener() {
        btnContinuar.setOnClickListener {
            validarYEnviar()
        }
    }

    private fun validarYEnviar() {
        var esValido = true

        if (spTipoArritmia.selectedItemPosition == 0) {
            Toast.makeText(this, "Selecciona el tipo de arritmia", Toast.LENGTH_SHORT).show()
            esValido = false
        }
        val tipoSeleccionado = spTipoArritmia.selectedItem?.toString() ?: ""

        val frecuenciaTexto = etFrecuenciaCardiaca.text.toString().trim()
        val frecuenciaCardiaca = frecuenciaTexto.toIntOrNull()
        if (frecuenciaTexto.isEmpty()) {
            etFrecuenciaCardiaca.error = "Ingresa la frecuencia cardíaca"
            esValido = false
        } else if (frecuenciaCardiaca == null || frecuenciaCardiaca <= 0) {
            etFrecuenciaCardiaca.error = "Ingresa un valor válido"
            esValido = false
        } else if (frecuenciaCardiaca > 300) {
            etFrecuenciaCardiaca.error = "Máximo 300 bpm"
            esValido = false
        }

        val duracionTexto = etDuracionEpisodio.text.toString().trim()
        val duracionSegundos = convertirMinutosASegundos(duracionTexto)
        if (duracionTexto.isEmpty()) {
            etDuracionEpisodio.error = "Ingresa la duración en minutos"
            esValido = false
        } else if (duracionSegundos == null || duracionSegundos <= 0) {
            etDuracionEpisodio.error = "Ingresa un valor válido en minutos"
            esValido = false
        }

        if (!esValido) {
            Toast.makeText(this, R.string.msg_campos_obligatorios, Toast.LENGTH_SHORT).show()
            return
        }

        val jwt = sessionManager.getToken()
        val idPaciente = sessionManager.getPacienteId()

        if (jwt.isBlank()) {
            Toast.makeText(
                this,
                "No se encontró sesión. Inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (idPaciente.isBlank()) {
            Toast.makeText(
                this,
                "No se encontró información del paciente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val request = RegistroArritmiaRequest(
            tipo = tipoSeleccionado,
            frecuenciaCardiaca = frecuenciaCardiaca!!,
            duracionEpisodioSeconds = duracionSegundos!!,
            idPaciente = idPaciente,
            sintomas = SintomasRequest(
                mareo = cbMareo.isChecked,
                palpitaciones = cbPalpitaciones.isChecked,
                dolorPecho = cbDolorPecho.isChecked,
                desmayo = cbDesmayo.isChecked,
                faltaAire = cbFaltaAire.isChecked,
                fatiga = cbFatiga.isChecked
            ),
            factoresRiesgo = FactoresRiesgoRequest(
                hipertensionArterial = cbHipertension.isChecked,
                obesidadImcElevado = cbObesidad.isChecked,
                apneaSueno = cbApnea.isChecked,
                tabaquismo = cbTabaquismo.isChecked,
                alcoholismo = cbAlcoholismo.isChecked,
                estresCronico = cbEstres.isChecked
            )
        )

        enviarRegistro(jwt, request)
    }

    private fun enviarRegistro(jwt: String, request: RegistroArritmiaRequest) {
        btnContinuar.isEnabled = false
        btnContinuar.text = "Guardando..."

        lifecycleScope.launch {
            try {
                Log.d("ARRITMIA_API", "Endpoint: api/salud/arritmia")

                val response = saludRepository.registrarArritmia(jwt, request)

                Log.d("ARRITMIA_API", "HTTP code: ${response.code()}")
                Log.d("ARRITMIA_API", "isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    sessionManager.guardarEstadoFormularios(
                        perfilCompletado = true,
                        diagnosticoCompletado = true
                    )

                    Toast.makeText(
                        this@RegistroArritmiaActivity,
                        "Información de arritmia registrada correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    if (sessionManager.isDispositivoVinculado()) {
                        startActivity(Intent(this@RegistroArritmiaActivity, MainActivity::class.java))
                    } else {
                        startActivity(Intent(this@RegistroArritmiaActivity, ConectarDispositivoActivity::class.java))
                    }
                    finish()
                } else {
                    Log.e("ARRITMIA_API", "Error HTTP ${response.code()}")

                    val mensaje = when (response.code()) {
                        400 -> "Datos inválidos. Verifica la información."
                        401 -> "Sesión expirada. Inicia sesión nuevamente."
                        404 -> "Endpoint no encontrado."
                        in 500..599 -> "Error del servidor. Intenta más tarde."
                        else -> "Error inesperado: ${response.code()}"
                    }
                    Toast.makeText(this@RegistroArritmiaActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("ARRITMIA_API", "Error de conexión", e)
                Toast.makeText(
                    this@RegistroArritmiaActivity,
                    "No se pudo conectar con el servidor",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("ARRITMIA_API", "Error inesperado", e)
                Toast.makeText(
                    this@RegistroArritmiaActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnContinuar.isEnabled = true
                btnContinuar.text = getString(R.string.registro_continuar)
            }
        }
    }

    private fun convertirMinutosASegundos(minutosTexto: String): Int? {
        return try {
            val minutos = minutosTexto.trim().toInt()
            if (minutos <= 0) null else minutos * 60
        } catch (e: Exception) {
            null
        }
    }
}
