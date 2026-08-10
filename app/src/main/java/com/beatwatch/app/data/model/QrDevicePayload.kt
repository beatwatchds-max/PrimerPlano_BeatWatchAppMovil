package com.beatwatch.app.data.model

data class QrDevicePayload(
    val idSesion: String,
    val tokenEmparejamiento: String,
    val alias: String? = null
)
