package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.RegistroArritmiaRequest
import com.beatwatch.app.data.model.MedicionResponse
import okhttp3.ResponseBody
import retrofit2.Response

class SaludRepository {

    suspend fun obtenerUltimaMedicion(jwt: String, pacienteId: String): Response<MedicionResponse> {
        return RetrofitClient.saludApiService.obtenerUltimaMedicion(
            authorization = "Bearer $jwt",
            patientId = pacienteId
        )
    }

    suspend fun registrarArritmia(
        jwt: String,
        request: RegistroArritmiaRequest
    ): Response<ResponseBody> {
        return RetrofitClient.saludApiService.registrarArritmia(
            authorization = "Bearer $jwt",
            request = request
        )
    }
}
