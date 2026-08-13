package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.ReporteResumenResponse
import retrofit2.Response

class ReportesRepository {

    suspend fun obtenerResumenGraficas(
        jwt: String,
        idPaciente: String,
        dias: Int
    ): Response<ReporteResumenResponse> {
        return RetrofitClient.tableroApiService.obtenerResumenGraficas(
            authorization = "Bearer $jwt",
            patientId = idPaciente,
            dias = dias
        )
    }
}
