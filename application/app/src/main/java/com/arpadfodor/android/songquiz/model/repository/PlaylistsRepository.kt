package com.arpadfodor.android.songquiz.model.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class PlaylistsRepository @Inject constructor(
    @ApplicationContext val context: Context
) {

}