package com.beatwatch.app.data.model

data class LoginMobileResponse(
    val tokenJwt: String? = null,
    val token: String? = null,
    val accessToken: String? = null,
    val jwt: String? = null,
    val usuarioId: String? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val rol: String? = null,
    val idLicencia: String? = null,
    val perfilCompletado: Boolean? = null,
    val diagnosticoCompletado: Boolean? = null,
    val dispositivoVinculado: Boolean? = null,
    val pacienteId: String? = null
)
