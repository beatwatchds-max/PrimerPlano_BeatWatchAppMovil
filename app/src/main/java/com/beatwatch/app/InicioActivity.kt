package com.beatwatch.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
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
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_inicio)

        sessionManager = SessionManager.getInstance(this)

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
            startActivity(Intent(this, ConectarDispositivoActivity::class.java))
            finish()
        }
    }
}
