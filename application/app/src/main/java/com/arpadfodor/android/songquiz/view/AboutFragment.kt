package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.FragmentAboutBinding
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.viewmodel.AboutViewModel
import com.arpadfodor.android.songquiz.viewmodel.TtsAboutState

class AboutFragment : AppFragment(R.layout.fragment_about) {

    private val binding: FragmentAboutBinding by viewBinding()

    private lateinit var viewModel: AboutViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)

        binding.fabSpeak.setOnClickListener {
            viewModel.speak(getString(R.string.about_text))
        }

        binding.fabMoreFromDeveloper.setOnClickListener {
            val developerPageUri = Uri.parse(getString(R.string.developer_page))
            val browserIntent = Intent(Intent.ACTION_VIEW, developerPageUri)
            startActivity(browserIntent)
        }

        binding.fabReview.setOnClickListener {
            val storePageUri = Uri.parse(getString(R.string.store_page, context?.packageName ?: ""))
            val storeIntent = Intent(Intent.ACTION_VIEW, storePageUri)
            startActivity(storeIntent)
        }

        binding.fabBugReport.setOnClickListener {
            val reportIntent = Intent(Intent.ACTION_SENDTO).apply {
                val appName = getString(R.string.app_name)
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, getString(R.string.maintenance_contact))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.maintenance_message_title, appName))

            }
            startActivity(reportIntent)
        }

    }

    override fun subscribeViewModel() {
        val ttsStateObserver = Observer<TtsAboutState> { state ->
            when(state){
                TtsAboutState.ENABLED -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_on)
                    binding.fabSpeak.setOnClickListener {
                        viewModel.speak(getString(R.string.about_text))
                    }
                }
                TtsAboutState.SPEAKING -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_off)
                    binding.fabSpeak.setOnClickListener {
                        viewModel.stopSpeaking()
                    }
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

}