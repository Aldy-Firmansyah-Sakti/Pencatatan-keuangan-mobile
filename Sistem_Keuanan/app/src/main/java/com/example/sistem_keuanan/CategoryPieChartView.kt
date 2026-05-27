package com.example.sistem_keuanan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CategoryPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class CategorySlice(val label: String, val value: Long, val isExpense: Boolean)

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 14f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 16f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
    }
    private var chartData: List<CategorySlice> = emptyList()

    private val incomeColors = listOf(
        Color.parseColor("#2E7D32"),
        Color.parseColor("#43A047"),
        Color.parseColor("#689F38"),
        Color.parseColor("#7CB342")
    )
    private val expenseColors = listOf(
        Color.parseColor("#B71C1C"),
        Color.parseColor("#C62828"),
        Color.parseColor("#D32F2F"),
        Color.parseColor("#E53935")
    )

    fun setChartData(data: List<CategorySlice>) {
        chartData = data.filter { it.value > 0 }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (chartData.isEmpty() || chartData.sumOf { it.value } <= 0) {
            canvas.drawText(
                "Tidak ada data transaksi",
                width * 0.5f,
                height * 0.5f,
                emptyPaint
            )
            return
        }

        val total = chartData.sumOf { it.value }.toFloat()
        val diameter = (width.coerceAtMost(height) * 0.7f)
        val radius = diameter / 2f
        val centerX = width * 0.5f
        val centerY = height * 0.45f
        val rectF = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var startAngle = -90f
        chartData.forEachIndexed { index, slice ->
            val sweepAngle = (slice.value / total) * 360f
            segmentPaint.color = if (slice.isExpense) {
                expenseColors[index % expenseColors.size]
            } else {
                incomeColors[index % incomeColors.size]
            }
            canvas.drawArc(rectF, startAngle, sweepAngle, true, segmentPaint)
            startAngle += sweepAngle
        }

        val centerLabel = when {
            chartData.all { !it.isExpense } -> "Pemasukan"
            chartData.all { it.isExpense } -> "Pengeluaran"
            else -> "Ringkasan"
        }

        canvas.drawText(
            centerLabel,
            centerX,
            centerY + (textPaint.textSize * 0.25f),
            textPaint
        )

        canvas.drawText(
            "Rp ${chartData.sumOf { it.value }}",
            centerX,
            centerY + (textPaint.textSize * 1.6f),
            textPaint
        )
    }
}
