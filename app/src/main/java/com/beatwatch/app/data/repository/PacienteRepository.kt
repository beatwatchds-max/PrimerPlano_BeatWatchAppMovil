package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.ActualizarPacienteRequest
import com.beatwatch.app.data.model.PacientePerfilResponse
import com.beatwatch.app.data.model.RegistroPacienteRequest
import com.beatwatch.app.data.model.RegistroPacienteResponse
import okhttp3.ResponseBody
import retrofit2.Response

class PacienteRepository {

    suspend fun registrarPerfilPaciente(
        jwt: String,
        request: RegistroPacienteRequest
    ): Response<RegistroPacienteResponse> {
        return RetrofitClient.pacienteApiService.registrarPerfilPaciente(
            authorization = "Bearer $jwt",
            request = request
        )
    }

    suspend fun obtenerPacientePorUsuarioId(
        jwt: String,
        usuarioId: String
    ): Response<PacientePerfilResponse> {
        return RetrofitClient.pacienteApiService.obtenerPacientePorUsuarioId(
            authorization = "Bearer $jwt",
            usuarioId = usuarioId
        )
    }

    suspend fun actualizarPerfilPaciente(
        jwt: String,
        usuarioId: String,
        request: ActualizarPacienteRequest
    ): Response<ResponseBody> {
        return RetrofitClient.pacienteApiService.actualizarPerfilPaciente(
            authorization = "Bearer $jwt",
            usuarioId = usuarioId,
            request = request
        )
    }
}
