package com.beatwatch.app.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://backend-beatwatch.onrender.com/"
    private const val FIREBASE_REALTIME_DATABASE_URL = "https://bpm-g2-default-rtdb.firebaseio.com/"

    private val okHttpClient = OkHttpClient.Builder()
        // Render puede tardar más de un minuto en reanudar una instancia inactiva.
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .callTimeout(100, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val firebaseRealtimeRetrofit = Retrofit.Builder()
        .baseUrl(FIREBASE_REALTIME_DATABASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)

    val notificacionesApiService: NotificacionesApiService by lazy {
        retrofit.create(NotificacionesApiService::class.java)
    }

    val pacienteApiService: PacienteApiService by lazy {
        retrofit.create(PacienteApiService::class.java)
    }

    val saludApiService: SaludApiService by lazy {
        retrofit.create(SaludApiService::class.java)
    }

    val firebaseRealtimeApiService: FirebaseRealtimeApiService by lazy {
        firebaseRealtimeRetrofit.create(FirebaseRealtimeApiService::class.java)
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
