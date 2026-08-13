package com.beatwatch.app.data.model

data class ReporteResumenResponse(
    val idPaciente: String? = null,
    val periodo: String? = null,
    val promedioBPM: Double? = null,
    val totalPasos: Int? = null,
    val totalArritmias: Int? = null,
    val promedioHorasSueno: Double? = null
)

data class GraficaBpmResponse(
    val idPaciente: String? = null,
    val puntos: List<PuntoBpmResponse> = emptyList()
)

data class PuntoBpmResponse(
    val fecha: String? = null,
    val promedio: Double? = null,
    val minimo: Int? = null,
    val maximo: Int? = null
)

data class GraficaEpisodiosResponse(
    val idPaciente: String? = null,
    val episodios: List<EpisodioGraficaResponse> = emptyList()
)

data class EpisodioGraficaResponse(
    val fecha: String? = null,
    val totalArritmias: Int? = null,
    val criticas: Int? = null,
    val duracionTotalSegundos: Int? = null
)

data class GraficaSeriesResponse(
    val idPaciente: String? = null,
    val metricasSolicitadas: List<String> = emptyList(),
    val series: List<PuntoSerieResponse> = emptyList()
)

data class PuntoSerieResponse(
    val fecha: String? = null,
    val bpmPromedio: Double? = null,
    val bpmMinimo: Int? = null,
    val bpmMaximo: Int? = null,
    val pasos: Int? = null,
    val calorias: Double? = null,
    val distanciaKm: Double? = null,
    val horasSueno: Double? = null
)
