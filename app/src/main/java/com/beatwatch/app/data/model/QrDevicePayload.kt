package com.beatwatch.app.data.model

data class QrDevicePayload(
    val numeroSerie: String,
    val alias: String,
    val tipoDispositivo: String,
    val codigoModelo: String,
    val codigoDispositivo: String,
    val sistemaOperativo: String,
    val versionAplicacion: String
)
