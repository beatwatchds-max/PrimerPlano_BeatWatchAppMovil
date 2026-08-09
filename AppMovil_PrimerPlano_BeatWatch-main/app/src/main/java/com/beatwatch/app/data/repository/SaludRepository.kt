package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.RegistroArritmiaRequest
import okhttp3.ResponseBody
import retrofit2.Response

class SaludRepository {

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
