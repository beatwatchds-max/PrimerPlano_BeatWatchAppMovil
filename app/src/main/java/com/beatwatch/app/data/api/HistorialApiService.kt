package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.HistorialResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface HistorialApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/historial")
    suspend fun obtenerHistorial(
        @Header("Authorization") authorization: String,
        @Query("idPaciente") idPaciente: String
    ): Response<List<HistorialResponse>>
}
