package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.AnalisisApiService
import com.beatwatch.app.data.model.AnalisisPacienteResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AnalisisRepository {

    companion object {
        const val BASE_URL = "https://backend-beatwatch.onrender.com/"
        private const val ML_API_KEY = "3853049439852348258"
    }

    private val analisisApiService: AnalisisApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AnalisisApiService::class.java)

    suspend fun analizarUltimaEstadistica(patientId: String): Response<AnalisisPacienteResponse> {
        return analisisApiService.analizarUltimaEstadistica(
            apiKey = ML_API_KEY,
            patientId = patientId
        )
    }
}
