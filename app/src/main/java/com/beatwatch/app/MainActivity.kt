package com.beatwatch.app

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.beatwatch.app.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val esPaciente = SessionManager.getInstance(this).getRol().equals("Paciente", ignoreCase = true)
        bottomNavigation.menu.findItem(R.id.nav_reportes).isVisible = !esPaciente

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, InicioFragment())
                .commit()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_reportes && esPaciente) return@setOnItemSelectedListener false

            val fragment: Fragment = when (item.itemId) {
                R.id.nav_inicio -> InicioFragment()
                R.id.nav_historial -> HistorialFragment()
                R.id.nav_reportes -> ReportesFragment()
                R.id.nav_perfil -> PerfilFragment()
                else -> InicioFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()

            true
        }
    }
}
