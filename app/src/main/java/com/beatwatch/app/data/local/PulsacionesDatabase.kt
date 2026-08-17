package com.beatwatch.app.data.local

import android.content.ContentValues
import android.content.Context
import com.beatwatch.app.utils.SessionManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

data class PulsacionLocal(
    val frecuenciaCardiacaBpm: Int,
    val saturacionOxigenoSpO2: Int?,
    val timestamp: String?
)

class PulsacionesDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    private val appContext = context.applicationContext

    init {
        SQLiteDatabase.loadLibs(appContext)
        // Las mediciones de la base previa no estaban cifradas; se descartan al migrar.
        appContext.deleteDatabase(LEGACY_DATABASE_NAME)
    }

    private fun openWritableDatabase(): SQLiteDatabase =
        super.getWritableDatabase(SessionManager.getInstance(appContext).getDatabasePassphrase())

    private fun openReadableDatabase(): SQLiteDatabase =
        super.getReadableDatabase(SessionManager.getInstance(appContext).getDatabasePassphrase())

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_PULSACIONES (" +
                "$COLUMN_PACIENTE_ID TEXT PRIMARY KEY, " +
                "$COLUMN_FRECUENCIA INTEGER NOT NULL, " +
                "$COLUMN_SATURACION INTEGER, " +
                "$COLUMN_TIMESTAMP TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun guardarUltimaPulsacion(pacienteId: String, pulsacion: PulsacionLocal) {
        val values = ContentValues().apply {
            put(COLUMN_PACIENTE_ID, pacienteId)
            put(COLUMN_FRECUENCIA, pulsacion.frecuenciaCardiacaBpm)
            pulsacion.saturacionOxigenoSpO2?.let { put(COLUMN_SATURACION, it) }
                ?: putNull(COLUMN_SATURACION)
            pulsacion.timestamp?.let { put(COLUMN_TIMESTAMP, it) } ?: putNull(COLUMN_TIMESTAMP)
        }
        openWritableDatabase().insertWithOnConflict(
            TABLE_PULSACIONES,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun obtenerUltimaPulsacion(pacienteId: String): PulsacionLocal? {
        openReadableDatabase().query(
            TABLE_PULSACIONES,
            arrayOf(COLUMN_FRECUENCIA, COLUMN_SATURACION, COLUMN_TIMESTAMP),
            "$COLUMN_PACIENTE_ID = ?",
            arrayOf(pacienteId),
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return PulsacionLocal(
                frecuenciaCardiacaBpm = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FRECUENCIA)),
                saturacionOxigenoSpO2 = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_SATURACION))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SATURACION)),
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            )
        }
    }

    companion object {
        const val DATABASE_NAME = "beatwatch_pulsaciones_secure.db"
        private const val LEGACY_DATABASE_NAME = "beatwatch_pulsaciones.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_PULSACIONES = "pulsaciones"
        private const val COLUMN_PACIENTE_ID = "paciente_id"
        private const val COLUMN_FRECUENCIA = "frecuencia_cardiaca_bpm"
        private const val COLUMN_SATURACION = "saturacion_oxigeno_spo2"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }
}
