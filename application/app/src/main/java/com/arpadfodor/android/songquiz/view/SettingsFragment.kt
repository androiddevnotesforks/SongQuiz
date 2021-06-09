package com.arpadfodor.android.songquiz.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.viewmodel.SettingsViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var viewModel: SettingsViewModel

    private var keyRepeat = ""
    private var keySongDuration = ""
    private var keySeasonalThemes = ""
    private var keyClearCache = ""
    private var keyDeletePlaylists = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        keyRepeat = getString(R.string.SETTINGS_KEY_REPEAT)
        keySongDuration = getString(R.string.SETTINGS_KEY_SONG_DURATION)
        keySeasonalThemes = getString(R.string.SETTINGS_KEY_SEASONAL_THEMES)
        keyClearCache = getString(R.string.SETTINGS_KEY_CLEAR_CACHE)
        keyDeletePlaylists = getString(R.string.SETTINGS_KEY_DELETE_PLAYLISTS)

        val clearCachePref = findPreference<Preference>(keyClearCache)
        clearCachePref?.setOnPreferenceClickListener {
            showClearCacheDialog()
            true
        }

        val deletePlaylistsPref = findPreference<Preference>(keyDeletePlaylists)
        deletePlaylistsPref?.setOnPreferenceClickListener {
            showDeletePlaylistsDialog()
            true
        }

        val songDurationPref = findPreference<SeekBarPreference>(keySongDuration)
        songDurationPref?.let {
            val min = requireContext().resources.getInteger(R.integer.song_duration_sec_min)
            val max = requireContext().resources.getInteger(R.integer.song_duration_sec_max)
            val default = requireContext().resources.getInteger(R.integer.song_duration_sec_default)
            it.max = max
            it.min = min
            it.setDefaultValue(default)
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun showClearCacheDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.clear_cache),
            getString(R.string.clear_cache_description), R.drawable.icon_warning)
        dialog.setPositiveButton {
            Glide.get(requireContext()).clearMemory()
        }
        dialog.show()
    }

    private fun showDeletePlaylistsDialog() {
        val dialog = AppDialog(requireContext(), getString(R.string.delete_playlists),
            getString(R.string.delete_playlists_description), R.drawable.icon_warning)
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

}