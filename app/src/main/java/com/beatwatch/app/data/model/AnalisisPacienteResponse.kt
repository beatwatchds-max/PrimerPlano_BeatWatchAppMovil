package com.beatwatch.app.data.model

data class AnalisisPacienteResponse(
    val fecha: String? = null,
    val risk: RiesgoMl? = null,
    val anomaly: AnomaliaMl? = null,
    val recommendations: List<RecomendacionMl>? = null
)

data class RiesgoMl(
    val risk: String? = null,
    val probability: Double? = null
)

data class AnomaliaMl(
    val anomaly: Boolean? = null
)

data class RecomendacionMl(
    val type: String? = null,
    val level: String? = null,
    val message: String? = null,
    val recommendation: String? = null
)
