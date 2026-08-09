package com.beatwatch.app.data.model

data class EmparejarDispositivoRequest(
    val idSesion: String,
    val tokenEmparejamiento: String,
    val idPaciente: String,
    val alias: String
)
