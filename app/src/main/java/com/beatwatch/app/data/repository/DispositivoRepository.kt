package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.ActualizarDispositivoRequest
import com.beatwatch.app.data.model.DispositivoResponse
import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import retrofit2.Response

class DispositivoRepository {

    suspend fun obtenerDispositivos(
        jwt: String,
        pacienteId: String
    ): Response<List<DispositivoResponse>> {
        return RetrofitClient.dispositivoApiService.obtenerDispositivos(
            authorization = "Bearer $jwt",
            idPaciente = pacienteId
        )
    }

    suspend fun emparejarDispositivo(
        jwt: String,
        request: EmparejarDispositivoRequest
    ): Response<DispositivoResponse> {
        return RetrofitClient.dispositivoApiService.emparejarDispositivo(
            authorization = "Bearer $jwt",
            request = request
        )
    }

    suspend fun actualizarDispositivo(
        jwt: String,
        id: String,
        request: ActualizarDispositivoRequest
    ): Response<DispositivoResponse> {
        return RetrofitClient.dispositivoApiService.actualizarDispositivo(
            authorization = "Bearer $jwt",
            id = id,
            request = request
        )
    }

    suspend fun eliminarDispositivo(
        jwt: String,
        id: String
    ): Response<Unit> {
        return RetrofitClient.dispositivoApiService.eliminarDispositivo(
            authorization = "Bearer $jwt",
            id = id
        )
    }
}
