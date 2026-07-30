package com.beatwatch.app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.beatwatch.app.utils.SessionManager

class InicioActivity : AppCompatActivity() {

    private lateinit var tvNombrePaciente: TextView
    private lateinit var tvPerfilEstado: TextView
    private lateinit var tvDiagnosticoEstado: TextView
    private lateinit var btnContinuar: AppCompatButton
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        sessionManager = SessionManager(this)

        tvNombrePaciente = findViewById(R.id.tvNombrePaciente)
        tvPerfilEstado = findViewById(R.id.tvPerfilEstado)
        tvDiagnosticoEstado = findViewById(R.id.tvDiagnosticoEstado)
        btnContinuar = findViewById(R.id.btnContinuarInicio)

        cargarDatosSesion()
        configurarListener()
    }

    private fun cargarDatosSesion() {
        val nombre = sessionManager.getNombre()
        val perfilCompletado = sessionManager.isPerfilCompletado()
        val diagnosticoCompletado = sessionManager.isDiagnosticoCompletado()
        val pacienteId = sessionManager.getPacienteId()

        Log.d("INICIO_FLOW", "InicioActivity abierta")
        Log.d("INICIO_FLOW", "nombre: $nombre")
        Log.d("INICIO_FLOW", "pacienteId: $pacienteId")
        Log.d("INICIO_FLOW", "perfilCompletado: $perfilCompletado")
        Log.d("INICIO_FLOW", "diagnosticoCompletado: $diagnosticoCompletado")

        if (nombre.isNotBlank()) {
            tvNombrePaciente.text = nombre
        } else {
            tvNombrePaciente.text = ""
        }

        tvPerfilEstado.text = if (perfilCompletado) "Completado" else "Pendiente"
        tvDiagnosticoEstado.text = if (diagnosticoCompletado) "Completado" else "Pendiente"
    }

    private fun configurarListener() {
        btnContinuar.setOnClickListener {
            Toast.makeText(
                this,
                "Enlace del reloj pendiente de implementar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
