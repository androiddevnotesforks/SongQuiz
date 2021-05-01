package com.arpadfodor.android.songquiz.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.CountDownTimer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class MediaPlayerService  @Inject constructor(
    @ApplicationContext val context: Context
) {

    var mediaPlayer: MediaPlayer? = null

    fun playUrlSound(soundUrl: String, finished: () -> Unit, error: () -> Unit){

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

    fun playLocalSound(soundName: String, finished: () -> Unit, error: () -> Unit){

        val assetFileDescriptor = context.assets.openFd(soundName)

        mediaPlayer = MediaPlayer().apply {

            setOnCompletionListener { player ->
                player.release()
                finished()
            }
            setOnErrorListener { player, what, extra ->
                player.release()
                error()
                true
            }

            setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
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