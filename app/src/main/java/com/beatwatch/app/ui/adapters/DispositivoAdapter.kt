package com.beatwatch.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.beatwatch.app.R
import com.beatwatch.app.data.model.DispositivoResponse

class DispositivoAdapter(
    private val dispositivos: MutableList<DispositivoResponse>,
    private val onEditarAlias: (DispositivoResponse) -> Unit,
    private val onEliminarDispositivo: (DispositivoResponse) -> Unit
) : RecyclerView.Adapter<DispositivoAdapter.DispositivoViewHolder>() {

    class DispositivoViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val tvAlias: TextView = view.findViewById(R.id.tvAliasDispositivo)
        val tvTipo: TextView = view.findViewById(R.id.tvTipoDispositivo)
        val tvModelo: TextView = view.findViewById(R.id.tvModeloDispositivo)
        val tvSO: TextView = view.findViewById(R.id.tvSistemaOperativo)
        val tvSerie: TextView = view.findViewById(R.id.tvNumeroSerie)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoDispositivo)
        val btnEditar: AppCompatButton = view.findViewById(R.id.btnEditarAlias)
        val btnEliminar: View = view.findViewById(R.id.btnEliminarDispositivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dispositivo, parent, false)
        return DispositivoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DispositivoViewHolder, position: Int) {
        val dispositivo = dispositivos[position]

        holder.tvAlias.text = dispositivo.alias ?: "Sin alias"

        val tipo = dispositivo.tipoDispositivo ?: "Sin tipo"
        holder.tvTipo.text = "Tipo: $tipo"

        holder.tvModelo.text = "Modelo: ${dispositivo.codigoModelo ?: "--"}"

        holder.tvSO.text = "SO: ${dispositivo.sistemaOperativo ?: "--"}"

        holder.tvSerie.text = "Serie: ${dispositivo.numeroSerie ?: "--"}"

        if (dispositivo.activo == true) {
            holder.tvEstado.text = "Activo"
            holder.tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
            holder.tvEstado.setTextColor(0xFF2E7D32.toInt())
        } else if (dispositivo.activo == false) {
            holder.tvEstado.text = "Inactivo"
            holder.tvEstado.setBackgroundColor(0xFFFFF3E0.toInt())
            holder.tvEstado.setTextColor(0xFFE65100.toInt())
        } else {
            holder.tvEstado.text = "Estado no disponible"
            holder.tvEstado.setBackgroundColor(0x00000000)
            holder.tvEstado.setTextColor(0xFF6B7A90.toInt())
        }

        holder.btnEditar.setOnClickListener {
            onEditarAlias(dispositivo)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarDispositivo(dispositivo)
        }
    }

    override fun getItemCount(): Int = dispositivos.size

    fun actualizarLista(nuevaLista: List<DispositivoResponse>) {
        dispositivos.clear()
        dispositivos.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
