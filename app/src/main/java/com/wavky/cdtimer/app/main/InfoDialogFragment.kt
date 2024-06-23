package com.wavky.cdtimer.app.main

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.wavky.cdtimer.BuildConfig
import com.wavky.cdtimer.R
import com.wavky.cdtimer.databinding.DialogInfoBinding

class InfoDialogFragment : DialogFragment() {

  private var binding: DialogInfoBinding? = null

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return Dialog(requireContext(), R.style.dialog).apply {
      setCanceledOnTouchOutside(true)
      setCancelable(true)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    binding = DialogInfoBinding.inflate(inflater, container, false)
    return binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding?.apply {
      version.text = getString(R.string.version, BuildConfig.VERSION_NAME)
      githubBox.setOnClickListener {
        openUrl(getString(R.string.github_url))
      }
    }
  }

  private fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
  }
}
