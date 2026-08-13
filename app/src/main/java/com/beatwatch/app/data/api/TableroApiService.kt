package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.ReporteResumenResponse
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
}
