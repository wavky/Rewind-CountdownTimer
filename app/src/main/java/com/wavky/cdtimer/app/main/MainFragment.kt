package com.wavky.cdtimer.app.main

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlin.math.roundToInt

private const val CLOCK_HAND_ROTATION_OFFSET = -90f

class MainFragment : BaseFragment() {

  private var binding: FragmentMainBinding? = null
  private var isCountdownStarted = false
  private var isMuted = false
  private var countdownTimer: Timer? = null
  private var tickSoundJob: Job? = null
  private var mediaPlayer: MediaPlayer? = null
  private var totalAngle: Float = 0f
  private val countDownSeconds: Int
    get() = (totalAngle / 6).roundToInt()
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
      }

      override fun onCircleGestureStart() {
        binding?.apply {
          circleGestureView.setCenterPoint(
            secondHand.x + secondHand.width / 2,
            secondHand.y + secondHand.height / 2
          )
        }
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
    mediaPlayer = MediaPlayer.create(requireContext(), R.raw.plastic_ticking).apply {
      isLooping = true
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
        isCountdownStarted = false
        resetClockHandRotation()
        updateCountDownTextDisplay()
        stopCountDown()
        startButton.text = getString(R.string.start)
      }
      startButton.setOnClickListener {
        if (!isCountdownStarted && totalAngle > 0) {
          isCountdownStarted = true
          startButton.text = getString(R.string.stop)
          startCountDown()
        } else {
          isCountdownStarted = false
          startButton.text = getString(R.string.start)
          stopCountDown()
        }
      }
      soundButton.setOnClickListener {
        if (isMuted) {
          isMuted = false
          mediaPlayer?.setVolume(1f, 1f)
          soundButton.imageTintList = null
          forbidMark.isGone = true
        } else {
          isMuted = true
          mediaPlayer?.setVolume(0f, 0f)
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
      if (mediaPlayer == null) {
        initMediaPlayer()
      }
      delay(1000)
      mediaPlayer?.start()
      while (true) {
        delay(300)
        if (totalAngle < 4) {
          stopTickSound()
        }
        replayTickSoundAfter(32)
      }
    }
    countdownTimer = fixedRateTimer("countdownTimer", false, 1000, 1000) {
      totalAngle -= 6
      if (totalAngle < 4) {
        totalAngle = 0f
        lifecycleScope.launch(Dispatchers.Main) {
          resetClockHandRotation()
          updateCountDownTextDisplay()
          binding?.startButton?.text = getString(R.string.start)
        }
        isCountdownStarted = false
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

  private fun stopTickSound() {
    mediaPlayer?.pause()
    mediaPlayer?.seekTo(0)
  }

  private fun replayTickSoundAfter(seconds: Int) {
    mediaPlayer?.apply {
      if (currentPosition >= seconds * 1000) {
        seekTo(currentPosition - seconds * 1000)
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stopCountDown()
    mediaPlayer?.release()
    mediaPlayer = null
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
