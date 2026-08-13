package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.ReporteResumenResponse
import com.beatwatch.app.data.model.GraficaBpmResponse
import com.beatwatch.app.data.model.GraficaEpisodiosResponse
import com.beatwatch.app.data.model.GraficaSeriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface TableroApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/graficas/{patientId}/resumen")
    suspend fun obtenerResumenGraficas(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
        @Query("dias") dias: Int
    ): Response<ReporteResumenResponse>

    @GET("api/graficas/{patientId}/bpm")
    suspend fun obtenerGraficaBpm(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
        @Query("dias") dias: Int
    ): Response<GraficaBpmResponse>

    @GET("api/graficas/{patientId}/episodios")
    suspend fun obtenerGraficaEpisodios(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
        @Query("dias") dias: Int
    ): Response<GraficaEpisodiosResponse>

    @GET("api/graficas/{patientId}/series")
    suspend fun obtenerGraficaSeries(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String,
        @Query("fecha_inicio") fechaInicio: String,
        @Query("fecha_fin") fechaFin: String,
        @Query("metricas") metricas: String
    ): Response<GraficaSeriesResponse>
}
