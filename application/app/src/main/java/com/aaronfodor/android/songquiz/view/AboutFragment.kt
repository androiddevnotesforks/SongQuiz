package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
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
        binding.content.legalText.text = getString(R.string.app_copyright)

        val privacyOpenAction = {
            val privacyPageUri = Uri.parse(getString(R.string.privacy_page))
            val browserIntent = Intent(Intent.ACTION_VIEW, privacyPageUri)
            startActivity(browserIntent)
        }
        binding.content.action1.fab.setOnClickListener { privacyOpenAction() }
        binding.content.action1.tv.setOnClickListener { privacyOpenAction() }
        binding.content.action1.fab.setImageResource(R.drawable.icon_privacy)
        binding.content.action1.fab.contentDescription = getText(R.string.app_privacy)
        binding.content.action1.tv.text = getText(R.string.app_privacy)

        val licenseOpenAction = {
            val licensePageUri = Uri.parse(getString(R.string.license_page))
            val browserIntent = Intent(Intent.ACTION_VIEW, licensePageUri)
            startActivity(browserIntent)
        }
        binding.content.action2.fab.setOnClickListener { licenseOpenAction() }
        binding.content.action2.tv.setOnClickListener { licenseOpenAction() }
        binding.content.action2.fab.setImageResource(R.drawable.icon_license)
        binding.content.action2.fab.contentDescription = getText(R.string.app_license)
        binding.content.action2.tv.text = getText(R.string.app_license)

        val reviewAction = {
            val storePageUri = Uri.parse(getString(R.string.store_page, context?.packageName ?: ""))
            val storeIntent = Intent(Intent.ACTION_VIEW, storePageUri)
            startActivity(storeIntent)
        }
        binding.content.action3.fab.setOnClickListener { reviewAction() }
        binding.content.action3.tv.setOnClickListener { reviewAction() }
        binding.content.action3.fab.setImageResource(R.drawable.icon_review)
        binding.content.action3.fab.contentDescription = getText(R.string.review_app)
        binding.content.action3.tv.text = getText(R.string.review_app)

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
        binding.content.action4.fab.setOnClickListener { reportAction() }
        binding.content.action4.tv.setOnClickListener { reportAction() }
        binding.content.action4.fab.setImageResource(R.drawable.icon_bug)
        binding.content.action4.fab.contentDescription = getText(R.string.report)
        binding.content.action4.tv.text = getText(R.string.report)

        val moreFromDeveloperAction = {
            val developerPageUri = Uri.parse(getString(R.string.developer_page))
            val browserIntent = Intent(Intent.ACTION_VIEW, developerPageUri)
            startActivity(browserIntent)
        }
        binding.content.action5.fab.setOnClickListener { moreFromDeveloperAction() }
        binding.content.action5.tv.setOnClickListener { moreFromDeveloperAction() }
        binding.content.action5.fab.setImageResource(R.drawable.icon_more_apps)
        binding.content.action5.fab.contentDescription = getText(R.string.more_from_developer)
        binding.content.action5.tv.text = getText(R.string.more_from_developer)
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

                        var text = getString(R.string.app_name) + ". " + getString(R.string.about_text, appCreator, appDate, BuildConfig.VERSION_NAME) + " " +
                                getString(R.string.acknowledgments_title) + ". " + getString(R.string.acknowledgments_text) + " " +
                                getString(R.string.legal_title) + ". " + appCopyright + ". "
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

    override fun appearingAnimations() {
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
        binding.fabSpeak.startAnimation(rightAnimation)
        binding.fabSpeak.visibility = View.VISIBLE

        val leftAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        binding.content.action1.fab.startAnimation(leftAnimation)
        binding.content.action2.fab.startAnimation(leftAnimation)
        binding.content.action3.fab.startAnimation(leftAnimation)
        binding.content.action4.fab.startAnimation(leftAnimation)
        binding.content.action5.fab.startAnimation(leftAnimation)
    }

    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

}