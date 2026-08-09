package com.beatwatch.app.data.model

data class ActualizarDispositivoRequest(
    val numeroSerie: String? = null,
    val alias: String? = null,
    val tipoDispositivo: String? = null,
    val codigoModelo: String? = null,
    val codigoDispositivo: String? = null,
    val sistemaOperativo: String? = null,
    val idPaciente: String? = null
)
