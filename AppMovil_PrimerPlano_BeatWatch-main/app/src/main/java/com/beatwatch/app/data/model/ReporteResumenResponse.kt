package com.beatwatch.app.data.model

data class ReporteResumenResponse(
    val totalEpisodiosPeriodo: Int? = null,
    val bpmPromedio: Int? = null,
    val bpmMaximo: Int? = null,
    val porcentajeDiasEstables: Double? = null,
    val conteoSintomas: ConteoSintomasResponse? = null,
    val totalPasos: Int? = null,
    val totalCalorias: Int? = null,
    val totalDistanciaKm: Double? = null,
    val graficaPicos: List<GraficaPicoResponse>? = null
)

data class ConteoSintomasResponse(
    val Mareo: Int? = null,
    val Palpitaciones: Int? = null,
    val DolorPecho: Int? = null,
    val Desmayo: Int? = null,
    val FaltaAire: Int? = null,
    val Fatiga: Int? = null
)

data class GraficaPicoResponse(
    val fecha: String? = null,
    val bpmMaximo: Int? = null,
    val bpmPromedio: Int? = null
)
