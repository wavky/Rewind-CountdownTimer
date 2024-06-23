package com.wavky.cdtimer.app.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.wavky.cdtimer.common.util.Debug
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.atan2

class CircleGestureView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

  interface OnCircleGestureListener {
    fun onTap(x: Float, y: Float, rawX: Float, rawY: Float)
    fun onCircleGesture(circleCount: Int, angle: Float, deltaAngle: Float)
    fun onCircleGestureStart()
    fun onGestureFinish()
  }

  private val gestureDetector: GestureDetector
  private val points = mutableListOf<Pair<Float, Float>>()
  private val paint = Paint().apply {
    color = Color.RED
    strokeWidth = 5f
    style = Paint.Style.STROKE
  }
  private var customCenterPoint: PointF? = null
  private val centerX
    get() = customCenterPoint?.x ?: (width / 2f)
  private val centerY
    get() = customCenterPoint?.y ?: (height / 2f)
  private var totalAngle = 0f
  private var circleCount = 0
  private val startGesture = AtomicBoolean(false)

  var onCircleGestureListener: OnCircleGestureListener? = null

  init {
    gestureDetector = GestureDetector(context, GestureListener())
  }

  fun setCenterPoint(x: Float, y: Float) {
    customCenterPoint = PointF(x, y)
  }

  fun resetCalculation() {
    totalAngle = 0f
    circleCount = 0
  }

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    val eventHandled = event?.let {
      gestureDetector.onTouchEvent(event)
    } ?: false
    return if (eventHandled) {
      true
    } else {
      if (event?.action == MotionEvent.ACTION_UP) {
        if (startGesture.compareAndSet(true, false)) {
          if (Debug.isEnable) {
            points.clear()
            invalidate()
          }
          onCircleGestureListener?.onGestureFinish()
        }
      }
      super.onTouchEvent(event)
    }
  }

  override fun performClick(): Boolean {
    return super.performClick()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (!Debug.isEnable) return

    // 绘制手势滑动轨迹
    if (points.isNotEmpty()) {
      for (i in 1 until points.size) {
        val (x1, y1) = points[i - 1]
        val (x2, y2) = points[i]
        canvas.drawLine(x1, y1, x2, y2, paint)
      }
    }

    // 绘制中心点
    canvas.drawCircle(centerX, centerY, 12f, paint)
  }

  private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
      performClick()
      onCircleGestureListener?.onTap(e.x, e.y, e.rawX, e.rawY)
      return true
    }

    override fun onDown(e: MotionEvent): Boolean {
      points.clear()
      totalAngle = 0f
      circleCount = 0
      return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
      return false
    }

    override fun onScroll(
      e1: MotionEvent?,
      e2: MotionEvent,
      distanceX: Float,
      distanceY: Float,
    ): Boolean {
      if (startGesture.compareAndSet(false, true)) {
        onCircleGestureListener?.onCircleGestureStart()
      }
      val x = e2.x
      val y = e2.y
      points.add(x to y)
      val angle = calculateAngle(centerX, centerY, x, y)
      if (points.size > 1) {
        val (prevX, prevY) = points[points.size - 2]
        val prevAngle = calculateAngle(centerX, centerY, prevX, prevY)
        val deltaAngle = normalizeAngle(angle - prevAngle)
        totalAngle += deltaAngle
        if (totalAngle >= 360) {
          circleCount++
          totalAngle = 0f
        } else if (totalAngle <= -360) {
          circleCount--
          totalAngle = 0f
        }
        onCircleGestureListener?.onCircleGesture(circleCount, totalAngle, deltaAngle)
      }
      invalidate()
      return true
    }
  }

  private fun calculateAngle(cx: Float, cy: Float, x: Float, y: Float): Float {
    return Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()
  }

  private fun normalizeAngle(angle: Float): Float {
    return when {
      angle < -180 -> angle + 360
      angle > 180 -> angle - 360
      else -> angle
    }
  }
}
