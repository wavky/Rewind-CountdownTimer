package com.wavky.cdtimer.app.main

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wavky.cdtimer.R
import com.wavky.cdtimer.app.main.CircleGestureView.OnCircleGestureListener
import com.wavky.cdtimer.app.main.MainFragment.ClockHand.HOUR
import com.wavky.cdtimer.app.main.MainFragment.ClockHand.MINUTE
import com.wavky.cdtimer.app.main.MainFragment.ClockHand.SECOND
import com.wavky.cdtimer.common.app.ui.fragment.BaseFragment
import com.wavky.cdtimer.common.ext.dpToPx
import com.wavky.cdtimer.databinding.FragmentMainBinding
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.random.Random

private const val COFFEE_BLUR_RADIUS = 8 // 咖啡杯高斯模糊半径 8
private const val DEFAULT_SHAKE_DURATION = 10_000 // 闹钟图像晃动默认时长 10s
private const val VIBRATION_AMPLITUDE = 255 // 最大震动强度
private const val SHAKE_AMPLITUDE = 10f // 闹钟图像晃动幅度
private const val SHAKE_UNIT_DURATION_MS = 50L // 闹钟图像晃动单位时长 50ms
private const val SCALE_ANIMATION_AMPLITUDE = 1.5f // 时间文本缩放动画幅度 1.5 倍
private const val SCALE_ANIMATION_DURATION_MS = 500L // 时间文本缩放动画时长 500ms
private const val MAX_VOLUME = 1f // 最大音量
private const val MIN_VOLUME = 0f // 静音
private const val TICKS_REPLAY_INTERVAL_MS = 32 * 1000 // 重播滴答声的时间间隔 32s
private const val TICKS_CHECK_INTERVAL_MS = 300L // 检查滴答声的时间间隔 300ms
private const val QUICK_TICKS_AUTO_STOP_DELAY_MS = 500L // 快速滴答声自动停止延迟 0.5s
private const val COUNTDOWN_INITIAL_DELAY_MS = 1000L // 倒计时初始延迟 1s
private const val COUNTDOWN_INTERVAL_MS = 1000L // 倒计时间隔 1s

private const val CLOCK_HAND_ROTATION_OFFSET = -90f // 时钟指针初始旋转校正角度（从 3 点钟指向更正为 12 点钟指向）
private const val SEC_MIN_UNIT_DEGREES = 6 // 秒针、分针的单位角度（1 秒钟/分钟刻度的角度）
private const val SEC_MIN_UNIT_DEGREES_F = 6f
private const val HOUR_UNIT_DEGREES = 30 // 时针的单位角度（1 小时刻度的角度）
private const val HOUR_UNIT_DEGREES_F = 30f
private const val DEGREES_IN_MINUTE = 360f // 秒针转动一分钟的总角度（转动 1 圈）
private const val DEGREES_IN_HOUR = 360 * 60f // 秒针转动一小时的总角度（转动 60 圈）
private const val DEGREES_IN_CIRCLE = 360 // 一圈的角度
private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 12 * 60
private const val MINUTES_IN_HOUR = 60
private const val MINUTES_IN_CIRCLE = 60 // 分针转动一圈的分钟数
private const val HOURS_IN_CIRCLE = 12 // 时针转动一圈的小时数
private const val SECOND_HAND_ANGLE_PER_SECOND = SEC_MIN_UNIT_DEGREES_F // 秒针转动一秒，秒针转动的角度
private const val MINUTE_HAND_ANGLE_PER_SECOND = 0.1f // 秒针转动一秒，分针转动的角度
private const val HOUR_HAND_ANGLE_PER_MINUTE = 0.5f // 分针转动一分钟，时针转动的角度
private const val HOUR_HAND_ANGLE_PER_SECOND = HOUR_HAND_ANGLE_PER_MINUTE / 60 // 秒针转动一秒，时针转动的角度
private const val MAX_HOURS = 100 // 最大计时小时数
private const val MAX_DEGREES_OF_TIME = MAX_HOURS * DEGREES_IN_HOUR // 最大有效计时角度（秒针）

class MainFragment : BaseFragment() {

  private enum class ClockHand {
    SECOND,
    MINUTE,
    HOUR,
  }

  private var binding: FragmentMainBinding? = null
  private var isCountingDown = false
  private var resumeCountdownOnScrollFinished = false
  private var isMuted = false
  private var countdownTimer: Timer? = null
  private var tickSoundJob: Job? = null
  private var stopQuickTickJob: Job? = null
  private var tickingMediaPlayer: MediaPlayer? = null
  private var quickTickingMediaPlayer: MediaPlayer? = null
  private var alarmMediaPlayer: MediaPlayer? = null
  private var clockAnimator: ObjectAnimator? = null
  private var timeXAnimator: ObjectAnimator? = null
  private var timeYAnimator: ObjectAnimator? = null
  private var vibrator: Vibrator? = null
  private var clockHandReceiveCircleGesture = SECOND // 接收旋转手势操作的指针
  private var totalAngle: Float = 0f // 以秒针为基准的总角度
  private var prevTotalAngle: Float = 0f // 下一次旋转手势开始时缓存的当前总角度
  private fun Float.angleToSeconds(): Int = (this / SEC_MIN_UNIT_DEGREES).toInt()
  private fun Float.angleToDisplaySeconds(): Int = this.angleToSeconds() % SECONDS_IN_MINUTE
  private fun Float.angleToMinutes(): Int = this.angleToSeconds() / SECONDS_IN_MINUTE
  private fun Float.angleToDisplayMinutes(): Int = this.angleToMinutes() % MINUTES_IN_HOUR

  private fun Float.angleToHours(): Int = this.angleToMinutes() / MINUTES_IN_HOUR
  private fun Float.angleToDisplayHours(): Int = this.angleToHours() % HOURS_IN_CIRCLE
  private val currSeconds: Int
    get() = totalAngle.angleToSeconds()
  private val currDisplaySeconds
    get() = totalAngle.angleToDisplaySeconds()
  private val currMinutes
    get() = totalAngle.angleToMinutes()
  private val currDisplayMinutes
    get() = totalAngle.angleToDisplayMinutes()
  private val currHours
    get() = totalAngle.angleToHours()
  private val currDisplayHours
    get() = totalAngle.angleToDisplayHours()

  private val random = Random(System.currentTimeMillis())

  private val onCircleGestureListener: OnCircleGestureListener
    get() = object : OnCircleGestureListener {
      override fun onTap(x: Float, y: Float, rawX: Float, rawY: Float) {
        binding?.apply {
          if (drinkImage.isVisible &&
            x >= drinkImage.x && x <= drinkImage.x + drinkImage.width &&
            y >= drinkImage.y && y <= drinkImage.y + drinkImage.height
          ) {
            onDrinkImageClick()
          }
          stopAlarm()
          stopTimeTextScaleAnimation()
          resetClockHandReceiveCircleGesture()
        }
      }

      /**
       * 旋转手势处理（多次回调）
       * 本次旋转累计：从手指按下到抬起为止
       * 本次回调：随着手指的移动，按一定频率触发回调，每次回调返回增量角度
       *
       * @param circleCount Int 本次旋转累计圈数
       * @param angle Float 本次旋转累计角度（-360~360，超过 360° 时计入圈数）
       * @param deltaAngle Float 当次回调的增量角度
       */
      override fun onCircleGesture(circleCount: Int, angle: Float, deltaAngle: Float) {
        stopQuickTickJob?.cancel()
        binding?.apply {
          // 不再处理超过有效时间范围的旋转
          if (totalAngle >= MAX_DEGREES_OF_TIME && deltaAngle > 0 ||
            totalAngle <= 0 && deltaAngle < 0
          ) {
            stopQuickTickSound()
            circleGestureView.resetCalculation()
            return
          }

          startQuickTickSound()
          stopQuickTickJob = lifecycleScope.launch {
            delay(QUICK_TICKS_AUTO_STOP_DELAY_MS)
            stopQuickTickSound()
          }
          // 旋转不同指针时的计算新的总角度
          when (clockHandReceiveCircleGesture) {
            SECOND -> totalAngle += deltaAngle

            MINUTE -> totalAngle =
              prevTotalAngle +
                (circleCount * MINUTES_IN_CIRCLE + angle.toInt() / SEC_MIN_UNIT_DEGREES) * DEGREES_IN_MINUTE

            HOUR -> totalAngle =
              prevTotalAngle +
                (circleCount * HOURS_IN_CIRCLE + angle.toInt() / HOUR_UNIT_DEGREES) * DEGREES_IN_HOUR
          }

          // 根据总角度旋转指针
          when {
            // 时间超过最大时限时，设置为最大时限的旋转角度
            totalAngle > MAX_DEGREES_OF_TIME -> {
              totalAngle = MAX_DEGREES_OF_TIME
              prevTotalAngle = MAX_DEGREES_OF_TIME
              adjustClockHandsRotation()
            }

            // 时间小于 0 时，重置旋转角度为 0
            totalAngle < 0 -> {
              totalAngle = 0f
              prevTotalAngle = 0f
              resetClockHandRotation()
            }

            // 在有效时间范围内旋转指针时
            else -> {
              when (clockHandReceiveCircleGesture) {
                SECOND -> {
                  secondHand.rotation += deltaAngle
                  minuteHand.rotation += deltaAngle / SECONDS_IN_MINUTE
                  hourHand.rotation += deltaAngle / SECONDS_IN_HOUR
                }

                else -> {
                  adjustClockHandsRotation(MINUTE, HOUR)
                }
              }
            }
          }
          updateCountDownTextDisplay()
        }
        stopAlarm()
      }

      override fun onCircleGestureStart() {
        binding?.apply {
          circleGestureView.setCenterPoint(
            secondHand.x + secondHand.width / 2,
            secondHand.y + secondHand.height / 2
          )
        }
        prevTotalAngle = totalAngle
        stopAlarm()
      }

      override fun onGestureFinish() {
        stopQuickTickSound()
        stopTimeTextScaleAnimation()
        adjustClockHandsRotation()
        resetClockHandReceiveCircleGesture()
        if (resumeCountdownOnScrollFinished) {
          performStartClick()
        }
      }
    }

  private fun resetClockHandReceiveCircleGesture() {
    clockHandReceiveCircleGesture = SECOND
  }

  private fun adjustClockHandsRotation(vararg clockHands: ClockHand = ClockHand.values()) {
    binding?.apply {
      clockHands.forEach { hand ->
        when (hand) {
          SECOND -> {
            secondHand.rotation =
              (CLOCK_HAND_ROTATION_OFFSET +
                currSeconds * SEC_MIN_UNIT_DEGREES // 秒针的基础角度
                ) % DEGREES_IN_CIRCLE
          }

          MINUTE -> {
            minuteHand.rotation =
              (CLOCK_HAND_ROTATION_OFFSET +
                currMinutes * SEC_MIN_UNIT_DEGREES + // 分针的基础角度
                currDisplaySeconds * MINUTE_HAND_ANGLE_PER_SECOND // 秒针秒数对应的分针角度增量
                ) % DEGREES_IN_CIRCLE
          }

          HOUR -> {
            hourHand.rotation =
              (CLOCK_HAND_ROTATION_OFFSET +
                currHours * HOUR_UNIT_DEGREES + // 时针的基础角度
                currDisplayMinutes * HOUR_HAND_ANGLE_PER_MINUTE // 分针分钟数对应的时针角度增量
                ) % DEGREES_IN_CIRCLE
          }
        }
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    initMediaPlayer()
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding?.root
  }

  private fun initMediaPlayer() {
    tickingMediaPlayer = MediaPlayer.create(requireContext(), R.raw.plastic_ticking).apply {
      isLooping = true
      setVolume(MAX_VOLUME, MAX_VOLUME)
    }
    quickTickingMediaPlayer = MediaPlayer.create(requireContext(), R.raw.quick_ticking).apply {
      isLooping = true
      setVolume(MAX_VOLUME, MAX_VOLUME)
    }
    alarmMediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm).apply {
      setVolume(MAX_VOLUME, MAX_VOLUME)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding?.apply {
      visibleButton.isActivated = true
      resetClockHandRotation()
      updateCountDownTextDisplay()
      initDrinkImage()

      circleGestureView.onCircleGestureListener = onCircleGestureListener
      resetButton.setOnClickListener {
        totalAngle = 0f
        isCountingDown = false
        resetClockHandRotation()
        updateCountDownTextDisplay()
        stopCountDown()
        stopAlarm()
        startButton.text = getString(R.string.start)
      }
      startButton.setOnClickListener {
        if (!isCountingDown) {
          performStartClick()
        } else {
          performStopClick()
        }
        stopTimeTextScaleAnimation()
        resetClockHandReceiveCircleGesture()
      }
      visibleButton.setOnClickListener {
        if (visibleButton.isActivated) {
          visibleButton.isActivated = false
          timerHmsGroup.isGone = true
          timerMsGroup.isGone = true
          buttonGroup.isGone = true
        } else {
          visibleButton.isActivated = true
          buttonGroup.isVisible = true
          updateCountDownTextDisplay()
        }
      }
      soundButton.setOnClickListener {
        if (isMuted) {
          isMuted = false
          tickingMediaPlayer?.setVolume(MAX_VOLUME, MAX_VOLUME)
          quickTickingMediaPlayer?.setVolume(MAX_VOLUME, MAX_VOLUME)
          alarmMediaPlayer?.setVolume(MAX_VOLUME, MAX_VOLUME)
          soundButton.imageTintList = null
          forbidMark.isGone = true
        } else {
          isMuted = true
          tickingMediaPlayer?.setVolume(MIN_VOLUME, MIN_VOLUME)
          quickTickingMediaPlayer?.setVolume(MIN_VOLUME, MIN_VOLUME)
          alarmMediaPlayer?.setVolume(MIN_VOLUME, MIN_VOLUME)
          soundButton.imageTintList =
            ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
          forbidMark.isVisible = true
        }
      }
      msSecondText.setOnClickListener {
        doOnTimeTextClick(SECOND)
      }
      hmsSecondText.setOnClickListener {
        doOnTimeTextClick(SECOND)
      }
      msMinuteText.setOnClickListener {
        doOnTimeTextClick(MINUTE)
      }
      hmsMinuteText.setOnClickListener {
        doOnTimeTextClick(MINUTE)
      }
      hmsHourText.setOnClickListener {
        doOnTimeTextClick(HOUR)
      }
    }
  }

  private fun initDrinkImage() {
    binding?.drinkImage?.apply {
      val visible = random.nextBoolean()
      isVisible = visible
      if (!visible) return

      val padding = random.nextInt(requireContext().dpToPx(24))
      val xOffset = random.nextInt(requireContext().dpToPx(30))
      val yOffset = random.nextInt(requireContext().dpToPx(50))
      setPadding(padding)
      x += xOffset
      y += yOffset

      Glide.with(this@MainFragment)
        .load(R.drawable.drink)
        .transform(BlurTransformation(COFFEE_BLUR_RADIUS))
        .into(this)
    }
  }

  private fun onDrinkImageClick() {
    InfoDialogFragment().show(parentFragmentManager, "InfoDialogFragment")
  }

  private fun doOnTimeTextClick(type: ClockHand) {
    clockHandReceiveCircleGesture = type
    startTimeTextScaleAnimation()
    resumeCountdownOnScrollFinished = isCountingDown
    performStopClick()
  }

  private fun performStopClick() {
    binding?.apply {
      if (isCountingDown) {
        isCountingDown = false
        startButton.text = getString(R.string.start)
        stopCountDown()
        stopAlarm()
      }
    }
  }

  private fun performStartClick() {
    resumeCountdownOnScrollFinished = false
    binding?.apply {
      if (!isCountingDown && totalAngle > 0) {
        isCountingDown = true
        startButton.text = getString(R.string.stop)
        startCountDown()
      }
    }
  }

  private fun resetClockHandRotation() {
    binding?.apply {
      secondHand.rotation = CLOCK_HAND_ROTATION_OFFSET
      minuteHand.rotation = CLOCK_HAND_ROTATION_OFFSET
      hourHand.rotation = CLOCK_HAND_ROTATION_OFFSET
    }
  }

  private fun updateCountDownTextDisplay() {
    binding?.apply {
      if (!visibleButton.isActivated) return

      if (currHours == 0) {
        timerHmsGroup.visibility = View.GONE
        timerMsGroup.visibility = View.VISIBLE

        msMinuteText.text = currDisplayMinutes.toTimeString()
        msSecondText.text = currDisplaySeconds.toTimeString()
      } else {
        timerHmsGroup.visibility = View.VISIBLE
        timerMsGroup.visibility = View.GONE

        hmsHourText.text = currHours.toTimeString()
        hmsMinuteText.text = currDisplayMinutes.toTimeString()
        hmsSecondText.text = currDisplaySeconds.toTimeString()
      }
    }
  }

  private fun startCountDown() {
    tickSoundJob = lifecycleScope.launch(Dispatchers.IO) {
      if (tickingMediaPlayer == null) {
        initMediaPlayer()
      }
      stopAlarm()
      delay(COUNTDOWN_INITIAL_DELAY_MS)
      tickingMediaPlayer?.start()
      while (true) {
        delay(TICKS_CHECK_INTERVAL_MS)
        // 剩余时间不满一秒时，直接结束计时
        if (totalAngle < SEC_MIN_UNIT_DEGREES) {
          stopTickSound()
          startAlarm()
          break
        }
        autoReplayTickSound()
      }
    }
    countdownTimer =
      fixedRateTimer("countdownTimer", false, COUNTDOWN_INITIAL_DELAY_MS, COUNTDOWN_INTERVAL_MS) {
        totalAngle -= SEC_MIN_UNIT_DEGREES_F
        // 剩余时间不满一秒时，直接结束计时
        if (totalAngle < SEC_MIN_UNIT_DEGREES) {
          totalAngle = 0f
          lifecycleScope.launch(Dispatchers.Main) {
            resetClockHandRotation()
            updateCountDownTextDisplay()
            binding?.startButton?.text = getString(R.string.start)
          }
          isCountingDown = false
          cancel()
          return@fixedRateTimer
        }
        lifecycleScope.launch(Dispatchers.Main) {
          binding?.apply {
            secondHand.rotation -= SECOND_HAND_ANGLE_PER_SECOND
            minuteHand.rotation -= MINUTE_HAND_ANGLE_PER_SECOND
            hourHand.rotation -= HOUR_HAND_ANGLE_PER_SECOND
            updateCountDownTextDisplay()
          }
        }
      }
  }

  private fun stopCountDown() {
    stopTickSound()
    tickSoundJob?.cancel()
    tickSoundJob = null
    countdownTimer?.cancel()
    countdownTimer = null
  }

  private fun startAlarm() {
    alarmMediaPlayer?.seekTo(0)
    alarmMediaPlayer?.start()

    lifecycleScope.launch(Dispatchers.Main) {
      startClockShakeAnimation()
    }

    startVibration()
  }

  private fun stopAlarm() {
    alarmMediaPlayer?.apply {
      if (isPlaying) pause()
      seekTo(0)
    }

    lifecycleScope.launch(Dispatchers.Main) {
      stopClockShakeAnimation()
    }

    vibrator?.cancel()
  }

  private fun stopTickSound() {
    tickingMediaPlayer?.apply {
      if (isPlaying) pause()
      seekTo(0)
    }
  }

  private fun startQuickTickSound() {
    quickTickingMediaPlayer?.apply {
      if (!isPlaying) {
        start()
      }
    }
  }

  private fun stopQuickTickSound() {
    quickTickingMediaPlayer?.apply {
      if (isPlaying) pause()
      seekTo(0)
    }
  }

  private fun startTimeTextScaleAnimation() {
    binding?.apply {
      stopTimeTextScaleAnimation()
      val timeText = when (clockHandReceiveCircleGesture) {
        SECOND -> hmsSecondText.takeIf { it.isVisible } ?: msSecondText
        MINUTE -> hmsMinuteText.takeIf { it.isVisible } ?: msMinuteText
        HOUR -> hmsHourText
      }
      timeXAnimator = timeText.makeScaleAnimation("scaleX")
      timeYAnimator = timeText.makeScaleAnimation("scaleY")
    }
  }

  private fun stopTimeTextScaleAnimation() {
    timeXAnimator?.cancel()
    timeYAnimator?.cancel()
    timeXAnimator = null
    timeYAnimator = null
  }

  private fun View.makeScaleAnimation(scale: String): ObjectAnimator =
    ObjectAnimator.ofFloat(this, scale, 1f, SCALE_ANIMATION_AMPLITUDE).apply {
      duration = SCALE_ANIMATION_DURATION_MS
      repeatMode = ObjectAnimator.REVERSE
      repeatCount = ObjectAnimator.INFINITE
      val onAnimatorEnd = { _: Animator ->
        scaleX = 1f
        scaleY = 1f
      }
      addListener(onEnd = onAnimatorEnd, onCancel = onAnimatorEnd)
      start()
    }

  private fun startClockShakeAnimation() {
    binding?.apply {
      clockAnimator?.cancel()
      clockAnimator =
        ObjectAnimator.ofFloat(clockBg, "translationX", -SHAKE_AMPLITUDE, SHAKE_AMPLITUDE).apply {
          duration = SHAKE_UNIT_DURATION_MS
          repeatMode = ObjectAnimator.REVERSE
          repeatCount =
            (alarmMediaPlayer?.remainingTime ?: DEFAULT_SHAKE_DURATION) / duration.toInt()
          val onAnimatorEnd = { _: Animator ->
            clockBg.translationX = 0f
          }
          addListener(onEnd = onAnimatorEnd, onCancel = onAnimatorEnd)
          start()
        }
    }
  }

  private fun stopClockShakeAnimation() {
    binding?.apply {
      clockAnimator?.cancel()
      clockAnimator = null
      clockBg.translationX = 0f
    }
  }

  private fun startVibration() {
    context?.apply {
      vibrator?.cancel()
      vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
      } else {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      }.apply {
        if (hasVibrator()) {
          vibrate(
            VibrationEffect.createOneShot(
              (alarmMediaPlayer?.remainingTime ?: DEFAULT_SHAKE_DURATION).toLong(),
              VIBRATION_AMPLITUDE
            )
          )
        }
      }
    }
  }

  private val MediaPlayer.remainingTime: Int
    get() = duration - currentPosition

  private fun autoReplayTickSound() {
    tickingMediaPlayer?.apply {
      if (currentPosition >= TICKS_REPLAY_INTERVAL_MS) {
        seekTo(currentPosition - TICKS_REPLAY_INTERVAL_MS)
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stopCountDown()
    tickingMediaPlayer?.release()
    tickingMediaPlayer = null
    quickTickingMediaPlayer?.release()
    quickTickingMediaPlayer = null
    alarmMediaPlayer?.release()
    alarmMediaPlayer = null
    binding = null
  }

  private fun Int.toTimeString(): String {
    return if (this < 10) {
      "0$this"
    } else {
      this.toString()
    }
  }
}
