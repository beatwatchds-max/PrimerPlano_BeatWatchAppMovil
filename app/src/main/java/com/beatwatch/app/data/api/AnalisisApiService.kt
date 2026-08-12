package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.AnalisisPacienteResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnalisisApiService {

    @Headers("Accept: application/json")
    @POST("analysis/latest/{patientId}")
    suspend fun analizarUltimaEstadistica(
        @Header("x-api-key") apiKey: String,
        @Path("patientId") patientId: String,
        @Query("persist") persist: Boolean = true
    ): Response<AnalisisPacienteResponse>
}
