/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.draw.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
class DrawView : View {
    private val delegate =
        DrawViewDelegate.get(this)
    
    constructor(context: Context) : super(context)
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        delegate.onSizeChanged(w, h, oldw, oldh)
    }
    
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        delegate.onDraw(canvas ?: return)
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return delegate.onTouchEvent(event ?: return super.onTouchEvent(event))
    }
    
    fun getBitmap(): Bitmap? = delegate.bitmap
    
    fun setBitmap(b: Bitmap) {
        delegate.bitmap = b
    }
    
    fun switch() = delegate.switch()

    fun isSwitched(): Boolean = delegate.isSwitched()

    fun changeOptions(bSize: Float, color: Int) = delegate.changeOptions(bSize, color)
}
