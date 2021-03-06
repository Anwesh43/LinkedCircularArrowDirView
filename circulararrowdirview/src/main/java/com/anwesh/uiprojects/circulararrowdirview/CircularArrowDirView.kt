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
val rSizeFactor : Float = 2.0f
val DELAY : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

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
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val rSize : Float = size / rSizeFactor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(w/2, gap * (i + 1))
    drawStrokedCircle(360f * sc2, size, paint)
    var deg : Float = 0f
    for (j in 0..(arrows - 1)) {
        val sc : Float = sc1.divideScale(j, arrows)
        val scj1 : Float = sc.divideScale(0, 2)
        val scj2 : Float = sc.divideScale(1, 2)
        deg += 90f * scj1
        save()
        rotate(90f * j)
        drawCircularArrow((size - rSize/2) * scj2, 0f, rSize, deg - 90f * j, paint)
        restore()
    }
    restore()
}

class CircularArrowDirView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, arrows * 2, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
                view.postInvalidate()
            }
        }
    }

    data class CADNode(var i : Int, val state : State = State()) {

        private var prev : CADNode? = null
        private var next : CADNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CADNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCADNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CADNode {
            var curr : CADNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CircularArrowDir(var i : Int) {
        private val root : CADNode = CADNode(0)
        private var curr : CADNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircularArrowDirView) {

        private val animator : Animator = Animator(view)
        private val cad : CircularArrowDir = CircularArrowDir(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            cad.draw(canvas, paint)
            animator.animate {
                cad.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cad.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : CircularArrowDirView {
            val view : CircularArrowDirView = CircularArrowDirView(activity)
            activity.setContentView(view)
            return view
        }
    }
 }