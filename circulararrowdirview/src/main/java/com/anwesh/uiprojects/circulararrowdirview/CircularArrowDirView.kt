package com.anwesh.uiprojects.circulararrowdirview

/**
 * Created by anweshmishra on 16/01/19.
 */

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path

val nodes : Int = 5
val arrows : Int = 4
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.8f
val circleColor : Int = Color.parseColor("#f44336")
val backColor : Int = Color.parseColor("#BDBDBD")