package com.aaronfodor.android.songquiz.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected anywhere as a singleton
 */
@Singleton
class MediaPlayerService  @Inject constructor(
    @ApplicationContext val context: Context
) {

    private var mediaPlayer: MediaPlayer? = null

    fun playUrlSound(soundUrl: String, started: () -> Unit, finished: () -> Unit, error: () -> Unit){
        mediaPlayer = MediaPlayer().apply {
            try{
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener {
                    start()
                    started()
                }
                setOnCompletionListener { player ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    finished()
                }
                setOnErrorListener { player, what, extra ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    error()
                    true
                }

                setDataSource(soundUrl)
                prepare()
            }
            catch (e: Exception){
                error()
            }
        }
    }

    fun playLocalSound(soundName: String, finished: () -> Unit, error: () -> Unit) : Boolean{
        val assetFileDescriptor = context.assets.openFd(soundName)
        var isSuccess = true

        mediaPlayer = MediaPlayer().apply {

            try{
                setOnCompletionListener { player ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    finished()
                }
                setOnErrorListener { player, what, extra ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
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
                isSuccess = false
            }

        }

        return isSuccess
    }

    fun stop(){
        mediaPlayer?.apply {
            reset()
            release()
        }
        mediaPlayer = null
    }

}