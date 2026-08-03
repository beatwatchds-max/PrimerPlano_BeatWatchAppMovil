package com.beatwatch.app.data.model

data class HistorialResponse(
    val id: String? = null,
    val idPaciente: String? = null,
    val tipoAnomalia: String? = null,
    val frecuenciaCardiaca: Int? = null,
    val duracionEpisodioSeconds: Int? = null,
    val esAlertaCritica: Boolean? = null,
    val fecha: String? = null
)
