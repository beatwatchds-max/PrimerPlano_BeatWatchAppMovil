package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.LoginMobileRequest
import com.beatwatch.app.data.model.LoginMobileResponse
import retrofit2.Response

class AuthRepository {

    private val api = RetrofitClient.authApiService

    suspend fun iniciarSesionMovil(token: String): Response<LoginMobileResponse> {
        val request = LoginMobileRequest(token = token)
        return api.iniciarSesionMovil(request)
    }

    suspend fun cerrarSesionMovil(jwt: String): Response<Unit> {
        return api.cerrarSesionMovil("Bearer $jwt")
    }
}
