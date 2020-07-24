package com.anwesh.uiprojects.orbitscreatorballview

/**
 * Created by anweshmishra on 25/07/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val colors : Array<String> = arrayOf("#3F51B5", "#4CAF50", "#2196F3", "#FF5722", "#009688")
val startSizeFactor : Float = 0.25f
val endSizeFactor : Float = 0.4f
val strokeFactor : Float = 90f
val ballRFactor : Float = 0.1f
val delay : Long = 20
val circles : Int = 3
val scGap : Float = 0.02f / circles
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawOrbitCreatorBall(scale : Float, w : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val start : Float = w * startSizeFactor
    val end : Float = w * endSizeFactor
    val gap : Float = (start - end) / circles
    var si1 : Float = 0f
    var si2 : Float = 0f
    val ballR : Float = w * ballRFactor
    for (j in 0..(circles - 1)) {
        val r  : Float = start + j * gap
        val sfi : Float = sf.divideScale(j, circles)
        if (sfi <= 0f) {
            break
        }
        val sfi1 : Float = sfi.divideScale(0, 2)
        val sfi2 : Float = sfi.divideScale(1, 2)
        si1 = sfi1
        if (j != circles - 1) {
            si2 += sfi2
        }
        paint.style = Paint.Style.STROKE
        drawArc(RectF(-r, -r, r, r), 0f, 360f * sfi1, false, paint)
    }
    paint.style = Paint.Style.FILL
    save()
    rotate(360f * si1)
    drawCircle(0f, -(start + gap * si2), ballR, paint)
    restore()
}

fun Canvas.drawOCBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor(colors[i])
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, h / 2)
    drawOrbitCreatorBall(scale, w, paint)
    restore()
}

class OrbitsCreatorBallView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
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
                    Thread.sleep(delay)
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
            }
        }
    }

    data class OCBNode(var i : Int, val state : State = State()) {

        private var next : OCBNode? = null
        private var prev : OCBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = OCBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawOCBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : OCBNode {
            var curr : OCBNode? = prev
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

    data class OrbitsCreatorBall(var i : Int) {

        private var curr : OCBNode = OCBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : OrbitsCreatorBallView) {

        private val animator : Animator = Animator(view)
        private val ocb : OrbitsCreatorBall = OrbitsCreatorBall(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            ocb.draw(canvas, paint)
            animator.animate {
                ocb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ocb.startUpdating {
                animator.start()
            }
        }
    }
}