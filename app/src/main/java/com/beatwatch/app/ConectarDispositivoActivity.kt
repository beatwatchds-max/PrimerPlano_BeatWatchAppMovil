package com.beatwatch.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import com.beatwatch.app.data.repository.DispositivoRepository
import com.beatwatch.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class ConectarDispositivoActivity : AppCompatActivity() {

    private lateinit var btnBuscar: AppCompatButton
    private lateinit var btnOmitir: AppCompatButton
    private lateinit var dispositivosContainer: LinearLayout
    private lateinit var tvSinDispositivos: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var dispositivoRepository: DispositivoRepository

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            buscarDispositivos()
        } else {
            Toast.makeText(
                this,
                "Permiso Bluetooth requerido para buscar dispositivos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conectar_dispositivo)

        sessionManager = SessionManager(this)
        dispositivoRepository = DispositivoRepository()

        btnBuscar = findViewById(R.id.btnBuscar)
        btnOmitir = findViewById(R.id.btnOmitir)
        dispositivosContainer = findViewById(R.id.dispositivosContainer)
        tvSinDispositivos = findViewById(R.id.tvSinDispositivos)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        btnBuscar.setOnClickListener {
            verificarPermisosBluetooth()
        }

        btnOmitir.setOnClickListener {
            Toast.makeText(this, "Puedes conectar tu dispositivo más tarde", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun verificarPermisosBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            Toast.makeText(
                this,
                "Activa el Bluetooth para buscar dispositivos.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permisosFaltantes = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisosFaltantes.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisosFaltantes.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (permisosFaltantes.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(permisosFaltantes.toTypedArray())
            } else {
                buscarDispositivos()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            } else {
                buscarDispositivos()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun buscarDispositivos() {
        dispositivosContainer.removeAllViews()
        tvSinDispositivos.visibility = View.GONE
        dispositivosContainer.visibility = View.VISIBLE

        val dispositivos = bluetoothAdapter?.bondedDevices

        if (dispositivos.isNullOrEmpty()) {
            dispositivosContainer.visibility = View.GONE
            tvSinDispositivos.visibility = View.VISIBLE
            return
        }

        for (device in dispositivos) {
            val itemView = layoutInflater.inflate(
                android.R.layout.simple_list_item_2,
                dispositivosContainer,
                false
            )
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1.text = device.name ?: "Dispositivo desconocido"
            text2.text = device.address ?: ""

            itemView.setOnClickListener {
                seleccionarDispositivo(device)
            }

            dispositivosContainer.addView(itemView)
        }
    }

    @SuppressLint("MissingPermission")
    private fun seleccionarDispositivo(device: BluetoothDevice) {
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
                "No se encontró información del paciente. Inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val codigoDispositivo = device.address ?: ""
        if (codigoDispositivo.isBlank()) {
            Toast.makeText(this, "No se pudo obtener la dirección del dispositivo", Toast.LENGTH_LONG).show()
            return
        }

        val alias = device.name ?: "Reloj inteligente"
        val numeroSerie = codigoDispositivo.replace(":", "")
        val codigoModelo = device.name ?: "Modelo desconocido"

        val request = EmparejarDispositivoRequest(
            numeroSerie = numeroSerie,
            alias = alias,
            tipoDispositivo = "Smartwatch",
            codigoModelo = codigoModelo,
            codigoDispositivo = codigoDispositivo,
            sistemaOperativo = "Android",
            idPaciente = idPaciente
        )

        emparejarDispositivo(jwt, request)
    }

    private fun emparejarDispositivo(jwt: String, request: EmparejarDispositivoRequest) {
        lifecycleScope.launch {
            try {
                Log.d("DISPOSITIVO_API", "Endpoint: api/Dispositivos/emparejar")
                Log.d("DISPOSITIVO_API", "JWT existe: ${jwt.isNotBlank()}")
                Log.d("DISPOSITIVO_API", "idPaciente: ${request.idPaciente}")
                Log.d("DISPOSITIVO_API", "numeroSerie: ${request.numeroSerie}")
                Log.d("DISPOSITIVO_API", "alias: ${request.alias}")
                Log.d("DISPOSITIVO_API", "tipoDispositivo: ${request.tipoDispositivo}")
                Log.d("DISPOSITIVO_API", "codigoModelo: ${request.codigoModelo}")
                Log.d("DISPOSITIVO_API", "codigoDispositivo: ${request.codigoDispositivo}")
                Log.d("DISPOSITIVO_API", "sistemaOperativo: ${request.sistemaOperativo}")
                Log.d("DISPOSITIVO_API", "Request enviado: $request")

                val response = dispositivoRepository.emparejarDispositivo(jwt, request)

                Log.d("DISPOSITIVO_API", "HTTP code: ${response.code()}")
                Log.d("DISPOSITIVO_API", "isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    Log.d("DISPOSITIVO_API", "Body: ${response.body()}")

                    Toast.makeText(
                        this@ConectarDispositivoActivity,
                        "Dispositivo emparejado correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(this@ConectarDispositivoActivity, MainActivity::class.java))
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DISPOSITIVO_API", "ErrorBody: $errorBody")

                    val mensaje = when (response.code()) {
                        400 -> "Datos inválidos. Verifica el dispositivo."
                        401 -> "Sesión expirada. Inicia sesión nuevamente."
                        404 -> "Endpoint no encontrado."
                        in 500..599 -> "Error del servidor. Intenta más tarde."
                        else -> "Error inesperado: ${response.code()}"
                    }
                    Toast.makeText(this@ConectarDispositivoActivity, mensaje, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("DISPOSITIVO_API", "Error de conexión", e)
                Toast.makeText(
                    this@ConectarDispositivoActivity,
                    "No se pudo conectar con el servidor",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("DISPOSITIVO_API", "Error inesperado", e)
                Toast.makeText(
                    this@ConectarDispositivoActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
