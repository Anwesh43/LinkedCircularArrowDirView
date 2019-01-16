package com.anwesh.uiprojects.circulararrowdirview

/**
 * Created by anweshmishra on 16/01/19.
 */

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

val nodes : Int = 5
val arrows : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.8f
val circleColor : Int = Color.parseColor("#f44336")
val backColor : Int = Color.parseColor("#BDBDBD")
val strokeFactor : Int = 90

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()

fun Canvas.drawArrow(size : Float, paint : Paint) {
    val path : Path = Path()
    path.moveTo(-size / 2, size / 2)
    path.lineTo(size / 2, size / 2)
    path.lineTo(0f, -size/2)
    drawPath(path, paint)
}

fun Canvas.drawCircularArrow(x : Float, y : Float, size : Float, rot : Float, paint : Paint) {
    paint.style = Paint.Style.FILL
    save()
    translate(x, y)
    rotate(rot)
    paint.color = circleColor
    drawCircle(0f, 0f, size/2, paint)
    paint.color = backColor
    drawArrow(2 * size/3, paint)
    restore()
}

fun Canvas.drawStrokedCircle(deg : Float, r : Float, paint : Paint) {
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = circleColor
    drawArc(RectF(-r, -r, r, r), 0f, deg, false, paint)
}

fun Canvas.drawCADNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val rSize : Float = size / 2
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(gap * (i + 1), h / 2)
    drawStrokedCircle(360f * sc2, size/2, paint)
    var deg : Float = 0f
    for (j in 0..(arrows - 1)) {
        val sc : Float = sc1.divideScale(j, arrows)
        val scj1 : Float = sc.divideScale(0, 2)
        val scj2 : Float = sc.divideScale(1, 2)
        deg += 90f * scj1
        drawCircularArrow((size - rSize) * scj2, 0f, rSize, deg, paint)
    }
    restore()
}

class CircularArrowDirView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}