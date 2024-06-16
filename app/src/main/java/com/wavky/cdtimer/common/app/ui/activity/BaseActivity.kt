package com.wavky.cdtimer.common.app.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wavky.cdtimer.common.const.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseActivity : AppCompatActivity() {

  protected open val finishOnBackPress = true

  private var contentView: View? = null
  private var systemBarHidingTimerJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    obverseViewModel()
  }

  override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    super.onCreate(savedInstanceState, persistentState)
    obverseViewModel()
  }

  override fun onResume() {
    super.onResume()
    if (!finishOnBackPress) {
      onBackPressedDispatcher.addCallback(this) {}
    }
//    hideSystemBars()
  }

  override fun setContentView(view: View) {
    super.setContentView(view)
    contentView = view
  }

  private fun hideSystemBars() {
    systemBarHidingTimerJob = lifecycleScope.launch(Dispatchers.IO) {
      while (true) {
        withContext(Dispatchers.Main) {
          contentView?.hideSystemBars()
        }
        delay(Config.SYSTEM_BAR_HIDING_INTERVAL_MS)
      }
    }
  }

  override fun onPause() {
    super.onPause()
    systemBarHidingTimerJob?.cancel()
  }

  private fun View.hideSystemBars() {
    windowInsetsController?.hide(android.view.WindowInsets.Type.systemBars())
  }

  protected open fun obverseViewModel() {}

}
