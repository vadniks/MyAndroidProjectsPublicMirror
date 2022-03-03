/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.draw.processing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import .R
import .common.*
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.common.Note
import .processing.draw.view.DrawView
import java.io.File
import java.io.FileOutputStream

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
private class DrawnImpl(private val note: Note?, private val context: Context, im: IIndividualModel) :
    IndividualModelWrapper(im), Drawn {
    
    private val isView = note != null
    private lateinit var title: EditText
    private lateinit var dv: DrawView
    private var selectedColor = COLOR_GREY_ID
    private var selectedSize = 1

    @SuppressLint("InflateParams")
    override fun initializeView(): View {
        model.crossPresenterModel.subscribeForMenuButton(this)
        
        val v = LayoutInflater.from(context).inflate(R.layout.fragment_draw, null)
        title = v.findViewById(R.id.fragment_draw_title)
        dv = v.findViewById(R.id.fragment_draw_draw)

        if (isView) {
            title.text = note?.title!!.toEditable()
            dv.setBitmap(load() ?: return v.apply { toast(context.getString(R.string.err_load_image), context) })
        }

        title.isEnabled = !isView

        return v
    }
    
    private fun load(): Bitmap? {
        val file = File(getDrawnFolder(), note!!.title + DRAWN_FILE_POSTFIX)
        if (!file.exists())
            return null
        
        return BitmapFactory.decodeFile(file.canonicalPath).copy(Bitmap.Config.ARGB_8888, true)
    }
    
    override fun onDestroy() = model.crossPresenterModel.unsubscribeOfMenuButton(this)
    
    override fun onMenuButtonClicked() = BottomSheetDialog(R.menu.menu_draw, context, isDark(context)) { id, instance ->
        instance.dismiss()
        
        when (id) {
            R.id.menu_draw_save -> save()
            R.id.menu_draw_delete -> delete()
            R.id.menu_draw_options -> options()
        }
    }.show()
    
    private fun save() {
        val t = title.text()

        val n =
            if (!isView && t.isNotBlank() && !checkDoubles(t))
                newNote(t, DRAWN_NOTATION, isDrawn = true)
            else if (isView)
                note!!
            else {
                notifyUserNoteAlreadyExists(context)
                return
            }
    
        doInCoroutine {
            if (!isView)
                save(n, context)
            else
                update(n)
        }
        save(t)
    
        model.crossPresenterModel.initiateMain(null)
    }
    
    private fun checkDoubles(t: String): Boolean =
        doBlocking<Boolean> { doesNoteAlreadyExist(t) } || File(t + DRAWN_FILE_POSTFIX).exists()
    
    private fun save(t: String) {
        val folder = getDrawnFolder()
        if (!folder.exists())
            folder.mkdir()
        
        val file = File(folder, t + DRAWN_FILE_POSTFIX)
        if (!file.exists())
            file.createNewFile()
        
        val fos = FileOutputStream(file)
        dv.getBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    }
    
    private fun delete() {
        doInCoroutine {
            deleteDrawn(title.text())
            delete(note!!, context)
        }
        
        model.crossPresenterModel.initiateMain(null)
    }
    
    @SuppressLint("InflateParams")
    private fun options() {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_draw_options, null)
        
        val seekBar = v.findViewById<SeekBar>(R.id.dialog_draw_options_seek_bar)
        seekBar.progress = selectedSize
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
    
            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                selectedSize = progress
            }
            
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        val spinner = v.findViewById<Spinner>(R.id.dialog_draw_options_spinner)
        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, COLOR_NAMES)
        spinner.setSelection(COLORS.indexOf(selectedColor), true)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
    
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, id: Long) {
                selectedColor = COLORS[id.toInt()]
            }
        }

        val switch = v.findViewById<Switch>(R.id.dialog_draw_options_switch)
        switch.isChecked = dv.isSwitched()
        switch.setOnCheckedChangeListener { _, _ -> dv.switch() }

        model.crossPresenterModel.showCustomDialog(context.getString(R.string.options), v, true) {
            it.setOnDismissListener { dv.changeOptions(selectedSize.toFloat(), selectedColor) }
        }
    }
    
    override fun deleteDrawn(title: String) {
        val file = File(getDrawnFolder(), title + DRAWN_FILE_POSTFIX)
        if (file.exists())
            file.delete()
    }
    
    override fun deleteAllDrawns() {
        for (i in getDrawnFolder().listFiles() ?: return)
            i.delete()
    }
    
    override fun restoreAllDrawns() {
        for (i in getDrawnFolder().listFiles() ?: return)
            save(newNote(i.name.substringBefore(DRAWN_FILE_POSTFIX), DRAWN_NOTATION, isDrawn = true), context)
    }

    override fun getFileFromTitle(t: String): File = File(getDrawnFolder(), t + DRAWN_FILE_POSTFIX)

    private fun getDrawnFolder(): File = File(model.getExternalStorageFolder(), DRAWN_FOLDER_POSTFIX)
    
    private companion object {
        private const val DRAWN_NOTATION = "<drawn>"
        private const val DRAWN_FILE_POSTFIX = ".png"
        private const val DRAWN_FOLDER_POSTFIX = "/Drawns"

        private val COLOR_NAMES = arrayOf(
            "White",
            "Red",
            "Blue",
            "Yellow",
            "Black",
            "Orange",
            "Green",
            "Grey",
            "Cyan",
            "Brown")
        private val COLORS = arrayOf(
            Color.WHITE,
            Color.RED,
            Color.BLUE,
            Color.YELLOW,
            Color.BLACK,
            Color.argb(100, 255, 111, 0),
            Color.GREEN,
            Color.GRAY,
            Color.CYAN,
            Color.argb(100, 126, 58, 28))

        private const val COLOR_GREY_ID = 7
    }
}
