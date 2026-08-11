package com.beatwatch.app.data.model

data class RegistrarTokenRequest(
    val token: String,
    val deviceType: String = "android",
    val deviceId: String
)
