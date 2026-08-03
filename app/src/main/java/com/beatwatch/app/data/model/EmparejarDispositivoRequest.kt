package com.beatwatch.app.data.model

data class EmparejarDispositivoRequest(
    val numeroSerie: String,
    val alias: String,
    val tipoDispositivo: String,
    val codigoModelo: String,
    val codigoDispositivo: String,
    val sistemaOperativo: String,
    val idPaciente: String
)
