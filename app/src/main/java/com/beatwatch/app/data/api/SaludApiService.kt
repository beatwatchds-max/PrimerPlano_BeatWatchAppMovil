package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.RegistroArritmiaRequest
import com.beatwatch.app.data.model.RegistroArritmiaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SaludApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("api/salud/arritmia")
    suspend fun registrarArritmia(
        @Header("Authorization") authorization: String,
        @Body request: RegistroArritmiaRequest
    ): Response<RegistroArritmiaResponse>
}
