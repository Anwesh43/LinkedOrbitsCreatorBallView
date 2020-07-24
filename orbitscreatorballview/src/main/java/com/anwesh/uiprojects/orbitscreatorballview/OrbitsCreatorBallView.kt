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
val endSizeFactor : Float = 0.8f
val strokeFactor : Float = 90f
val ballRFactor : Float = 0.1f
val delay : Long = 20
val circles : Int = 3
val scGap : Float = 0.02f / circles

