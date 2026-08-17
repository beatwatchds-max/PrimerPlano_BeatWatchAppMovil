package com.beatwatch.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
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
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
        if (contenido.length > MAX_QR_PAYLOAD_LENGTH) {
            Toast.makeText(this, "QR inválido: contenido demasiado largo", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val payload = gson.fromJson(contenido, QrDevicePayload::class.java)

            if (
                payload.idSesion.isBlank() ||
                payload.idSesion.length > MAX_SESSION_ID_LENGTH ||
                payload.tokenEmparejamiento.isBlank() ||
                payload.tokenEmparejamiento.length > MAX_PAIRING_TOKEN_LENGTH ||
                payload.alias?.length ?: 0 > MAX_ALIAS_LENGTH
            ) {
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
                Toast.makeText(this, "No se encontró el paciente de la sesión", Toast.LENGTH_LONG).show()
                return
            }

            val request = EmparejarDispositivoRequest(
                idSesion = payload.idSesion,
                tokenEmparejamiento = payload.tokenEmparejamiento,
                idPaciente = pacienteId,
                alias = payload.alias,
            )

            emparejarPorQR(jwt, request)

        } catch (_: Exception) {
            Toast.makeText(this, "QR inválido. El código no contiene datos válidos del dispositivo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun emparejarPorQR(jwt: String, request: EmparejarDispositivoRequest) {
        Toast.makeText(this, "Emparejando dispositivo...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = dispositivoRepository.emparejarDispositivo(jwt, request)

                if (response.isSuccessful) {
                    sessionManager.guardarDispositivoVinculado(true)

                    Toast.makeText(this@ConectarDispositivoActivity, "Dispositivo vinculado correctamente", Toast.LENGTH_LONG).show()

                    cargarDispositivos()
                } else {
                    Toast.makeText(this@ConectarDispositivoActivity, "No fue posible vincular el dispositivo.", Toast.LENGTH_LONG).show()
                }
            } catch (_: IOException) {
                Toast.makeText(this@ConectarDispositivoActivity, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(this@ConectarDispositivoActivity, "No fue posible vincular el dispositivo.", Toast.LENGTH_LONG).show()
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

            if (nuevoAlias.length > MAX_ALIAS_LENGTH) {
                etAlias.error = "El alias no puede exceder $MAX_ALIAS_LENGTH caracteres"
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
                    val response = dispositivoRepository.actualizarDispositivo(jwt, id, request)

                    if (response.isSuccessful) {
                        Toast.makeText(this@ConectarDispositivoActivity, "Alias actualizado correctamente.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarDispositivos()
                    } else {
                        Toast.makeText(this@ConectarDispositivoActivity, "No se pudo actualizar el alias.", Toast.LENGTH_LONG).show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                    }
                } catch (_: IOException) {
                    Toast.makeText(this@ConectarDispositivoActivity, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Guardar"
                } catch (_: Exception) {
                    Toast.makeText(this@ConectarDispositivoActivity, "No se pudo actualizar el alias.", Toast.LENGTH_LONG).show()
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
                eliminarDispositivo(jwt, id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDispositivo(jwt: String, id: String) {
        lifecycleScope.launch {
            try {
                val response = dispositivoRepository.eliminarDispositivo(jwt, id)

                if (response.isSuccessful) {
                    Toast.makeText(this@ConectarDispositivoActivity, "Dispositivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarDispositivos()
                } else {
                    Toast.makeText(this@ConectarDispositivoActivity, "No se pudo eliminar el dispositivo.", Toast.LENGTH_LONG).show()
                }
            } catch (_: IOException) {
                Toast.makeText(this@ConectarDispositivoActivity, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(this@ConectarDispositivoActivity, "No se pudo eliminar el dispositivo.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private companion object {
        const val MAX_QR_PAYLOAD_LENGTH = 4096
        const val MAX_SESSION_ID_LENGTH = 128
        const val MAX_PAIRING_TOKEN_LENGTH = 2048
        const val MAX_ALIAS_LENGTH = 80
    }
}
