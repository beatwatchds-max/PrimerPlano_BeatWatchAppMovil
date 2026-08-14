package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.AnalisisPacienteResponse
import retrofit2.Response

class AnalisisRepository {

    suspend fun analizarUltimaEstadistica(jwt: String, patientId: String): Response<AnalisisPacienteResponse> {
        return RetrofitClient.analisisApiService.analizarUltimaEstadistica(
            authorization = "Bearer $jwt",
            patientId = patientId
        )
    }
}
