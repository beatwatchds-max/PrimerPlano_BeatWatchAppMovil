package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.AnalisisPacienteResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AnalisisApiService {

    @Headers("Accept: application/json")
    @POST("api/analisis/{patientId}/latest")
    suspend fun analizarUltimaEstadistica(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String
    ): Response<AnalisisPacienteResponse>
}
