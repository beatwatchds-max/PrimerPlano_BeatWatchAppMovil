package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import com.beatwatch.app.data.model.EmparejarDispositivoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface DispositivoApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("api/Dispositivos/emparejar")
    suspend fun emparejarDispositivo(
        @Header("Authorization") authorization: String,
        @Body request: EmparejarDispositivoRequest
    ): Response<EmparejarDispositivoResponse>
}
