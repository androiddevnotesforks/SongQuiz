package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.FragmentAboutBinding
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.viewmodel.AboutViewModel
import com.arpadfodor.android.songquiz.viewmodel.TtsAboutState

class AboutFragment : AppFragment() {

    private var _binding: FragmentAboutBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private lateinit var viewModel: AboutViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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