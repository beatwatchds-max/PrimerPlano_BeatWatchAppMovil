package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.RegistroArritmiaRequest
import com.beatwatch.app.data.model.MedicionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface SaludApiService {

    @GET("api/salud/pacientes/{patientId}/ultima-medicion")
    suspend fun obtenerUltimaMedicion(
        @Header("Authorization") authorization: String,
        @Path("patientId") patientId: String
    ): Response<MedicionResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("api/salud/arritmia")
    suspend fun registrarArritmia(
        @Header("Authorization") authorization: String,
        @Body request: RegistroArritmiaRequest
    ): Response<ResponseBody>
}
