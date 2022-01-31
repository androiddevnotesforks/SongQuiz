package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.model.database.ApplicationDB
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
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, AuthRequestModule {

    private lateinit var viewModel: SettingsViewModel

    @Inject
    lateinit var database: ApplicationDB

    // preferences
    private var keyAccount = ""
    private var keyRepeat = ""
    private var keySongDuration = ""
    private var keyExtendedQuizInfo = ""
    private var keySeasonalThemes = ""
    private var keyLanguage = ""
    private var keySpeech = ""
    private var keyOnboarding = ""
    private var keyClearCache = ""
    private var keyDeletePlaylists = ""
    private var keyRestoreDefaultDb = ""

    // boarding flag keys
    private var keyOnboardingMenuShowed = ""
    private var keyOnboardingQuizShowed = ""
    private var keyOnboardingInfoShowed = ""
    private var keyOnboardingAboutShowed = ""
    private var keyOnboardingPlaylistAddShowed = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        // preferences
        keyAccount = getString(R.string.SETTINGS_KEY_ACCOUNT)
        keyRepeat = getString(R.string.SETTINGS_KEY_REPEAT)
        keySongDuration = getString(R.string.SETTINGS_KEY_SONG_DURATION)
        keyExtendedQuizInfo = getString(R.string.SETTINGS_KEY_EXTENDED_QUIZ_INFO)
        keySeasonalThemes = getString(R.string.SETTINGS_KEY_SEASONAL_THEMES)
        keyLanguage = getString(R.string.SETTINGS_KEY_LANGUAGE)
        keySpeech = getString(R.string.SETTINGS_KEY_SPEECH)
        keyOnboarding = getString(R.string.SETTINGS_KEY_ONBOARDING)
        keyClearCache = getString(R.string.SETTINGS_KEY_CLEAR_CACHE)
        keyDeletePlaylists = getString(R.string.SETTINGS_KEY_DELETE_ALL_PLAYLISTS)
        keyRestoreDefaultDb = getString(R.string.SETTINGS_KEY_RESTORE_DEFAULT_DB)
        // boarding flag keys
        keyOnboardingMenuShowed = getString(R.string.PREF_KEY_ONBOARDING_MENU_SHOWED)
        keyOnboardingQuizShowed = getString(R.string.PREF_KEY_ONBOARDING_QUIZ_SHOWED)
        keyOnboardingInfoShowed = getString(R.string.PREF_KEY_ONBOARDING_INFO_SHOWED)
        keyOnboardingAboutShowed = getString(R.string.PREF_KEY_ONBOARDING_ABOUT_SHOWED)
        keyOnboardingPlaylistAddShowed = getString(R.string.PREF_KEY_ONBOARDING_PLAYLIST_ADD_SHOWED)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        subscribeViewModel()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        val clearCachePref = findPreference<Preference>(keyClearCache)?.setOnPreferenceClickListener {
            showClearCacheDialog()
            true
        }

        val deletePlaylistsPref = findPreference<Preference>(keyDeletePlaylists)?.setOnPreferenceClickListener {
            showDeletePlaylistsDialog()
            true
        }

        val restoreDefaultDbPref = findPreference<Preference>(keyRestoreDefaultDb)?.setOnPreferenceClickListener {
            showRestoreDefaultDbDialog()
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

        val languagePref = findPreference<Preference>(keyLanguage)?.setOnPreferenceClickListener {
            // Open Android Language settings
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)
            true
        }

        val speechPref = findPreference<Preference>(keySpeech)?.setOnPreferenceClickListener {
            // Open Android Text-To-Speech settings
            val intent = Intent().setAction("com.android.settings.TTS_SETTINGS").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)
            true
        }

        val onboardingPref = findPreference<Preference>(keyOnboarding)?.setOnPreferenceClickListener {
            // clear onboarding flags
            val sharedPreferences = context?.let {
                PreferenceManager.getDefaultSharedPreferences(it)
            }

            sharedPreferences?.let {
                with(it.edit()){
                    remove(keyOnboardingMenuShowed)
                    putBoolean(keyOnboardingMenuShowed, false)
                    remove(keyOnboardingQuizShowed)
                    putBoolean(keyOnboardingQuizShowed, false)
                    remove(keyOnboardingInfoShowed)
                    putBoolean(keyOnboardingInfoShowed, false)
                    remove(keyOnboardingAboutShowed)
                    putBoolean(keyOnboardingAboutShowed, false)
                    remove(keyOnboardingPlaylistAddShowed)
                    putBoolean(keyOnboardingPlaylistAddShowed, false)
                    apply()
                }
            }

            // "restart" start MenuActivity
            val intent = Intent(requireContext(), MenuActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            this.startActivity(intent)
            true
        }
    }

    private fun subscribeViewModel() {
        val uiStateObserver = Observer<SettingsUiState> { state ->
            when(state){
                SettingsUiState.CACHE_CLEARED -> {
                    showInfo(state)
                }
                SettingsUiState.PLAYLISTS_DELETED -> {
                    showInfo(state)
                }
                SettingsUiState.DEFAULT_DB_RESTORED -> {
                    showInfo(state)
                }
                SettingsUiState.ACCOUNT_LOGGED_OUT -> {
                    showInfo(state)
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
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
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
            it.summary = accountNameAndEmail.second + "\n" + getString(R.string.settings_summary_log_out)

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

    private fun showRestoreDefaultDbDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.restore_default_db),
            getString(R.string.restore_default_db_description), R.drawable.icon_restore)
        dialog.setPositiveButton {
            CoroutineScope(Dispatchers.IO).launch {
                // copy default database content to current database
                val assetManager = requireContext().assets
                val defaultDbStream = assetManager.open(ApplicationDB.DEFAULT_DB_FILE_PATH)
                val currentDbStream = requireContext().getDatabasePath(ApplicationDB.APPLICATION_DB_NAME).outputStream()
                defaultDbStream.copyTo(currentDbStream)
                // delete Glide local disk cache too; blocks main thread so schedule on an IO dispatcher
                Glide.get(requireContext()).clearDiskCache()
                // notify viewModel
                viewModel.restoredDefaultDB()
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
                // preferences
                remove(keyRepeat)
                val valueRepeat = it.getBoolean(keyRepeat, true)
                putBoolean(keyRepeat, valueRepeat)

                remove(keySongDuration)
                val valueDuration = it.getInt(keySongDuration, requireContext().resources.getInteger(R.integer.song_duration_sec_default))
                putInt(keySongDuration, valueDuration)

                remove(keyExtendedQuizInfo)
                val valueExtendedQuizInfo = it.getBoolean(keyExtendedQuizInfo, true)
                putBoolean(keyExtendedQuizInfo, valueExtendedQuizInfo)

                remove(keySeasonalThemes)
                val valueSeasonalThemes = it.getBoolean(keySeasonalThemes, true)
                putBoolean(keySeasonalThemes, valueSeasonalThemes)

                // persist changes
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
            SettingsUiState.DEFAULT_DB_RESTORED -> {
                getString(R.string.default_db_restored)
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