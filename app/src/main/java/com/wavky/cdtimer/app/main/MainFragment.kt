package com.wavky.cdtimer.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavky.cdtimer.common.app.ui.fragment.BaseFragment
import com.wavky.cdtimer.databinding.FragmentMainBinding

class MainFragment : BaseFragment() {

  private var binding: FragmentMainBinding? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    binding = null
  }
}
