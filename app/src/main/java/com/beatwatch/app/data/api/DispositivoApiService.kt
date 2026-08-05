package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.ActualizarDispositivoRequest
import com.beatwatch.app.data.model.DispositivoResponse
import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DispositivoApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/Dispositivos")
    suspend fun obtenerDispositivos(
        @Header("Authorization") authorization: String
    ): Response<List<DispositivoResponse>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("api/Dispositivos/emparejar")
    suspend fun emparejarDispositivo(
        @Header("Authorization") authorization: String,
        @Body request: EmparejarDispositivoRequest
    ): Response<DispositivoResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @PUT("api/Dispositivos/{id}")
    suspend fun actualizarDispositivo(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: ActualizarDispositivoRequest
    ): Response<DispositivoResponse>

    @DELETE("api/Dispositivos/{id}")
    suspend fun eliminarDispositivo(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<Unit>
}
