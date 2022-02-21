/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * @author Vad Nik.
 * @version dated Dec 25, 2018.
 * @link http://github.com/vadniks
 */
class AudioPlayer(private val file: File, context: Context, private val view: AudioPlayerView) {
    private val player = MediaPlayer()
    var isPlaying = false
    //private var watcher: Job
    private var stopWatcher = false

    init {
//        watcher = launch(CommonPool) {
//            while (true) {
//                if (stopWatcher)
//                    return@launch
//
//                if (isPlaying)
//                    setPointerAt(player.currentPosition)
//            }
//        }

        player.setDataSource(context, Uri.parse(file.path))
        player.setOnCompletionListener { _ ->
            pause(true)
        }

        view.setProgressBarMax(getAudioDuration())
        view.setProgressBarProgress(0)
        //view.setTitles(file.name, getMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR, context))

        //TODO: make AudioPlayer work (it's hanged when starts).

        view.initControls(
            this::rewindToStart,
            this::play,
            { pause() },
            this::rewindToEnd
        )
    }

    private fun play() {
        isPlaying = true
        view.setPlaying(true)

        player.start()
    }

    private fun pause(isComplete: Boolean = false) {
        isPlaying = false
        view.setPlaying(false)

        if (isComplete)
            return

        player.pause()
    }

    private fun setPointerAt(pos: Int) {
        pause()
        player.seekTo(pos)
        play()
    }

    private fun stop() {
        if (isPlaying) {
            player.stop()
            isPlaying = false
        }
        player.reset()

        stopWatcher = true
        //watcher.cancel()
    }

    private fun rewindToStart(): Unit = setPointerAt(0)

    private fun rewindToEnd(): Unit = setPointerAt(player.currentPosition+10)

    private fun getAudioDuration(): Int = player.duration

    private fun getMetadata(key: Int, context: Context): String {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, Uri.parse(file.path))
        return mmr.extractMetadata(key)
    }

    fun onFinish() {
        stop()
        player.release()
    }
}
