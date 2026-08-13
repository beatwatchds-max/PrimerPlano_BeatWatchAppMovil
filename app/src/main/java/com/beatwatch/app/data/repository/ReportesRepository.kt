package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.ReporteResumenResponse
import com.beatwatch.app.data.model.GraficaBpmResponse
import com.beatwatch.app.data.model.GraficaEpisodiosResponse
import com.beatwatch.app.data.model.GraficaSeriesResponse
import retrofit2.Response

class ReportesRepository {

    suspend fun obtenerResumenGraficas(
        jwt: String,
        idPaciente: String,
        dias: Int
    ): Response<ReporteResumenResponse> {
        return RetrofitClient.tableroApiService.obtenerResumenGraficas(
            authorization = "Bearer $jwt",
            patientId = idPaciente,
            dias = dias
        )
    }

    suspend fun obtenerBpm(jwt: String, idPaciente: String, dias: Int): Response<GraficaBpmResponse> =
        RetrofitClient.tableroApiService.obtenerGraficaBpm("Bearer $jwt", idPaciente, dias)

    suspend fun obtenerEpisodios(jwt: String, idPaciente: String, dias: Int): Response<GraficaEpisodiosResponse> =
        RetrofitClient.tableroApiService.obtenerGraficaEpisodios("Bearer $jwt", idPaciente, dias)

    suspend fun obtenerSeries(
        jwt: String,
        idPaciente: String,
        fechaInicio: String,
        fechaFin: String
    ): Response<GraficaSeriesResponse> = RetrofitClient.tableroApiService.obtenerGraficaSeries(
        authorization = "Bearer $jwt",
        patientId = idPaciente,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        metricas = "bpmPromedio,pasos,calorias,distanciaKm,horasSueno"
    )
}
