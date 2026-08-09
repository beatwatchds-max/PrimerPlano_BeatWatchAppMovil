package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.ActualizarPacienteRequest
import com.beatwatch.app.data.model.PacientePerfilResponse
import com.beatwatch.app.data.model.RegistroPacienteRequest
import com.beatwatch.app.data.model.RegistroPacienteResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PacienteApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("api/Pacientes/perfil")
    suspend fun registrarPerfilPaciente(
        @Header("Authorization") authorization: String,
        @Body request: RegistroPacienteRequest
    ): Response<RegistroPacienteResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/Pacientes/usuario/{usuarioId}")
    suspend fun obtenerPacientePorUsuarioId(
        @Header("Authorization") authorization: String,
        @Path("usuarioId") usuarioId: String
    ): Response<PacientePerfilResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @PATCH("api/Pacientes/perfil/{usuarioId}")
    suspend fun actualizarPerfilPaciente(
        @Header("Authorization") authorization: String,
        @Path("usuarioId") usuarioId: String,
        @Body request: ActualizarPacienteRequest
    ): Response<ResponseBody>
}
