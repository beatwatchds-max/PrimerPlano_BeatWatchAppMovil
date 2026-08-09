package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.HistorialResponse
import retrofit2.Response

class HistorialRepository {

    suspend fun obtenerHistorial(
        jwt: String,
        idPaciente: String
    ): Response<List<HistorialResponse>> {
        return RetrofitClient.historialApiService.obtenerHistorial(
            authorization = "Bearer $jwt",
            idPaciente = idPaciente
        )
    }
}
