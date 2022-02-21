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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * @author Vad Nik.
 * @version dated Dec 11, 2018.
 * @link http://github.com/vadniks
 */
internal data class FileView(var id: Int, var name: String, var isDirectory: Boolean, var isDoubleDot: Boolean) {
    lateinit var file: File

    constructor(id: Int, name: String, isDirectory: Boolean, file: File) : this(id, name, isDirectory, false) {
        this.file = file
    }

    fun getDisplayedImage(context: Context): Bitmap {
        try {
            file.isDirectory
        } catch (ex: UninitializedPropertyAccessException) {
            //ex.printStackTrace()
            return BitmapFactory.decodeResource(
                context.resources, R.drawable.folder_new)
        }

        return BitmapFactory.decodeResource(
            context.resources, when (true) {

                //TODO: check is showing hidden files (and folders) feature is enabled.

                file.isDirectory -> R.drawable.folder_new
                file.isPNG() -> R.drawable.png_new
                file.isJPG() -> R.drawable.jpg_new
                file.isText() -> R.drawable.text_new
                file.isVideo() -> R.drawable.video_new
                file.isMusic() -> R.drawable.audio_new
                //file.isGif() -> R.drawable.gif
                file.isArchive() -> R.drawable.zip_new
                file.isApk() -> R.drawable.apk_new
                file.isSymlink() -> R.drawable.symlink_new
                //file.isDirectory && file.isSymlink() -> R.drawable.folder_symlink

                //TODO: open original file when user clicks on symlink.

                else -> R.drawable.unknown_new
            }
        )
    }

    companion object {
        val DOUBLE_DOT_DIR = FileView(0, "..", true, true)

        private fun File.toFileView(id: Int): FileView = FileView(id, name, isDirectory, this)

        fun toThese(arr: ArrayList<File>): ArrayList<FileView> {
            val res = ArrayList<FileView>()

            for ((j, i) in arr.withIndex())
                res.add(i.toFileView(j))

            return res
        }
    }
}
