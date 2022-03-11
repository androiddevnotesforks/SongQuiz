package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.BuildConfig
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentProfileBinding
import com.aaronfodor.android.songquiz.safeArithmetic
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelProfile
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.getTotalXP
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
                binding.profileImage.setImageResource(R.drawable.icon_profile)
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
                    .into(binding.profileImage)
            }

            binding.content.title.text = profile.name

            // total XP
            binding.content.totalXp.text = getString(R.string.stats_xp, profile.getTotalXP().toString())

            // single player stats
            binding.content.singleStat1.name.text = getString(R.string.stats_total_play)
            binding.content.singleStat1.score.text = profile.single_NumGamesPlayed.toString()

            binding.content.singleStat2.name.text = getString(R.string.stats_total_win)
            binding.content.singleStat2.score.text = profile.single_TotalWins.toString()
            var calculatedValue = safeArithmetic { (profile.single_TotalWins.toDouble() / profile.single_NumGamesPlayed) * 100 }
            binding.content.singleStat2.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.singleStat3.name.text = getString(R.string.stats_total_tie)
            binding.content.singleStat3.score.text = profile.single_TotalTies.toString()
            calculatedValue = safeArithmetic { (profile.single_TotalTies.toDouble() / profile.single_NumGamesPlayed) * 100 }
            binding.content.singleStat3.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.singleStat4.name.text = getString(R.string.stats_total_songs)
            binding.content.singleStat4.score.text = profile.single_TotalNumSongs.toString()

            binding.content.singleStat5.name.text = getString(R.string.stats_total_artist_hits)
            binding.content.singleStat5.score.text = profile.single_TotalArtistHits.toString()
            calculatedValue = safeArithmetic { (profile.single_TotalArtistHits.toDouble() / profile.single_TotalNumSongs) * 100 }
            binding.content.singleStat5.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.singleStat6.name.text = getString(R.string.stats_total_title_hits)
            binding.content.singleStat6.score.text = profile.single_TotalTitleHits.toString()
            calculatedValue = safeArithmetic { (profile.single_TotalTitleHits.toDouble() / profile.single_TotalNumSongs) * 100 }
            binding.content.singleStat6.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.singleStat7.name.text = getString(R.string.stats_avg_song_length)
            calculatedValue = safeArithmetic { profile.single_TotalSongLength.toDouble() / profile.single_TotalNumSongs }
            binding.content.singleStat7.score.text = getString(R.string.stats_sec, String.format("%.2f", calculatedValue))

            binding.content.singleStat8.name.text = getString(R.string.stats_avg_song_difficulty)
            calculatedValue = safeArithmetic { profile.single_TotalSongDifficulty.toDouble() / profile.single_TotalNumSongs }
            binding.content.singleStat8.score.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            // multiplayer stats
            binding.content.multiStat1.name.text = getString(R.string.stats_total_play)
            binding.content.multiStat1.score.text = profile.multi_NumGamesPlayed.toString()

            binding.content.multiStat2.name.text = getString(R.string.stats_avg_num_players)
            calculatedValue = safeArithmetic { profile.multi_TotalNumPlayers.toDouble() / profile.multi_NumGamesPlayed }
            binding.content.multiStat2.score.text = String.format("%.2f", calculatedValue)

            binding.content.multiStat3.name.text = getString(R.string.stats_total_tie)
            binding.content.multiStat3.score.text = profile.multi_TotalTies.toString()
            calculatedValue = safeArithmetic { (profile.multi_TotalTies.toDouble() / profile.multi_NumGamesPlayed) * 100 }
            binding.content.multiStat3.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.multiStat4.name.text = getString(R.string.stats_total_songs)
            binding.content.multiStat4.score.text = profile.multi_TotalNumSongs.toString()

            binding.content.multiStat5.name.text = getString(R.string.stats_total_artist_hits)
            binding.content.multiStat5.score.text = profile.multi_TotalArtistHits.toString()
            calculatedValue = safeArithmetic { (profile.multi_TotalArtistHits.toDouble() / profile.multi_TotalNumSongs) * 100 }
            binding.content.multiStat5.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.multiStat6.name.text = getString(R.string.stats_total_title_hits)
            binding.content.multiStat6.score.text = profile.multi_TotalTitleHits.toString()
            calculatedValue = safeArithmetic { (profile.multi_TotalTitleHits.toDouble() / profile.multi_TotalNumSongs) * 100 }
            binding.content.multiStat6.percentage.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))

            binding.content.multiStat7.name.text = getString(R.string.stats_avg_song_length)
            calculatedValue = safeArithmetic { profile.multi_TotalSongLength.toDouble() / profile.multi_TotalNumSongs }
            binding.content.multiStat7.score.text = getString(R.string.stats_sec, String.format("%.2f", calculatedValue))

            binding.content.multiStat8.name.text = getString(R.string.stats_avg_song_difficulty)
            calculatedValue = safeArithmetic { profile.multi_TotalSongDifficulty.toDouble() / profile.multi_TotalNumSongs }
            binding.content.multiStat8.score.text = getString(R.string.stats_percentage, String.format("%.2f", calculatedValue))
        }
        viewModel.currentProfile.observe(this, profileObserver)

        val notificationObserver = Observer<ProfileNotification> { notification ->
            when(notification){
                ProfileNotification.LOGGED_OUT -> {
                    Snackbar.make(this.requireView(), getString(R.string.account_logged_out), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(ProfileNotification.NONE)
                }
                ProfileNotification.PROFILE_STATS_DELETED -> {
                    Snackbar.make(this.requireView(), getString(R.string.profile_stats_deleted), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(ProfileNotification.NONE)
                }
                ProfileNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)

        viewModel.subscribeTtsListeners()
        val ttsStateObserver = Observer<TtsProfileState> { state ->
            when(state){
                TtsProfileState.ENABLED -> {
                    binding.fabMain.setImageResource(R.drawable.icon_sound_on)
                    binding.fabMain.setOnClickListener {
                        viewModel.currentProfile.value?.let {
                            if(it.id.isNotBlank()){
                                val singlePlayerText = getString(R.string.stats_single_player)
                                val multiplayerText = getString(R.string.stats_multiplayer)
                                var text = it.name + ". " + getString(R.string.stats_spoken, it.getTotalXP().toString(),
                                    it.single_NumGamesPlayed.toString(), singlePlayerText, it.multi_NumGamesPlayed.toString(), multiplayerText)
                                text = text.replace("\n\n", ".\n\n").replace("..", ".").replace(" . ", " ")
                                viewModel.speak(text)
                            }
                            else{
                                val text = getString(R.string.stats_login)
                                viewModel.speak(text)
                            }
                        }
                    }
                }
                TtsProfileState.SPEAKING -> {
                    binding.fabMain.setImageResource(R.drawable.icon_sound_off)
                    binding.fabMain.setOnClickListener {
                        viewModel.stopSpeaking()
                    }
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        binding.tvLogin.text = getString(R.string.settings_summary_log_in)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_login)
        binding.tvLogin.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
        binding.tvLogin.setOnClickListener {
            startAuthentication()
        }

        val logoutContentDescription = getString(R.string.logout)
        binding.content.action1.tv.text = logoutContentDescription
        binding.content.action1.tv.setOnClickListener { showLogoutDialog() }
        binding.content.action1.fab.setImageResource(R.drawable.icon_logout)
        binding.content.action1.fab.contentDescription = logoutContentDescription
        binding.content.action1.fab.setOnClickListener { showLogoutDialog() }

        val deleteProfileStatsContentDescription = getString(R.string.delete_profile_stats)
        binding.content.action2.tv.text = deleteProfileStatsContentDescription
        binding.content.action2.tv.setOnClickListener { showDeleteProfileStatsDialog() }
        binding.content.action2.fab.setImageResource(R.drawable.icon_delete)
        binding.content.action2.fab.contentDescription = deleteProfileStatsContentDescription
        binding.content.action2.fab.setOnClickListener { showDeleteProfileStatsDialog() }
    }

    private fun showLogoutDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.logout),
            getString(R.string.logout_description), R.drawable.icon_logout)
        dialog.setPositiveButton {
            viewModel.logout()
        }
        dialog.show()
    }

    private fun showDeleteProfileStatsDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.delete_profile_stats),
            getString(R.string.delete_profile_stats_description), R.drawable.icon_delete)
        dialog.setPositiveButton {
            // delete profile stats
            viewModel.deleteProfileStats()
        }
        dialog.show()
    }

    override fun appearingAnimations() {
        binding.fabMain.appear(R.anim.slide_in_right, true)
        binding.content.action1.fab.appear(R.anim.slide_in_left, true)
        binding.content.action2.fab.appear(R.anim.slide_in_left, true)

        if(binding.tvLogin.visibility == View.VISIBLE){
            binding.tvLogin.appear(R.anim.slide_in_bottom)
        }
    }

    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        authLauncherStarted = false
    }

}