/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.draw.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import .mvp.model.individual.IIndividualModel
import .processing.draw.view.DrawViewDelegateGetter.getDrawViewDelegate

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
interface DrawViewDelegate : IIndividualModel {
    var bitmap: Bitmap?
    
    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    
    fun onDraw(canvas: Canvas)
    
    fun onTouchEvent(event: MotionEvent): Boolean
    
    fun switch()

    fun isSwitched(): Boolean

    fun changeOptions(bSize: Float, color: Int)
    
    companion object {
        
        fun get(dv: DrawView): DrawViewDelegate = getDrawViewDelegate(dv)
    }
}
