package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.RegistrarTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT

interface NotificacionesApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("api/Notificaciones/token")
    suspend fun registrarToken(
        @Header("Authorization") authorization: String,
        @Body request: RegistrarTokenRequest
    ): Response<Unit>
}
