package com.aaronfodor.android.songquiz.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
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

    private var mediaPlayer: MediaPlayer? = null

    fun playUrlSound(soundUrl: String, finished: () -> Unit, error: () -> Unit) : Boolean{

        mediaPlayer = MediaPlayer().apply {

            try{
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
                setOnPreparedListener {
                    start()
                }

                setDataSource(soundUrl)
                prepare()
            }
            catch (e: Exception){
                error()
                return false
            }

        }

        return true
    }

    fun playLocalSound(soundName: String, finished: () -> Unit, error: () -> Unit) : Boolean{

        val assetFileDescriptor = context.assets.openFd(soundName)

        mediaPlayer = MediaPlayer().apply {

            try{
                setOnCompletionListener { player ->
                    player.release()
                    finished()
                }
                setOnErrorListener { player, what, extra ->
                    player.release()
                    error()
                    true
                }
                setOnPreparedListener {
                    start()
                }

                setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
                prepare()
            }
            catch (e: Exception){
                error()
                return false
            }

        }

        return true
    }

    fun stop(){
        mediaPlayer?.apply {
            release()
        }
    }

}