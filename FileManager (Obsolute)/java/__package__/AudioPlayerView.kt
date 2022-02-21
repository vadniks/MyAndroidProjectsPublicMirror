/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

/**
 * @author Vad Nik.
 * @version dated Dec 25, 2018.
 * @link http://github.com/vadniks
 */
interface AudioPlayerView {
    fun setProgressBarMax(max: Int)
    fun setProgressBarProgress(p: Int)
    fun setPlaying(b: Boolean)
    fun initControls(onBack: () -> Unit, onPlay: () -> Unit, onPause: () -> Unit, onForward: () -> Unit)
    fun setTitles(title: String, subtitle: String)
}
