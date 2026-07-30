package com.beatwatch.app.data.model

data class RegistroArritmiaRequest(
    val tipo: String,
    val frecuenciaCardiaca: Int,
    val duracionEpisodioSeconds: Int,
    val idPaciente: String,
    val sintomas: SintomasRequest,
    val factoresRiesgo: FactoresRiesgoRequest
)

data class SintomasRequest(
    val mareo: Boolean,
    val palpitaciones: Boolean,
    val dolorPecho: Boolean,
    val desmayo: Boolean,
    val faltaAire: Boolean,
    val fatiga: Boolean
)

data class FactoresRiesgoRequest(
    val hipertensionArterial: Boolean,
    val obesidadImcElevado: Boolean,
    val apneaSueno: Boolean,
    val tabaquismo: Boolean,
    val alcoholismo: Boolean,
    val estresCronico: Boolean
)
