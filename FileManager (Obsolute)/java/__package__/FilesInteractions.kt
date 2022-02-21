/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.webkit.MimeTypeMap
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Vad Nik.
 * @version dated Dec 11, 2018.
 * @link http://github.com/vadniks
 */

/**
 * @return list of files (can be directories) in the given directory,
 *         if can read the given folder.
 */
fun echoAll(path: String): ArrayList<File> {
    val arr = ArrayList<File>()

//    if (!File(path).canRead())
//        return arr

    for (file: File in MFile(path).listFiles())
        arr.add(file)

    return arr.sortAlphabetically()
}

private fun ArrayList<File>.sortAlphabetically(): ArrayList<File> {
    val order = Comparator<File> { st1, st2 ->
        val str1 = st1.name
        val str2 = st2.name
        var res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2)
        if (res == 0)
            res = str1.compareTo(str2)
        return@Comparator res
    }
    Collections.sort<File>(this, order)
    return this
}

fun File.extension(): String {
    return if (!isDirectory) {
        extension
//        val a = name.indexOf('.')
//        name.substring(if (a == 0) a else name.length-1)
    } else ""
}

fun File.isSymlink(): Boolean = absolutePath.compareTo(canonicalPath) != 0

fun File.isFolderSymlink(): Boolean = isDirectory && isSymlink() //TODO: add icon for folder symlinks and processing for them.

fun File.isPNG(): Boolean = extension() == "png"

fun File.isJPG(): Boolean {
    val s = extension()
    return s == "jpg" || s == "jpeg"
}

fun File.isMusic(): Boolean = extension() == "mp3"

fun File.isVideo(): Boolean = extension() == "mp4"

fun File.isGif(): Boolean = extension() == "gif"

fun File.isText(): Boolean = extension() == "txt"

fun File.isApk(): Boolean = extension() == "apk"

fun File.isArchive(): Boolean = extension() == "zip"

fun File.getMimeType(): String =
    if (extension() != "") MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension()) ?: "" else ""

@Deprecated("use default java's implementation instead, useless if rooted")
fun File._canRead(): Boolean {
    val proc = Runtime.getRuntime().exec(arrayOf("ls", "-l", "grep", this.name))
    val s = String(proc.inputStream.readBytes())

    println("testo permissions $s") //TODO: debug.

    return false
}

const val MIME_TEXT    = "text/*"
const val MIME_IMAGE   = "image/*"
const val MIME_VIDEO   = "video/mp4"
const val MIME_AUDIO   = "audio/mpeg"
const val MIME_ARCHIVE = "application/zip"
const val MIME_ANY     = "*/*"
