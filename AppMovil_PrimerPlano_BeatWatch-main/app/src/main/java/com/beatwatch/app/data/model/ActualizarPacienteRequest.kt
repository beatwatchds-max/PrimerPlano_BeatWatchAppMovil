package com.beatwatch.app.data.model

data class ActualizarPacienteRequest(
    val curp: String? = null,
    val edad: Int? = null,
    val sexo: String? = null,
    val peso: Double? = null,
    val estatura: Double? = null,
    val fechaNacimiento: String? = null,
    val direccion: String? = null,
    val tipoSangre: String? = null,
    val fotografia: String? = null
)
