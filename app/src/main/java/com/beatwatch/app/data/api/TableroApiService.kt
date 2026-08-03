package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.ReporteResumenResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface TableroApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/tablero/resumen")
    suspend fun obtenerResumenTablero(
        @Header("Authorization") authorization: String,
        @Query("idPaciente") idPaciente: String,
        @Query("dias") dias: Int
    ): Response<ReporteResumenResponse>
}
