package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.MedicionResponse
import retrofit2.Response
import retrofit2.http.GET

interface FirebaseRealtimeApiService {
    @GET("beatwatch/galaxy-watch-4-classic/ultimaMedicion.json")
    suspend fun obtenerUltimaMedicion(): Response<MedicionResponse>
}
