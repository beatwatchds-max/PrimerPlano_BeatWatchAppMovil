package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.ActualizarDispositivoRequest
import com.beatwatch.app.data.model.DispositivoResponse
import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import com.beatwatch.app.data.model.MedicionesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DispositivoApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/Dispositivos")
    suspend fun obtenerDispositivos(
        @Header("Authorization") authorization: String,
        @Query("idPaciente") idPaciente: String
    ): Response<List<DispositivoResponse>>

    @Headers("Accept: application/json")
    @GET("api/Pacientes/{idPaciente}/mediciones")
    suspend fun obtenerMedicionesPaciente(
        @Header("Authorization") authorization: String,
        @Path("idPaciente") idPaciente: String,
        @Query("limite") limite: Int = 1
    ): Response<MedicionesResponse>

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
