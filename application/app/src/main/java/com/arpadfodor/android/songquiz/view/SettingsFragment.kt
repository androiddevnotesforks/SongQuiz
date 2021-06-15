package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.viewmodel.SettingsAccountState
import com.arpadfodor.android.songquiz.viewmodel.SettingsUiState
import com.arpadfodor.android.songquiz.viewmodel.SettingsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var viewModel: SettingsViewModel

    private var keyLogout = ""
    private var keyRepeat = ""
    private var keySongDuration = ""
    private var keySeasonalThemes = ""
    private var keyClearCache = ""
    private var keyDeletePlaylists = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        keyLogout = getString(R.string.SETTINGS_KEY_LOGOUT)
        keyRepeat = getString(R.string.SETTINGS_KEY_REPEAT)
        keySongDuration = getString(R.string.SETTINGS_KEY_SONG_DURATION)
        keySeasonalThemes = getString(R.string.SETTINGS_KEY_SEASONAL_THEMES)
        keyClearCache = getString(R.string.SETTINGS_KEY_CLEAR_CACHE)
        keyDeletePlaylists = getString(R.string.SETTINGS_KEY_DELETE_ALL_PLAYLISTS)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        subscribeViewModel()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val clearCachePref = findPreference<Preference>(keyClearCache)?.setOnPreferenceClickListener {
            showClearCacheDialog()
            true
        }

        val deletePlaylistsPref = findPreference<Preference>(keyDeletePlaylists)?.setOnPreferenceClickListener {
            showDeletePlaylistsDialog()
            true
        }

        val songDurationPref = findPreference<SeekBarPreference>(keySongDuration)?.let {
            val min = requireContext().resources.getInteger(R.integer.song_duration_sec_min)
            val max = requireContext().resources.getInteger(R.integer.song_duration_sec_max)
            val default = requireContext().resources.getInteger(R.integer.song_duration_sec_default)
            it.max = max
            it.min = min
            it.setDefaultValue(default)
        }
    }

    private fun subscribeViewModel() {
        val uiStateObserver = Observer<SettingsUiState> { state ->
            when(state){
                SettingsUiState.CACHE_CLEARED -> {
                    showInfo(SettingsUiState.CACHE_CLEARED)
                }
                SettingsUiState.PLAYLISTS_DELETED -> {
                    showInfo(SettingsUiState.PLAYLISTS_DELETED)
                }
                SettingsUiState.ACCOUNT_LOGGED_OUT -> {
                    showInfo(SettingsUiState.ACCOUNT_LOGGED_OUT)
                }
                else -> {}
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val accountStateObserver = Observer<SettingsAccountState> { accountState ->
            if(accountState == SettingsAccountState.LOGGED_OUT){
                showLoggedOut()
            }
            else{
                showLoggedIn(viewModel.getUserName())
            }
        }
        viewModel.accountState.observe(this, accountStateObserver)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun authenticate(){
        val intent = Intent(this.requireContext(), AuthActivity::class.java)
        // After auth finished, return to the caller screen by removing the auth screen
        intent.putExtra(AuthActivity.DESTROY_SELF_WHEN_READY_KEY, true)
        startActivity(intent)
    }

    private fun showLogoutDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.logout),
            getString(R.string.logout_description), R.drawable.icon_warning)
        dialog.setPositiveButton {
            viewModel.logout()
        }
        dialog.show()
    }

    private fun showLoggedOut(){
        val logoutPref = findPreference<Preference>(keyLogout)?.let {
            it.title = getString(R.string.settings_title_logged_out)
            it.summary = getString(R.string.settings_summary_login)

            it.setOnPreferenceClickListener {
                authenticate()
                true
            }
        }
    }

    private fun showLoggedIn(accountName: String){
        val logoutPref = findPreference<Preference>(keyLogout)?.let {
            it.title = getString(R.string.settings_title_logout, accountName)
            it.summary = getString(R.string.settings_summary_logout)

            it.setOnPreferenceClickListener {
                showLogoutDialog()
                true
            }
        }
    }

    private fun showClearCacheDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.clear_cache),
            getString(R.string.clear_cache_description), R.drawable.icon_warning)
        dialog.setPositiveButton {
            Glide.get(requireContext()).clearMemory()
            viewModel.uiState.postValue(SettingsUiState.CACHE_CLEARED)
        }
        dialog.show()
    }

    private fun showDeletePlaylistsDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.delete_playlists),
            getString(R.string.delete_playlists_description), R.drawable.icon_delete)
        dialog.setPositiveButton {
            // delete playlists
            viewModel.deletePlaylists()
            // delete Glide local disk cache too; blocks main thread so schedule on an IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                Glide.get(requireContext()).clearDiskCache()
            }
        }
        dialog.show()
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * This callback will be run on your main thread.
     *
     * *Note: This callback will not be triggered when preferences are cleared
     * via [Editor.clear], unless targeting [android.os.Build.VERSION_CODES.R]
     * on devices running OS versions [Android R][android.os.Build.VERSION_CODES.R]
     * or later.*
     *
     * @param sharedPreferences The [SharedPreferences] that received the change.
     * @param key The key of the preference that was changed, added, or removed. Apps targeting
     * [android.os.Build.VERSION_CODES.R] on devices running OS versions
     * [Android R][android.os.Build.VERSION_CODES.R] or later, will receive
     * a `null` value when preferences are cleared.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            with (it.edit()){
                remove(keyRepeat)
                val valueRepeat = it.getBoolean(keyRepeat, false)
                putBoolean(keyRepeat, valueRepeat)

                remove(keySongDuration)
                val valueDuration = it.getInt(keySongDuration, 0)
                putInt(keySongDuration, valueDuration)

                remove(keySeasonalThemes)
                val valueSeasonalThemes = it.getBoolean(keySeasonalThemes, false)
                putBoolean(keySeasonalThemes, valueSeasonalThemes)

                apply()
            }
        }
    }

    private fun showInfo(infoType: SettingsUiState){
        val message = when(infoType){
            SettingsUiState.CACHE_CLEARED -> {
                getString(R.string.cache_cleared)
            }
            SettingsUiState.PLAYLISTS_DELETED -> {
                getString(R.string.playlists_deleted)
            }
            SettingsUiState.ACCOUNT_LOGGED_OUT -> {
                getString(R.string.account_forgot)
            }
            else -> {
                ""
            }
        }
        Snackbar.make(this.requireView(), message, Snackbar.LENGTH_LONG).show()
        viewModel.uiState.postValue(SettingsUiState.READY)
    }

}