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
import androidx.lifecycle.lifecycleScope
import com.wavky.cdtimer.R
import com.wavky.cdtimer.app.main.CircleGestureView.OnCircleGestureListener
import com.wavky.cdtimer.common.app.ui.fragment.BaseFragment
import com.wavky.cdtimer.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

private const val CLOCK_HAND_ROTATION_OFFSET = -90f
private const val DEFAULT_SHAKE_DURATION = 10_000 // 10s
private const val VIBRATION_AMPLITUDE = 255 // max amplitude

class MainFragment : BaseFragment() {

  private var binding: FragmentMainBinding? = null
  private var isCountingDown = false
  private var isMuted = false
  private var countdownTimer: Timer? = null
  private var tickSoundJob: Job? = null
  private var tickingMediaPlayer: MediaPlayer? = null
  private var alarmMediaPlayer: MediaPlayer? = null
  private var clockAnimator: ObjectAnimator? = null
  private var vibrator: Vibrator? = null
  private var totalAngle: Float = 0f
  private val countDownSeconds: Int
    get() = (totalAngle / 6).toInt()
  private val countDownSecondsToDisplay
    get() = countDownSeconds % 60
  private val countDownMinutes
    get() = countDownSeconds / 60
  private val countDownMinutesToDisplay
    get() = countDownMinutes % 60
  private val countDownHours
    get() = countDownMinutes / 60
  private val countDownHoursToDisplay
    get() = countDownHours % 12

  private val onCircleGestureListener: OnCircleGestureListener
    get() = object : OnCircleGestureListener {
      override fun onTap() {
        stopAlarm()
      }

      override fun onCircleGesture(circleCount: Int, angle: Float, deltaAngle: Float) {
        binding?.apply {
          totalAngle += deltaAngle
          if (totalAngle < 0) {
            totalAngle = 0f
            resetClockHandRotation()
          } else {
            secondHand.rotation += deltaAngle
            minuteHand.rotation += deltaAngle / 60
            hourHand.rotation += deltaAngle / 720
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
        stopAlarm()
      }

      override fun onGestureFinish() {
        // Do something
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
      setVolume(1f, 1f)
    }
    alarmMediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm).apply {
      setVolume(1f, 1f)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding?.apply {
      resetClockHandRotation()
      updateCountDownTextDisplay()

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
        if (!isCountingDown && totalAngle > 0) {
          isCountingDown = true
          startButton.text = getString(R.string.stop)
          startCountDown()
        } else {
          isCountingDown = false
          startButton.text = getString(R.string.start)
          stopCountDown()
          stopAlarm()
        }
      }
      soundButton.setOnClickListener {
        if (isMuted) {
          isMuted = false
          tickingMediaPlayer?.setVolume(1f, 1f)
          alarmMediaPlayer?.setVolume(1f, 1f)
          soundButton.imageTintList = null
          forbidMark.isGone = true
        } else {
          isMuted = true
          tickingMediaPlayer?.setVolume(0f, 0f)
          alarmMediaPlayer?.setVolume(0f, 0f)
          soundButton.imageTintList =
            ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
          forbidMark.isVisible = true
        }
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
      if (countDownHoursToDisplay == 0) {
        timerHmsGroup.visibility = View.GONE
        timerMsGroup.visibility = View.VISIBLE

        msMinuteText.text = countDownMinutesToDisplay.toTimeString()
        msSecondText.text = countDownSecondsToDisplay.toTimeString()
      } else {
        timerHmsGroup.visibility = View.VISIBLE
        timerMsGroup.visibility = View.GONE

        hmsHourText.text = countDownHoursToDisplay.toTimeString()
        hmsMinuteText.text = countDownMinutesToDisplay.toTimeString()
        hmsSecondText.text = countDownSecondsToDisplay.toTimeString()
      }
    }
  }

  private fun startCountDown() {
    tickSoundJob = lifecycleScope.launch(Dispatchers.IO) {
      if (tickingMediaPlayer == null) {
        initMediaPlayer()
      }
      stopAlarm()
      delay(1000)
      tickingMediaPlayer?.start()
      while (true) {
        delay(300)
        if (totalAngle < 6) {
          stopTickSound()
          startAlarm()
          break
        }
        replayTickSoundAfter(32)
      }
    }
    countdownTimer = fixedRateTimer("countdownTimer", false, 1000, 1000) {
      totalAngle -= 6
      if (totalAngle < 6) {
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
          secondHand.rotation -= 6
          minuteHand.rotation -= 6 / 60f
          hourHand.rotation -= 6 / 720f
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

  private fun startClockShakeAnimation() {
    binding?.apply {
      clockAnimator?.cancel()
      clockAnimator = ObjectAnimator.ofFloat(clockBg, "translationX", -10f, 10f).apply {
        duration = 50
        repeatMode = ObjectAnimator.REVERSE
        repeatCount = (alarmMediaPlayer?.remainingTime ?: DEFAULT_SHAKE_DURATION) / duration.toInt()
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

  private fun replayTickSoundAfter(seconds: Int) {
    tickingMediaPlayer?.apply {
      if (currentPosition >= seconds * 1000) {
        seekTo(currentPosition - seconds * 1000)
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stopCountDown()
    tickingMediaPlayer?.release()
    tickingMediaPlayer = null
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
