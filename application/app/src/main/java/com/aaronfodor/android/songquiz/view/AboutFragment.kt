package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.BuildConfig
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentAboutBinding
import com.aaronfodor.android.songquiz.view.utils.AppFragment
import com.aaronfodor.android.songquiz.viewmodel.AboutViewModel
import com.aaronfodor.android.songquiz.viewmodel.TtsAboutState

class AboutFragment : AppFragment(R.layout.fragment_about) {

    private val binding: FragmentAboutBinding by viewBinding()

    private lateinit var viewModel: AboutViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)

        val appCreator = getString(R.string.app_creator)
        val appDate = getString(R.string.app_date)
        binding.content.aboutText.text = getString(R.string.about_text, appCreator, appDate, BuildConfig.VERSION_NAME)

        val appCopyright = getString(R.string.app_copyright)
        binding.content.legalText.text = getString(R.string.legal_text, appCopyright)

        val moreFromDeveloperAction = {
            val developerPageUri = Uri.parse(getString(R.string.developer_page))
            val browserIntent = Intent(Intent.ACTION_VIEW, developerPageUri)
            startActivity(browserIntent)
        }
        binding.content.fabMoreFromDeveloper.setOnClickListener { moreFromDeveloperAction() }
        binding.content.tvMoreFromDeveloper.setOnClickListener { moreFromDeveloperAction() }

        val reviewAction = {
            val storePageUri = Uri.parse(getString(R.string.store_page, context?.packageName ?: ""))
            val storeIntent = Intent(Intent.ACTION_VIEW, storePageUri)
            startActivity(storeIntent)
        }
        binding.content.fabReview.setOnClickListener { reviewAction() }
        binding.content.tvReview.setOnClickListener { reviewAction() }

        val reportAction = {
            val reportIntent = Intent(Intent.ACTION_SENDTO).apply {
                val appName = getString(R.string.app_name)
                data = Uri.parse("mailto:")
                // mailto must be an array of addresses
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.report_contact)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_message_title, appName))
            }
            startActivity(reportIntent)
        }
        binding.content.fabReport.setOnClickListener { reportAction() }
        binding.content.tvReport.setOnClickListener { reportAction() }
    }

    override fun subscribeViewModel() {
        viewModel.subscribeTtsListeners()
        val ttsStateObserver = Observer<TtsAboutState> { state ->
            when(state){
                TtsAboutState.ENABLED -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_on)
                    binding.fabSpeak.setOnClickListener {
                        val appDate = getString(R.string.app_date)
                        val appCreator = getString(R.string.app_creator)
                        val appCopyright = getString(R.string.app_copyright)

                        var text = getString(R.string.app_name) + ". " + getString(R.string.about_text, appCreator, appDate, BuildConfig.VERSION_NAME) +
                                " " + getString(R.string.legal_title) + ". " + getString(R.string.legal_text, appCopyright) +
                                " " + getString(R.string.acknowledgments_title) + ". " + getString(R.string.acknowledgments_text)
                        text = text.replace("\n\n", ".\n\n").replace("..", ".").replace(" . ", " ")
                        viewModel.speak(text)
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
    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

}