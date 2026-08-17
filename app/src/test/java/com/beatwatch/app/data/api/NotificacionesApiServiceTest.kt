package com.beatwatch.app.data.api

import com.beatwatch.app.data.model.RegistrarTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificacionesApiServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: NotificacionesApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificacionesApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `registra el token con bearer y acepta 204`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        val response = service.registrarToken("Bearer jwt-prueba", RegistrarTokenRequest("fcm-prueba", deviceId = "android-id"))

        assertTrue(response.isSuccessful)
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/Notificaciones/token", request.path)
        assertEquals("Bearer jwt-prueba", request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("\"deviceType\":\"android\""))
    }

    @Test
    fun `expone una respuesta 401 para que el gestor la registre`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("sesión no válida"))

        val response = service.registrarToken("Bearer jwt-prueba", RegistrarTokenRequest("fcm-prueba", deviceId = "android-id"))

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
        assertEquals("sesión no válida", response.errorBody()?.string())
    }
}
