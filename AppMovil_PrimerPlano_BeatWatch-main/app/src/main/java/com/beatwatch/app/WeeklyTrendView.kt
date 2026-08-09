package com.beatwatch.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class WeeklyTrendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E6ED")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A90E2")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A90E2")
        style = Paint.Style.FILL
    }

    private val pointStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7A90")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9AA7B4")
        textSize = 22f
        textAlign = Paint.Align.RIGHT
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A4A90E2")
        style = Paint.Style.FILL
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9AA7B4")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val days = arrayOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    private val yValues = floatArrayOf(0f, 35f, 70f, 105f, 140f)
    private val maxY = 140f

    private var data: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private var hasRealData = false

    fun setData(values: FloatArray) {
        if (values.size == 7) {
            data = values
            hasRealData = values.any { it > 0f }
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingTop = 30f
        val paddingBottom = 50f
        val paddingLeft = 60f
        val paddingRight = 30f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        for (yVal in yValues) {
            val y = paddingTop + chartHeight * (1f - yVal / maxY)
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
            canvas.drawText(
                yVal.toInt().toString(),
                paddingLeft - 8f,
                y + 8f,
                yLabelPaint
            )
        }

        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
        for (i in data.indices) {
            val x = paddingLeft + stepX * i
            canvas.drawText(days[i], x, height - 4f, labelPaint)
        }

        if (hasRealData) {
            dibujarPuntos(canvas, stepX, paddingLeft, paddingTop, chartHeight)
        } else {
            canvas.drawText(
                "Sin datos para la gráfica",
                width / 2f,
                height / 2f,
                emptyPaint
            )
        }
    }

    private fun dibujarPuntos(
        canvas: Canvas,
        stepX: Float,
        paddingLeft: Float,
        paddingTop: Float,
        chartHeight: Float
    ) {
        val points = mutableListOf<Pair<Float, Float>>()
        val path = Path()
        var started = false

        for (i in data.indices) {
            val x = paddingLeft + stepX * i
            val y = paddingTop + chartHeight * (1f - data[i] / maxY)
            points.add(x to y)

            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }

        if (points.size >= 2) {
            val lastPoint = points.last()
            val fillPath = Path(path)
            fillPath.lineTo(lastPoint.first, paddingTop + chartHeight)
            fillPath.lineTo(points.first().first, paddingTop + chartHeight)
            fillPath.close()
            canvas.drawPath(fillPath, fillPaint)
            canvas.drawPath(path, linePaint)
        }

        for ((x, y) in points) {
            canvas.drawCircle(x, y, 7f, pointPaint)
            canvas.drawCircle(x, y, 7f, pointStrokePaint)
        }
    }
}
