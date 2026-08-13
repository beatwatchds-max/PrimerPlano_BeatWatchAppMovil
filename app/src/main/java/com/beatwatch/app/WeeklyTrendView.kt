package com.beatwatch.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class WeeklyTrendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E0E6ED"); strokeWidth = 1f }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF6B75"); strokeWidth = 4f; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF6B75") }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6B7A90"); textSize = 24f; textAlign = Paint.Align.CENTER }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9AA7B4"); textSize = 22f; textAlign = Paint.Align.RIGHT }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9AA7B4"); textSize = 28f; textAlign = Paint.Align.CENTER }
    private var values = emptyList<Float>()
    private var labels = emptyList<String>()

    fun setData(newValues: List<Float>, newLabels: List<String>) {
        values = newValues
        labels = newLabels
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) {
            canvas.drawText("Sin datos para la gráfica", width / 2f, height / 2f, emptyPaint)
            return
        }
        val left = 56f; val right = 20f; val top = 18f; val bottom = 42f
        val chartWidth = width - left - right; val chartHeight = height - top - bottom
        val minValue = values.minOrNull() ?: 0f
        val maxValue = max(values.maxOrNull() ?: 1f, minValue + 1f)
        repeat(4) { index ->
            val ratio = index / 3f
            val y = top + chartHeight * ratio
            canvas.drawLine(left, y, width - right, y, gridPaint)
            canvas.drawText("%.0f".format(maxValue - (maxValue - minValue) * ratio), left - 8f, y + 7f, valuePaint)
        }
        val step = chartWidth / (values.size - 1).coerceAtLeast(1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = left + index * step
            val y = top + chartHeight * (1 - (value - minValue) / (maxValue - minValue))
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            canvas.drawCircle(x, y, 6f, pointPaint)
            if (index == 0 || index == values.lastIndex || values.size <= 7) {
                canvas.drawText(labels.getOrElse(index) { "" }, x, height - 5f, labelPaint)
            }
        }
        canvas.drawPath(path, linePaint)
    }
}
