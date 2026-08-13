package com.beatwatch.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.beatwatch.app.BuildConfig
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "https://backend-beatwatch.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder().apply {
        // Request and response bodies may include clinical data; log them only in debug builds.
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val firebaseRetrofit = Retrofit.Builder()
        .baseUrl("https://bpm-g2-default-rtdb.firebaseio.com/")
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

    val medicionFirebaseApiService: MedicionFirebaseApiService by lazy {
        firebaseRetrofit.create(MedicionFirebaseApiService::class.java)
    }

    val historialApiService: HistorialApiService by lazy {
        retrofit.create(HistorialApiService::class.java)
    }

    val tableroApiService: TableroApiService by lazy {
        retrofit.create(TableroApiService::class.java)
    }

}
