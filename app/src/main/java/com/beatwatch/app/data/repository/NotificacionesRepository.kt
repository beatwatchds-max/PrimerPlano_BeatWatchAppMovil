package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.RegistrarTokenRequest
import retrofit2.Response

class NotificacionesRepository {
    private val api = RetrofitClient.notificacionesApiService

    suspend fun registrarToken(jwt: String, request: RegistrarTokenRequest): Response<Unit> =
        api.registrarToken("Bearer $jwt", request)
}
