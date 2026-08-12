package com.beatwatch.app.data.model

data class MedicionesResponse(
    val success: Boolean = false,
    val mediciones: List<MedicionResponse> = emptyList()
)

data class MedicionResponse(
    val idMedicion: String? = null,
    val frecuenciaCardiacaBpm: Int? = null,
    val saturacionOxigenoSpO2: Int? = null,
    val timestamp: String? = null
)
