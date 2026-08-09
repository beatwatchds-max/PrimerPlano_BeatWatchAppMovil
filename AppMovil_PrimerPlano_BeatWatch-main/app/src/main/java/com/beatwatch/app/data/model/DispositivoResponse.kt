package com.beatwatch.app.data.model

data class DispositivoResponse(
    val id: String? = null,
    val dispositivoId: String? = null,
    val numeroSerie: String? = null,
    val alias: String? = null,
    val tipoDispositivo: String? = null,
    val codigoModelo: String? = null,
    val codigoDispositivo: String? = null,
    val sistemaOperativo: String? = null,
    val idPaciente: String? = null,
    val fechaEmparejamiento: String? = null,
    val activo: Boolean? = null
)
