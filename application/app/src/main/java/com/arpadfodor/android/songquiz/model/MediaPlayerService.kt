package com.arpadfodor.android.songquiz.model

import android.media.AudioAttributes
import android.media.MediaPlayer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class MediaPlayerService  @Inject constructor() {

    var mediaPlayer: MediaPlayer? = null

    fun play(soundUrl: String, finished: () -> Unit, error: () -> Unit){

        mediaPlayer = MediaPlayer().apply {

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setOnCompletionListener { player ->
                player.release()
                finished()
            }
            setOnErrorListener { player, what, extra ->
                player.release()
                error()
                true
            }

            setDataSource(soundUrl)
            prepare()
            start()

        }

    }

    fun stop(){
        mediaPlayer?.apply {
            release()
        }
    }

}