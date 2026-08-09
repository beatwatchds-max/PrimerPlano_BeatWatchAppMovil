package com.beatwatch.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beatwatch.app.data.model.ActualizarDispositivoRequest
import com.beatwatch.app.data.model.DispositivoResponse
import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import com.beatwatch.app.data.model.QrDevicePayload
import com.beatwatch.app.data.repository.DispositivoRepository
import com.beatwatch.app.ui.adapters.DispositivoAdapter
import com.beatwatch.app.utils.SessionManager
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.IOException

class ConectarDispositivoActivity : AppCompatActivity() {

    private lateinit var btnEscanearQR: AppCompatButton
    private lateinit var btnOmitir: AppCompatButton
    private lateinit var rvDispositivos: RecyclerView
    private lateinit var tvCargandoDispositivos: TextView
    private lateinit var emptyDispositivos: LinearLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var dispositivoRepository: DispositivoRepository
    private lateinit var adapter: DispositivoAdapter

    private val gson = Gson()

    private val qrLauncher = registerForActivityResult(
        ScanContract()
    ) { result ->
        if (result.contents != null) {
            procesarCodigoQR(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conectar_dispositivo)

        sessionManager = SessionManager.getInstance(this)
        dispositivoRepository = DispositivoRepository()

        btnEscanearQR = findViewById(R.id.btnEscanearQR)
        btnOmitir = findViewById(R.id.btnOmitir)
        rvDispositivos = findViewById(R.id.rvDispositivos)
        tvCargandoDispositivos = findViewById(R.id.tvCargandoDispositivos)
        emptyDispositivos = findViewById(R.id.emptyDispositivos)

        adapter = DispositivoAdapter(
            mutableListOf(),
            onEditarAlias = { dispositivo -> mostrarDialogoEditarAlias(dispositivo) },
            onEliminarDispositivo = { dispositivo -> mostrarDialogoEliminarDispositivo(dispositivo) }
        )

        rvDispositivos.layoutManager = LinearLayoutManager(this)
        rvDispositivos.adapter = adapter

        btnEscanearQR.setOnClickListener {
            iniciarEscaneoQR()
        }

        btnOmitir.setOnClickListener {
            sessionManager.guardarDispositivoVinculado(false)
            Log.d("DISPOSITIVO_FLOW", "Dispositivo omitido. dispositivoVinculado: ${sessionManager.isDispositivoVinculado()}")
            Toast.makeText(this, "Puedes conectar tu dispositivo más tarde", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        cargarDispositivos()
    }

    private fun iniciarEscaneoQR() {
        val options = ScanOptions().apply {
            setPrompt("Apunta al código QR de tu reloj")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        }
        qrLauncher.launch(options)
    }

    private fun procesarCodigoQR(contenido: String) {
        Log.d("DEVICE_QR", "QR leído: $contenido")

        try {
            val payload = gson.fromJson(contenido, QrDevicePayload::class.java)

            val idSesion = payload.idSesion.orEmpty()
            val tokenEmparejamiento = payload.tokenEmparejamiento.orEmpty()
            if (idSesion.isBlank() || tokenEmparejamiento.isBlank()) {
                Toast.makeText(this, "QR inválido: faltan datos de emparejamiento", Toast.LENGTH_LONG).show()
                return
            }

            val jwt = sessionManager.getToken()
            if (jwt.isBlank()) {
                Toast.makeText(this, "Sesión inválida. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
                sessionManager.cerrarSesion()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }

            val pacienteId = sessionManager.getPacienteId()
            if (pacienteId.isBlank()) {
                Toast.makeText(this, "No se encontró información del paciente.", Toast.LENGTH_LONG).show()
                return
            }

            val request = EmparejarDispositivoRequest(
                idSesion = idSesion,
                tokenEmparejamiento = tokenEmparejamiento,
                idPaciente = pacienteId,
                alias = payload.alias?.trim().orEmpty().ifBlank { "Mi reloj" }
            )

            emparejarPorQR(jwt, request)

        } catch (e: Exception) {
            Log.e("DEVICE_QR", "Error al parsear QR: ${e.message}")
            Toast.makeText(this, "QR inválido. El código no contiene datos válidos del dispositivo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun emparejarPorQR(jwt: String, request: EmparejarDispositivoRequest) {
        Toast.makeText(this, "Emparejando dispositivo...", Toast.LENGTH_SHORT).show()

        Log.d("DEVICE_PAIR", "POST api/Dispositivos/emparejar")

        lifecycleScope.launch {
            try {
                val response = dispositivoRepository.emparejarDispositivo(jwt, request)

                Log.d("DEVICE_PAIR_RESPONSE", "HTTP code: ${response.code()}")
                Log.d("DEVICE_PAIR_RESPONSE", "isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    sessionManager.guardarDispositivoVinculado(true)
                    Log.d("DISPOSITIVO_FLOW", "Dispositivo vinculado guardado: ${sessionManager.isDispositivoVinculado()}")

                    Toast.makeText(this@ConectarDispositivoActivity, "Dispositivo vinculado correctamente", Toast.LENGTH_LONG).show()

                    cargarDispositivos()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DEVICE_PAIR_ERROR", "ErrorBody: $errorBody")

                    val mensajeBackend = try {
                        val errorJson = gson.fromJson(errorBody, Map::class.java)
                        errorJson["message"] as? String ?: errorJson["mensaje"] as? String
                    } catch (e: Exception) {
                        null
                    }

                    val mensaje = mensajeBackend
                        ?: "No fue posible vincular el dispositivo"
                    Toast.makeText(this@ConectarDispositivoActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("DEVICE_PAIR_ERROR", "Error de conexión", e)
                Toast.makeText(this@ConectarDispositivoActivity, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("DEVICE_PAIR_ERROR", "Error inesperado", e)
                Toast.makeText(this@ConectarDispositivoActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarDispositivos() {
        val jwt = sessionManager.getToken()
        val pacienteId = sessionManager.getPacienteId()

        if (jwt.isBlank() || pacienteId.isBlank()) {
            tvCargandoDispositivos.text = "Sesión inválida."
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("DISPOSITIVOS_GET", "GET api/Dispositivos iniciado")

                val response = dispositivoRepository.obtenerDispositivos(jwt)

                Log.d("DISPOSITIVOS_GET", "HTTP code: ${response.code()}")
                Log.d("DISPOSITIVOS_GET", "cantidad recibida: ${response.body()?.size ?: 0}")

                if (response.isSuccessful) {
                    val body = response.body().orEmpty()
                    val propios = body.filter { it.idPaciente == pacienteId }

                    Log.d("DISPOSITIVOS_GET", "cantidad filtrada por paciente: ${propios.size}")

                    adapter.actualizarLista(propios)

                    if (propios.isEmpty()) {
                        rvDispositivos.visibility = View.GONE
                        emptyDispositivos.visibility = View.VISIBLE
                    } else {
                        rvDispositivos.visibility = View.VISIBLE
                        emptyDispositivos.visibility = View.GONE
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DISPOSITIVOS_GET", "ErrorBody: $errorBody")

                    tvCargandoDispositivos.text = "No se pudieron cargar los dispositivos."
                }
            } catch (e: IOException) {
                Log.e("DISPOSITIVOS_GET", "Error de conexión", e)
                tvCargandoDispositivos.text = "No se pudieron cargar los dispositivos."
            } catch (e: Exception) {
                Log.e("DISPOSITIVOS_GET", "Error inesperado", e)
                tvCargandoDispositivos.text = "Error al cargar dispositivos."
            } finally {
                tvCargandoDispositivos.visibility = View.GONE
            }
        }
    }

    private fun mostrarDialogoEditarAlias(dispositivo: DispositivoResponse) {
        val id = dispositivo.id ?: dispositivo.dispositivoId

        if (id.isNullOrBlank()) {
            Toast.makeText(this, "No se encontró el id del dispositivo.", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etAlias = EditText(this).apply {
            setBackgroundResource(R.drawable.bg_edit_text)
            inputType = InputType.TYPE_CLASS_TEXT
            setText(dispositivo.alias ?: "")
            hint = "Nuevo alias"
            setPadding(24, 16, 24, 16)
            textSize = 14f
        }

        layout.addView(etAlias)

        val dialog = AlertDialog.Builder(this)
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
                Toast.makeText(this, "El alias es el mismo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jwt = sessionManager.getToken()
            if (jwt.isBlank()) {
                Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_LONG).show()
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
                    Log.d("DISPOSITIVOS_PUT", "PUT api/Dispositivos/$id")
                    Log.d("DISPOSITIVOS_PUT", "alias anterior: ${dispositivo.alias}")
                    Log.d("DISPOSITIVOS_PUT", "alias nuevo: $nuevoAlias")

                    val response = dispositivoRepository.actualizarDispositivo(jwt, id, request)

                    Log.d("DISPOSITIVOS_PUT", "HTTP code: ${response.code()}")

                    if (response.isSuccessful) {
                        Toast.makeText(this@ConectarDispositivoActivity, "Alias actualizado correctamente.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarDispositivos()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("DISPOSITIVOS_PUT", "ErrorBody: $errorBody")
                        Toast.makeText(this@ConectarDispositivoActivity, "No se pudo actualizar el alias.", Toast.LENGTH_LONG).show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                    }
                } catch (e: IOException) {
                    Log.e("DISPOSITIVOS_PUT", "Error de conexión", e)
                    Toast.makeText(this@ConectarDispositivoActivity, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                } catch (e: Exception) {
                    Log.e("DISPOSITIVOS_PUT", "Error inesperado", e)
                    Toast.makeText(this@ConectarDispositivoActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                }
            }
        }
    }

    private fun mostrarDialogoEliminarDispositivo(dispositivo: DispositivoResponse) {
        val id = dispositivo.id ?: dispositivo.dispositivoId

        if (id.isNullOrBlank()) {
            Toast.makeText(this, "No se encontró el id del dispositivo.", Toast.LENGTH_SHORT).show()
            return
        }

        val jwt = sessionManager.getToken()
        if (jwt.isBlank()) {
            Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar dispositivo")
            .setMessage("¿Deseas eliminar este dispositivo?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarDispositivo(jwt, id, dispositivo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDispositivo(jwt: String, id: String, dispositivo: DispositivoResponse) {
        lifecycleScope.launch {
            try {
                Log.d("DISPOSITIVOS_DELETE", "DELETE api/Dispositivos/$id")
                Log.d("DISPOSITIVOS_DELETE", "alias: ${dispositivo.alias}")

                val response = dispositivoRepository.eliminarDispositivo(jwt, id)

                Log.d("DISPOSITIVOS_DELETE", "HTTP code: ${response.code()}")

                if (response.isSuccessful) {
                    Toast.makeText(this@ConectarDispositivoActivity, "Dispositivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarDispositivos()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DISPOSITIVOS_DELETE", "ErrorBody: $errorBody")
                    Toast.makeText(this@ConectarDispositivoActivity, "No se pudo eliminar el dispositivo.", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("DISPOSITIVOS_DELETE", "Error de conexión", e)
                Toast.makeText(this@ConectarDispositivoActivity, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("DISPOSITIVOS_DELETE", "Error inesperado", e)
                Toast.makeText(this@ConectarDispositivoActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
