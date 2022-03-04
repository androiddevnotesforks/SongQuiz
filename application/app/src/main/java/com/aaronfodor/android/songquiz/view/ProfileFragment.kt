package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentProfileBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelProfile
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : AppFragment(R.layout.fragment_profile), AuthRequestModule {

    private val binding: FragmentProfileBinding by viewBinding()

    override lateinit var viewModel: ProfileViewModel

    var imageSize = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        imageSize = resources.getDimension(R.dimen.profile_image_pixels).toInt()
    }

    override fun subscribeViewModel() {
        // needed to trigger refresh when UI is shown again
        viewModel.loadData()

        val profileObserver = Observer<ViewModelProfile> { profile ->
            if(profile.id.isBlank()){
                binding.content.root.visibility = View.INVISIBLE
                binding.tvLogin.appear(R.anim.slide_in_bottom)
            }
            else{
                binding.content.root.visibility = View.VISIBLE
                binding.tvLogin.disappear(R.anim.slide_out_bottom)
            }

            // set profile-related data
            if(profile.imageUri.isEmpty()){
                binding.content.profileImage.setImageResource(R.drawable.icon_profile)
            }
            else{
                val options = RequestOptions()
                    .centerCrop()
                    // better image quality: 4 bytes per pixel
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    // specific, small image needed as thumbnail
                    .override(imageSize, imageSize)
                    .placeholder(R.drawable.icon_profile)
                    .error(R.drawable.icon_profile)

                Glide.with(this)
                    .load(profile.imageUri)
                    .transition(DrawableTransitionOptions.with(DrawableCrossFadeFactory()))
                    .apply(options)
                    .into(binding.content.profileImage)
            }

            binding.content.title.text = profile.name

        }
        viewModel.currentProfile.observe(this, profileObserver)

        val notificationObserver = Observer<ProfileNotification> { notification ->
            when(notification){
                ProfileNotification.LOGGED_OUT -> {
                    Snackbar.make(this.requireView(), getString(R.string.account_forgot), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(ProfileNotification.NONE)
                }
                ProfileNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)

        binding.tvLogin.text = getString(R.string.settings_summary_log_in)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_login)
        binding.tvLogin.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
        binding.tvLogin.setOnClickListener {
            startAuthentication()
        }

        val logoutContentDescription = getString(R.string.logout)

        binding.content.action.tv.text = logoutContentDescription
        binding.content.action.tv.setOnClickListener { showLogoutDialog() }

        binding.content.action.fab.setImageResource(R.drawable.icon_logout)
        binding.content.action.fab.contentDescription = logoutContentDescription
        binding.content.action.fab.setOnClickListener { showLogoutDialog() }
    }

    private fun showLogoutDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.logout),
            getString(R.string.logout_description), R.drawable.icon_logout)
        dialog.setPositiveButton {
            viewModel.logout()
        }
        dialog.show()
    }

    override fun appearingAnimations() {
        binding.fabMain.appear(R.anim.slide_in_right, true)
        binding.content.action.fab.appear(R.anim.slide_in_left, true)

        if(binding.tvLogin.visibility == View.VISIBLE){
            binding.tvLogin.appear(R.anim.slide_in_bottom)
        }
    }

    override fun unsubscribeViewModel() {}

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        authLauncherStarted = false
    }

}