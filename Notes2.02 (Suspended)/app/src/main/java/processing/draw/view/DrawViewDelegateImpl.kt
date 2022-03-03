/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.draw.view

import android.graphics.*
import android.view.MotionEvent
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import kotlin.math.abs

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
private class DrawViewDelegateImpl(private val dv: DrawView, im: IIndividualModel) :
    IndividualModelWrapper(im), DrawViewDelegate {
    
    private var paint = Paint()
    private val path = Path()
    private val bPaint = Paint(Paint.DITHER_FLAG)
    private val cPaint = Paint()
    private val cPath = Path()
    override var bitmap: Bitmap? = null
    private lateinit var canvas: Canvas
    private var mX = 0.0f
    private var mY = 0.0f
    private var eraser = false
    
    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 12.0f
        paint.xfermode = null
    
        cPaint.isAntiAlias = true
        cPaint.isDither = true
        cPaint.color = Color.BLACK
        cPaint.style = Paint.Style.STROKE
        cPaint.strokeJoin = Paint.Join.MITER
        cPaint.strokeWidth = 4.0f
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (bitmap == null)
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
    }
    
    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap ?: return, 0.0f, 0.0f, bPaint)
        canvas.drawPath(path, paint)
        canvas.drawPath(cPath, cPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
    
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                dv.invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                dv.invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                dv.invalidate()
            }
        }
        return true
    }
    
    private fun touchStart(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        mX = x
        mY = y
    }
    
    private fun touchMove(x: Float, y: Float) {
        val dX = abs(x - mX)
        val dY = abs(y - mY)
        
        if (dX >= 4 || dY >= 4) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
            
            cPath.reset()
            cPath.addCircle(mX, mY, 10.0f, Path.Direction.CW)
        }
    }
    
    private fun touchUp() {
        path.lineTo(mX, mY)
        
        canvas.drawPath(path, paint)
        
        path.reset()
    }
    
    override fun switch() {
        eraser = !eraser
        if (eraser)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        else
            paint.xfermode = null
    }

    override fun isSwitched(): Boolean = eraser

    override fun changeOptions(bSize: Float, color: Int) {
        paint.strokeWidth = bSize
        paint.color = color
    }
}
