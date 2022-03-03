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

    private var startedCallback: () -> Unit = {}
    private var finishedCallback: () -> Unit = {}
    private var errorCallback: () -> Unit = {}

    fun setCallbacks(started: () -> Unit, finished: () -> Unit, error: () -> Unit){
        this.startedCallback = started
        this.finishedCallback = finished
        this.errorCallback = error
    }

    fun playUrlSound(soundUrl: String, started: () -> Unit, finished: () -> Unit, error: () -> Unit){
        if(isPlaying()){
            stop()
        }
        setCallbacks(started, finished, error)

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
                    startedCallback()
                }
                setOnCompletionListener { player ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    finishedCallback()
                }
                setOnErrorListener { player, what, extra ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    errorCallback()
                    true
                }

                setDataSource(soundUrl)
                prepare()
            }
            catch (e: Exception){
                errorCallback()
            }
        }
    }

    fun playLocalSound(soundName: String, finished: () -> Unit, error: () -> Unit) : Boolean{
        if(isPlaying()){
            stop()
        }
        setCallbacks({}, finished, error)

        val assetFileDescriptor = context.assets.openFd(soundName)
        var isSuccess = true

        mediaPlayer = MediaPlayer().apply {

            try{
                setOnCompletionListener { player ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    finishedCallback()
                }
                setOnErrorListener { player, what, extra ->
                    player.reset()
                    player.release()
                    mediaPlayer = null
                    errorCallback()
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
                errorCallback()
                isSuccess = false
            }

        }

        return isSuccess
    }

    fun stop(){
        if(isPlaying()){
            mediaPlayer?.apply {
                reset()
                release()
            }
            mediaPlayer = null
            finishedCallback()
        }
    }

    fun isPlaying(): Boolean{
        return mediaPlayer?.isPlaying ?: false
    }

}