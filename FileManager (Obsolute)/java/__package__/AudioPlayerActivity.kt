/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import kotlinx.android.synthetic.main.activity_audio_player.*
import java.io.File

/**
 * @author Vad Nik.
 * @version dated Dec 25, 2018.
 * @link http://github.com/vadniks
 */
@Deprecated("listener not working, todo")
class AudioPlayerActivity : AppCompatActivity(), AudioPlayerView {
    private var type: String? = null
    private lateinit var player: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        if (intent != null && intent.data != null) {
            type = intent.type
            player = AudioPlayer(File(intent.data!!.toString().substring("file://".length)), this, this as AudioPlayerView)
        }
    }

    override fun setProgressBarMax(max: Int): Unit = audio_pb.setMax(max)

    override fun setProgressBarProgress(p: Int): Unit = audio_pb.setProgress(p)

    override fun setPlaying(b: Boolean) {
        if (b) {
            audio_play.visibility = View.GONE
            audio_pause.visibility = View.VISIBLE
        } else {
            audio_play.visibility = View.VISIBLE
            audio_pause.visibility = View.GONE
        }
    }

    override fun initControls(onBack: () -> Unit, onPlay: () -> Unit, onPause: () -> Unit, onForward: () -> Unit) {
        audio_back.setOnClickListener { _ -> onBack.invoke() }
        audio_play.setOnClickListener { _ -> onPlay.invoke() }
        audio_pause.setOnClickListener { _ -> onPause.invoke() }
        audio_forward.setOnClickListener { _ -> onForward.invoke() }
    }

    override fun setTitles(title: String, subtitle: String) {
        audio_title.text = Editable.Factory.getInstance().newEditable(title)
        audio_subtitle.text = Editable.Factory.getInstance().newEditable(subtitle)
    }

    override fun onPause() {
        player.onFinish()
        super.onPause()
        finish()
    }
}
