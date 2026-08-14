package com.beatwatch.app.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://backend-beatwatch.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)

    val pacienteApiService: PacienteApiService by lazy {
        retrofit.create(PacienteApiService::class.java)
    }

    val saludApiService: SaludApiService by lazy {
        retrofit.create(SaludApiService::class.java)
    }

    val dispositivoApiService: DispositivoApiService by lazy {
        retrofit.create(DispositivoApiService::class.java)
    }

    val historialApiService: HistorialApiService by lazy {
        retrofit.create(HistorialApiService::class.java)
    }

    val tableroApiService: TableroApiService by lazy {
        retrofit.create(TableroApiService::class.java)
    }

    val analisisApiService: AnalisisApiService by lazy {
        retrofit.create(AnalisisApiService::class.java)
    }

}
