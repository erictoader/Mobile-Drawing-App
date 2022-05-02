package com.erictoader.drawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var drawPath : CustomPath? = null
    private var canvasBitmap : Bitmap? = null
    private var drawPaint : Paint? = null
    private var canvasPaint : Paint? = null
    private var canvas : Canvas? = null

    private val drawPaths = ArrayList<CustomPath>()
    private val undoPaths = ArrayList<CustomPath>()

    private var brushSize : Float = 0.0F
    private var brushColor : Int = Color.BLACK

    init {
        setupDrawing()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(canvasBitmap!!, 0F, 0F, canvasPaint)

        for(path in drawPaths) {
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas?.drawPath(path, drawPaint!!)
        }

        if(!drawPath!!.isEmpty) {
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas?.drawPath(drawPath!!, drawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = brushColor
                drawPath!!.brushThickness = brushSize
                drawPath!!.reset()

                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                drawPaths.add(drawPath!!)
                drawPath = CustomPath(brushColor, brushSize)
            }
            else -> {
                return false
            }
        }
        invalidate()
        return true
    }

    private fun setupDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(brushColor, brushSize)
        drawPaint!!.color = brushColor
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    fun setSizeForBrush(newSize: Float) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        drawPaint!!.strokeWidth = brushSize
    }

    fun setColorForBrush(newColor: Int) {
        brushColor = newColor
        drawPaint!!.color = brushColor
    }

    fun resetCanvas() {
        drawPaths.clear()
        invalidate()
    }

    fun undo(c : Context) {
        if(drawPaths.isNotEmpty()) {
            undoPaths.add(drawPaths.removeLast())
            invalidate()
        }

    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }

}