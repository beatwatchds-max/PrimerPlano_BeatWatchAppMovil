package com.beatwatch.app.data.repository

import com.beatwatch.app.data.api.RetrofitClient
import com.beatwatch.app.data.model.EmparejarDispositivoRequest
import com.beatwatch.app.data.model.EmparejarDispositivoResponse
import retrofit2.Response

class DispositivoRepository {

    suspend fun emparejarDispositivo(
        jwt: String,
        request: EmparejarDispositivoRequest
    ): Response<EmparejarDispositivoResponse> {
        return RetrofitClient.dispositivoApiService.emparejarDispositivo(
            authorization = "Bearer $jwt",
            request = request
        )
    }
}
