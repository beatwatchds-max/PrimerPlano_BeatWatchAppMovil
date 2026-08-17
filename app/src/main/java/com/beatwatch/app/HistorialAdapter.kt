package com.beatwatch.app

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beatwatch.app.data.model.HistorialResponse

class HistorialAdapter(
    private val items: List<HistorialResponse>
) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tvTipoHistorial)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaHistorial)
        val tvDuracion: TextView = view.findViewById(R.id.tvDuracionHistorial)
        val tvBpm: TextView = view.findViewById(R.id.tvBpmHistorial)
        val vwIndicador: View = view.findViewById(R.id.vwIndicadorColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val tipo = item.tipoAnomalia?.trim()
        holder.tvTipo.text = when {
            !tipo.isNullOrBlank() && tipo != "string" -> tipo
            else -> "Registro"
        }

        holder.tvFecha.text = formatearFecha(item)

        val bpm = item.frecuenciaCardiaca
        holder.tvBpm.text = if (bpm != null) bpm.toString() else "--"

        val duracionSegundos = item.duracionEpisodioSeconds
        if (duracionSegundos != null && duracionSegundos > 0) {
            val minutos = duracionSegundos / 60
            holder.tvDuracion.visibility = View.VISIBLE
            holder.tvDuracion.text = "Duración: $minutos min"
        } else {
            holder.tvDuracion.visibility = View.GONE
        }

        val color = when {
            tipo.isNullOrBlank() || tipo == "string" -> Color.parseColor("#4A90E2")
            tipo.equals("Normal", ignoreCase = true) -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#FF6B75")
        }

        val bg = holder.vwIndicador.background as? GradientDrawable
        if (bg != null) {
            bg.setColor(color)
        } else {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            holder.vwIndicador.background = drawable
        }
    }

    override fun getItemCount(): Int = items.size

    private fun formatearFecha(item: HistorialResponse): String {
        val fechaRaw = item.fecha ?: return "Fecha no disponible"

        return try {
            val isoDate = fechaRaw.take(19).replace("T", " ")
            if (isoDate.length >= 16) {
                val fechaParte = isoDate.substring(0, 10)
                val horaParte = isoDate.substring(11, 16)

                val partes = fechaParte.split("-")
                if (partes.size == 3) {
                    "${partes[2]}/${partes[1]} · $horaParte"
                } else {
                    fechaRaw
                }
            } else {
                fechaRaw
            }
        } catch (e: Exception) {
            fechaRaw
        }
    }
}
