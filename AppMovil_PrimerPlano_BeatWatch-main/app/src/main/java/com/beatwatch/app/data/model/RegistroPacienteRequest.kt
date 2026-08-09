package com.beatwatch.app.data.model

data class RegistroPacienteRequest(
    val usuarioId: String,
    val curp: String,
    val edad: Int,
    val sexo: String,
    val peso: Double,
    val estatura: Double,
    val fechaNacimiento: String,
    val direccion: String,
    val tipoSangre: String,
    val idLicencia: String,
    val fotografia: String? = null
)
