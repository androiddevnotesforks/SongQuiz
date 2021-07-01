package com.aaronfodor.android.songquiz.view

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.view.utils.AppDialog
import com.aaronfodor.android.songquiz.view.utils.AuthRequestModule
import com.aaronfodor.android.songquiz.view.utils.AuthRequestContract
import com.aaronfodor.android.songquiz.viewmodel.SettingsAccountState
import com.aaronfodor.android.songquiz.viewmodel.SettingsUiState
import com.aaronfodor.android.songquiz.viewmodel.SettingsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, AuthRequestModule {

    private lateinit var viewModel: SettingsViewModel

    private var keyAccount = ""
    private var keyLoginout = ""
    private var keyRepeat = ""
    private var keySongDuration = ""
    private var keySeasonalThemes = ""
    private var keyClearCache = ""
    private var keyDeletePlaylists = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        keyAccount = getString(R.string.SETTINGS_KEY_ACCOUNT)
        keyLoginout = getString(R.string.SETTINGS_KEY_LOGINOUT)
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
                showLoggedIn(viewModel.getUserNameAndEmail())
            }
        }
        viewModel.accountState.observe(this, accountStateObserver)

        // to start, show current account state
        val currentAccountState = viewModel.accountState.value ?: SettingsAccountState.LOGGED_OUT
        if(currentAccountState == SettingsAccountState.LOGGED_OUT){
            showLoggedOut()
        }
        else{
            showLoggedIn(viewModel.getUserNameAndEmail())
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
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
        val accountPref = findPreference<Preference>(keyAccount)?.let {
            it.title = getString(R.string.settings_title_logged_out)
            it.summary = ""
        }

        val loginoutPref = findPreference<Preference>(keyLoginout)?.let {
            it.title = getString(R.string.settings_title_log_in)
            it.summary = getString(R.string.settings_summary_log_in)

            it.setOnPreferenceClickListener {
                startAuthentication()
                true
            }
        }
    }

    private fun showLoggedIn(accountNameAndEmail: Pair<String, String>){
        val accountPref = findPreference<Preference>(keyAccount)?.let {
            it.title = getString(R.string.settings_title_logged_in, accountNameAndEmail.first)
            it.summary = accountNameAndEmail.second
        }

        val loginoutPref = findPreference<Preference>(keyLoginout)?.let {
            it.title = getString(R.string.settings_title_log_out)
            it.summary = getString(R.string.settings_summary_log_out)

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
            viewModel.cacheCleared()
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
     * via Editor.clear, unless targeting [android.os.Build.VERSION_CODES.R]
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
                val valueRepeat = it.getBoolean(keyRepeat, true)
                putBoolean(keyRepeat, valueRepeat)

                remove(keySongDuration)
                val valueDuration = it.getInt(keySongDuration,
                    requireContext().resources.getInteger(R.integer.song_duration_sec_default))
                putInt(keySongDuration, valueDuration)

                remove(keySeasonalThemes)
                val valueSeasonalThemes = it.getBoolean(keySeasonalThemes, true)
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
        viewModel.ready()
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        authLauncherStarted = false
    }

}