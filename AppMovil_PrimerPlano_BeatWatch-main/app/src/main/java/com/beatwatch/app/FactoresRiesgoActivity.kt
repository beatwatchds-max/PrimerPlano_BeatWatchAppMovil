package com.beatwatch.app

import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class FactoresRiesgoActivity : AppCompatActivity() {

    private lateinit var cbHipertension: CheckBox
    private lateinit var cbObesidad: CheckBox
    private lateinit var cbApnea: CheckBox
    private lateinit var cbTabaquismo: CheckBox
    private lateinit var cbAlcoholismo: CheckBox
    private lateinit var cbEstres: CheckBox
    private lateinit var btnContinuar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_factores_riesgo)

        inicializarVistas()
        configurarListener()
    }

    private fun inicializarVistas() {
        cbHipertension = findViewById(R.id.cbHipertension)
        cbObesidad = findViewById(R.id.cbObesidad)
        cbApnea = findViewById(R.id.cbApnea)
        cbTabaquismo = findViewById(R.id.cbTabaquismo)
        cbAlcoholismo = findViewById(R.id.cbAlcoholismo)
        cbEstres = findViewById(R.id.cbEstres)
        btnContinuar = findViewById(R.id.btnContinuarFactores)
    }

    private fun configurarListener() {
        btnContinuar.setOnClickListener {
            validarFormulario()
        }
    }

    private fun validarFormulario() {
        val haySeleccion = cbHipertension.isChecked ||
                cbObesidad.isChecked ||
                cbApnea.isChecked ||
                cbTabaquismo.isChecked ||
                cbAlcoholismo.isChecked ||
                cbEstres.isChecked

        if (!haySeleccion) {
            Toast.makeText(this, R.string.error_selecciona_factor, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.msg_factores_capturados, Toast.LENGTH_LONG).show()
        }
    }
}
