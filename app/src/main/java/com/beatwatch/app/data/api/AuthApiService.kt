package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.LoginMobileRequest
import com.beatwatch.app.data.model.LoginMobileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("api/Autenticacion/iniciar-sesion-movil")
    suspend fun iniciarSesionMovil(
        @Body request: LoginMobileRequest
    ): Response<LoginMobileResponse>

    @Headers("Accept: application/json")
    @POST("api/Autenticacion/cerrar-sesion-movil")
    suspend fun cerrarSesionMovil(
        @Header("Authorization") authorization: String
    ): Response<Unit>
}
