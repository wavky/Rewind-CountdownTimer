package com.wavky.cdtimer.app.main

import android.os.Bundle
import androidx.navigation.findNavController
import com.wavky.cdtimer.R
import com.wavky.cdtimer.common.app.ui.activity.BaseActivity
import com.wavky.cdtimer.common.app.ui.viewmodel.resetObserversWith
import com.wavky.cdtimer.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

  private val viewModel: MainViewModel by viewModel()
  private val binding: ActivityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val navController
    get() = findNavController(R.id.nav_host_fragment)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
  }

  override fun obverseViewModel() {
    viewModel.resetObserversWith(this) {
      // Observe live data here
    }
  }
}
